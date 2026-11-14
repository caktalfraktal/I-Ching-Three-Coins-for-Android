package com.example.iching1;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class CastingTest {
    // Independent lower/upper trigram chart from I Ching with Clarity.
    private static final String[] TRIGRAMS = {"111","100","010","001","000","011","101","110"};
    private static final int[][] CHART = {
        {1,34,5,26,11,9,14,43}, {25,51,3,27,24,42,21,17},
        {6,40,29,4,7,59,64,47}, {33,62,39,52,15,53,56,31},
        {12,16,8,23,2,20,35,45}, {44,32,48,18,46,57,50,28},
        {13,55,63,22,36,37,30,49}, {10,54,60,41,19,61,38,58}
    };
    private int expected(String bits) {
        return CHART[Arrays.asList(TRIGRAMS).indexOf(bits.substring(0,3))]
                    [Arrays.asList(TRIGRAMS).indexOf(bits.substring(3))] - 1;
    }
    private int[] repeated(int value) { int[] coins = new int[18]; Arrays.fill(coins,value); return coins; }

    @Test public void onlineCoinsAreUnchangedAndNeverUseLocalRandomness() throws Exception {
        int[] coins = CoinSource.parse("2\n3\n3\n2\n2\n3\n3\n3\n3\n2\n2\n2\n3\n2\n3\n2\n3\n2\n");
        int[] expected = {2,3,3,2,2,3,3,3,3,2,2,2,3,2,3,2,3,2};
        assertArrayEquals(expected, coins);
        CoinSource.Cast cast = CoinSource.obtain(() -> coins, () -> { throw new AssertionError("Local source used"); });
        assertSame(coins, cast.coins);
        assertEquals("RANDOM.ORG (atmospheric noise)", cast.source);
        ArrayList<MainActivity.IChing.CoinTossResult> lines = MainActivity.IChing.linesFromCoins(coins);
        for (int i = 0; i < 6; i++) assertArrayEquals(Arrays.copyOfRange(coins,i*3,i*3+3), lines.get(i).coinValues);
    }

    @Test public void failedRequestFallsBackOnceAndRetriesOnlineNextCast() {
        AtomicInteger onlineCalls = new AtomicInteger();
        AtomicInteger localCalls = new AtomicInteger();
        CoinSource.Online online = () -> { onlineCalls.incrementAndGet(); throw new SocketTimeoutException(); };
        CoinSource.Local local = () -> { localCalls.incrementAndGet(); return repeated(2); };
        for (int i=0; i<2; i++) {
            CoinSource.Cast cast = CoinSource.obtain(online,local);
            assertArrayEquals(repeated(2),cast.coins);
            assertEquals("system source (offline)",cast.source);
        }
        assertEquals(2,onlineCalls.get()); assertEquals(2,localCalls.get());
    }

    @Test public void invalidResponsesUseACompleteLocalCast() {
        String[] invalid = {"", "2 3", "<html>Error</html>",
            "2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 4",
            "2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2.0",
            "2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2"};
        for (String text: invalid) {
            CoinSource.Cast cast = CoinSource.obtain(() -> CoinSource.parse(text), () -> repeated(3));
            assertArrayEquals(repeated(3),cast.coins);
            assertEquals("system source (offline)",cast.source);
        }
    }

    @Test public void all4096LineStatesMapAndChangeCorrectly() {
        int[][] samples={{2,2,2},{2,2,3},{2,3,3},{3,3,3}};
        for (int state=0; state<4096; state++) {
            int[] coins=new int[18];
            StringBuilder primary=new StringBuilder(), secondary=new StringBuilder();
            ArrayList<Integer> changes=new ArrayList<>();
            for (int i=0;i<6;i++) {
                int kind=(state >> (2*i)) & 3;
                System.arraycopy(samples[kind],0,coins,i*3,3);
                primary.append(kind==1 || kind==3 ? '1' : '0');
                secondary.append(kind==0 || kind==1 ? '1' : '0');
                if (kind==0 || kind==3) changes.add(i);
            }
            ArrayList<MainActivity.IChing.CoinTossResult> lines=MainActivity.IChing.linesFromCoins(coins);
            assertEquals(expected(primary.toString()),MainActivity.IChing.getPrimaryHexagramIndex(lines));
            assertEquals(expected(secondary.toString()),MainActivity.IChing.getPrimaryHexagramIndex(MainActivity.IChing.getSecondaryLines(lines)));
            int[] positions=MainActivity.IChing.getChangingLinePositions(lines);
            assertEquals(changes.size(),positions.length);
            for (int i=0;i<positions.length;i++) assertEquals((int)changes.get(i),positions[i]);
        }
    }

    @Test public void threeCoinsHaveOneThreeThreeOneProbabilities() {
        int[] counts=new int[4];
        for(int bits=0;bits<8;bits++) {
            int[] coins=repeated(2);
            for(int i=0;i<3;i++) coins[i]=((bits>>i)&1)==0 ? 2 : 3;
            counts[MainActivity.IChing.linesFromCoins(coins).get(0).lineValue-6]++;
        }
        assertArrayEquals(new int[]{1,3,3,1},counts);
    }
}
