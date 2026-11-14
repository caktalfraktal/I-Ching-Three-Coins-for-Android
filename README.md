# Three Coins for Android v1.3

Android source for the Three Coins I Ching caster. Open the `android` folder in Android Studio. The current package is `com.example.iching1`, version name `1.3`, version code `3`, with Android 5.0 (API 21) as the minimum supported version.

## Included

- Complete Java app source, Android resources, manifest and Gradle build files.
- Gradle 8.9 wrapper, including its required wrapper JAR.
- Original launcher artwork in `assets/icon.png` and packaged Android resources.
- Casting tests covering all 4,096 line states, the 64 hexagrams, changing lines, coin order, probabilities, invalid responses and fallback.
- A build/signing helper that can create a fresh private signing key without asking for a password.

The Windows and Tarot Reader applications are outside this project.

## Casting behaviour

Each cast requests eighteen fresh integers, each 2 or 3, from RANDOM.ORG over HTTPS. The returned values are used in order, three per line, from the bottom line upward. Successful online values are never shuffled or used to seed another generator. RANDOM.ORG processes atmospheric noise into the integers it supplies.

The line totals 6, 7, 8 and 9 have three-coin probabilities 1/8, 3/8, 3/8 and 1/8. Only 6 and 9 change. Failed or malformed online responses cause a complete local cast using the original Android SecureRandom code. The source is displayed. The next cast attempts RANDOM.ORG again. No API key or account is required.

## Build an installable APK

Install Python 3, Android Studio and JDK 17 or 21. In Android Studio's SDK Manager, install Android SDK Platform 34 and Build-Tools 34.0.0. Set `JAVA_HOME` to your JDK directory and `ANDROID_HOME` to your Android SDK directory. Android Studio's bundled JDK can be used. Initial Gradle builds need internet access to download dependencies.

From this folder run the following command on Windows, macOS or Linux (use `python3` if that is your Python command).

```text
python scripts/build_android.py
```

The helper runs the release unit tests, builds the app, signs it and verifies its signature and alignment. The result is `release/ThreeCoins-Android.apk`.

You can supply the SDK location explicitly.

```text
python scripts/build_android.py --sdk "PATH_TO_ANDROID_SDK"
```

Build versions are controlled by `android/app/build.gradle`. Increase `versionCode` for each Android update, and set `versionName` to the version shown to users.

## Signing keys, forgotten passwords and lost keys

On its first run the helper creates `.private-signing/threecoins-release.p12` and `.private-signing/release-password.txt`. Later builds reuse them automatically. You do not need to remember or type the generated password. Back up both files together in private storage.

This public source folder does not contain the signing key used for the APK already installed on your phone. To preserve update compatibility, copy your existing `threecoins-release.p12` and `release-password.txt` privately into `.private-signing/` before building, or pass `--key-dir` pointing to their existing private directory. The current signing alias is `threecoins`.

If only one signing file survives, restore the missing file from a backup. If you lose the key or its password, create a new identity in a new private directory.

```text
python scripts/build_android.py --key-dir .private-signing/replacement
```

Use that same `--key-dir` for subsequent builds. A lost private key cannot be reconstructed from source, an APK or a certificate. You can continue development and build a working app with a new key, but Android requires uninstalling the app signed with the old key before installing the newly signed APK with the same package name. Uninstalling removes local app data. This describes direct APK installation; Google Play signing has its own recovery procedures.

## Copying this project to GitHub

Upload this folder's contents, including `.gitignore`, `.gitattributes`, and the Gradle wrapper JAR. The supplied folder contains source, icons, build instructions and the signed v1.3 APK in `release/ThreeCoins-Android-v1.3.apk`. It has no private keys, passwords, local SDK paths, personal IDE settings, build caches or Git history.

Generated keys, password files, build folders and local machine settings are excluded by `.gitignore`. The supplied release APK is intentionally included and may be uploaded to GitHub as a repository file or release asset. After building locally, do not upload `.private-signing/`, `.signing/`, or password/keystore files. Keep private backups separate from the GitHub copy.

The original project files were copied rather than moved. No license was added on the owner's behalf.
