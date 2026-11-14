package com.example.iching1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;

/** Uses all eighteen returned integers unchanged and in response order. */
public final class CoinSource {
    static final String URL_STRING = "https://www.random.org/integers/?num=18&min=2&max=3&col=1&base=10&format=plain&rnd=new";
    interface Online { int[] fetch() throws IOException; }
    interface Local { int[] fetch(); }
    public static final class Cast {
        final int[] coins;
        final String source;
        Cast(int[] coins, String source) { this.coins = coins; this.source = source; }
    }

    static Cast obtain(Online online, Local local) {
        try {
            int[] coins = online.fetch();
            validate(coins);
            return new Cast(coins, "RANDOM.ORG (atmospheric noise)");
        } catch (IOException | SecurityException unavailable) {
            // Use a complete local cast, never a mixture of online/local coins.
            return new Cast(local.fetch(), "system source (offline)");
        }
    }

    static void validate(int[] coins) throws IOException {
        if (coins == null || coins.length != 18) throw new IOException("Expected eighteen coins");
        for (int coin : coins) if (coin != 2 && coin != 3) throw new IOException("Invalid coin");
    }

    static int[] parse(String response) throws IOException {
        String[] tokens = response.trim().split("\\s+");
        if (tokens.length != 18) throw new IOException("Incomplete response");
        int[] coins = new int[18];
        for (int i = 0; i < coins.length; i++) {
            if (!tokens[i].equals("2") && !tokens[i].equals("3")) throw new IOException("Invalid coin");
            coins[i] = Integer.parseInt(tokens[i]);
        }
        return coins;
    }

    static int[] fetch() throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(URL_STRING).openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("User-Agent", "ThreeCoins-Android/1.4");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestProperty("Cache-Control", "no-cache");
        try {
            if (connection.getResponseCode() != 200) throw new IOException("Service unavailable");
            try (InputStream input = connection.getInputStream();
                 ByteArrayOutputStream body = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[256];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (body.size() + count > 4096) throw new IOException("Oversized response");
                    body.write(buffer, 0, count);
                }
                return parse(new String(body.toByteArray(), StandardCharsets.US_ASCII));
            }
        } finally {
            connection.disconnect();
        }
    }
}
