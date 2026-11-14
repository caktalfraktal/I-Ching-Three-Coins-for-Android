"""Build and sign ThreeCoins Android. Private keys stay outside tracked source."""
import argparse
import os
from pathlib import Path
import secrets
import shutil
import subprocess

ROOT = Path(__file__).resolve().parent.parent


def run(arguments, **kwargs):
    subprocess.run([str(value) for value in arguments], check=True, **kwargs)


def java_tool(name):
    suffix = '.exe' if os.name == 'nt' else ''
    home = os.environ.get('JAVA_HOME')
    candidate = Path(home) / 'bin' / (name + suffix) if home else None
    if candidate and candidate.is_file():
        return str(candidate)
    found = shutil.which(name)
    if not found:
        raise SystemExit('Set JAVA_HOME to a JDK 17 or 21 installation.')
    return found


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--sdk', default=os.environ.get('ANDROID_HOME') or os.environ.get('ANDROID_SDK_ROOT'), help='Android SDK directory')
    parser.add_argument('--key-dir', type=Path, default=ROOT / '.private-signing', help='Private directory containing the signing key and password file')
    parser.add_argument('--unsigned-apk', type=Path, help='Sign an already-built APK instead of rebuilding')
    args = parser.parse_args()
    if not args.sdk:
        raise SystemExit('Set ANDROID_HOME or pass --sdk with the Android SDK directory.')
    sdk = Path(args.sdk).resolve()
    build_tools = sdk / 'build-tools' / '34.0.0'
    suffix = '.exe' if os.name == 'nt' else ''
    align = build_tools / ('zipalign' + suffix)
    signer = build_tools / 'lib' / 'apksigner.jar'
    if not align.is_file() or not signer.is_file():
        raise SystemExit('Install Android SDK Build-Tools 34.0.0 using Android Studio SDK Manager.')
    java = java_tool('java')
    keytool = java_tool('keytool')
    environment = os.environ.copy()
    environment['ANDROID_HOME'] = str(sdk)
    if args.unsigned_apk:
        unsigned = args.unsigned_apk.resolve()
    else:
        project = ROOT / 'android'
        wrapper = [str(project / 'gradlew.bat')] if os.name == 'nt' else ['sh', str(project / 'gradlew')]
        run(wrapper + ['--no-daemon', '--console=plain', ':app:testReleaseUnitTest', ':app:assembleRelease'], cwd=project, env=environment)
        unsigned = project / 'app/build/outputs/apk/release/app-release-unsigned.apk'
    if not unsigned.is_file():
        raise SystemExit('Unsigned APK not found: ' + str(unsigned))
    private = args.key_dir.resolve()
    private.mkdir(parents=True, exist_ok=True)
    key = private / 'threecoins-release.p12'
    password_file = private / 'release-password.txt'
    if key.exists() != password_file.exists():
        raise SystemExit('Only part of the signing backup exists. Restore both files, or use --key-dir .private-signing/replacement to create a new identity. A new identity requires reinstalling the app.')
    if not key.exists():
        password = secrets.token_urlsafe(40)
        with password_file.open('x', encoding='utf-8') as stream:
            stream.write(password)
        password_file.chmod(0o600)
        environment['THREECOINS_KEY_PASSWORD'] = password
        run([keytool, '-genkeypair', '-keystore', key, '-storetype', 'PKCS12', '-alias', 'threecoins', '-keyalg', 'RSA', '-keysize', '3072', '-validity', '10000', '-dname', 'CN=Three Coins Android', '-storepass:env', 'THREECOINS_KEY_PASSWORD', '-keypass:env', 'THREECOINS_KEY_PASSWORD'], env=environment)
        key.chmod(0o600)
        print('Created a new signing identity. Keep this private directory backed up. Existing installations signed with another key must be uninstalled first.')
    environment['THREECOINS_KEY_PASSWORD'] = password_file.read_text(encoding='utf-8').strip()
    output_dir = ROOT / 'release'
    output_dir.mkdir(exist_ok=True)
    aligned = output_dir / 'threecoins-aligned.apk'
    output = output_dir / 'ThreeCoins-Android.apk'
    run([align, '-f', '-p', '4', unsigned, aligned])
    run([java, '-jar', signer, 'sign', '--ks', key, '--ks-key-alias', 'threecoins', '--ks-pass', 'env:THREECOINS_KEY_PASSWORD', '--v4-signing-enabled', 'false', '--out', output, aligned], env=environment)
    run([java, '-jar', signer, 'verify', '--verbose', output])
    run([align, '-c', '-p', '4', output])
    aligned.unlink()
    print('Installable APK: ' + str(output))


if __name__ == '__main__':
    main()
