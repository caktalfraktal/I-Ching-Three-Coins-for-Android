package com.example.iching1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.widget.Toast;

import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static class IChing {

        public static class CoinTossResult {
            public int[] coinValues; // each coin is 2 or 3
            public int sum;          // total of the three coins: 6..9
            public int lineValue;    // final line value: 6..9

            public CoinTossResult(int[] coinValues, int sum, int lineValue) {
                this.coinValues = coinValues;
                this.sum = sum;
                this.lineValue = lineValue;
            }
        }

        // The 64 hexagrams data: [ symbol, name, short meaning ]
        private static final String[][] HEXAGRAMS = {
            {"䷀", "The Creative", "Pure, dynamic energy and the force of creation."},
            {"䷁", "The Receptive", "Receptivity, nurturance and supportiveness."},
            {"䷂", "Difficulty at the Beginning", "Initial hurdles or obstacles."},
            {"䷃", "Youthful Folly", "Inexperience requiring instruction and guidance."},
            {"䷄", "Waiting", "Patience and readiness for the right timing."},
            {"䷅", "Conflict", "Opposition or tension that requires balancing."},
            {"䷆", "The Army", "Discipline and collective efforts."},
            {"䷇", "Holding Together", "Harmony and mutual support rooted in shared values and principles."},
            {"䷈", "Small Taming", "Modest influence or gentle guidance."},
            {"䷉", "Treading", "Careful consideration and measured steps."},
            {"䷊", "Peace", "Serenity, tranquility and stability."},
            {"䷋", "Standstill", "Stagnation or a period of reflection and preparation."},
            {"䷌", "Fellowship", "Harmonious collaboration towards a shared vision."},
            {"䷍", "Great Possession", "Responsibility and wise use of abundance."},
            {"䷎", "Humbling", "Modesty and receptivity fostering balance and success."},
            {"䷏", "Enthusiasm", "Excitement and creative spark."},
            {"䷐", "Following", "Adaptation and going with the flow."},
            {"䷑", "Work on the Decayed", "Rectifying decay through addressing the root cause."},
            {"䷒", "Approach", "Favourable momentum or advancing step."},
            {"䷓", "Contemplation", "Looking inward or inner inquiry."},
            {"䷔", "Biting Through", "Perseverance in the face of adversity."},
            {"䷕", "Grace", "Outward elegance reflecting inner worth."},
            {"䷖", "Splitting Apart", "A natural decline requiring patience and acceptance."},
            {"䷗", "Return", "Renewal, restoration, cautious progress after a setback."},
            {"䷘", "Innocence", "Pure intentions, naturalness and spontaneity."},
            {"䷙", "Great Taming", "Mastery over oneself or one's circumstances."},
            {"䷚", "Mouth Corners", "Self-cultivation through proper nourishment."},
            {"䷛", "Critical Mass", "A threshold or tipping point that requires proper management."},
            {"䷜", "The Abysmal Water", "Navigating challenges with caution and resilience."},
            {"䷝", "The Clinging Fire", "Clarity and radiance sustained by proper support."},
            {"䷞", "Influence", "Magnetic attraction of two things leading to connection and mutual influence."},
            {"䷟", "Duration", "Perseverance and endurance."},
            {"䷠", "Retreat", "Tactical retreat and reorientation."},
            {"䷡", "Great Power", "Exercising power with restraint and integrity."},
            {"䷢", "Progress", "Advancement and gradual unfolding."},
            {"䷣", "Darkening of the Light", "Challenge or darkness that invites illumination."},
            {"䷤", "The Family", "Primary relationships or close bonds."},
            {"䷥", "Opposition", "Opposing polarities and the need for reconciliation."},
            {"䷦", "Obstruction", "Difficulties and obstacles requiring careful navigation and wise counsel."},
            {"䷧", "Deliverance", "Liberation or breakthrough."},
            {"䷨", "Decrease", "Releasing or surrendering what no longer serves."},
            {"䷩", "Increase", "Growth or amplification."},
            {"䷪", "Breakthrough", "Decisive action to remove obstacles or influences and achieve resolution."},
            {"䷫", "Coming to Meet", "Unexpected encounter that requires cautious discernment."},
            {"䷬", "Gathering Together", "Uniting around a shared purpose or center."},
            {"䷭", "Pushing Upward", "Steady progress through persistent effort."},
            {"䷮", "Oppression", "Weight or pressure that demands release."},
            {"䷯", "The Well", "A shared source of sustenance requiring maintenance and proper use."},
            {"䷰", "Revolution", "Necessary radical change and the shedding of the old."},
            {"䷱", "The Cauldron", "Transformation and refinement leading to nourishment and benefit."},
            {"䷲", "The Arousing Thunder", "Sudden shock requiring composure and adaptation."},
            {"䷳", "The Keeping Still Mountain", "Inner stillness and the restraint of action."},
            {"䷴", "Development", "Steady, sequential advancement."},
            {"䷵", "The Marrying Maiden", "Joining an existing order or structure."},
            {"䷶", "Abundance", "A time of great prosperity and energy."},
            {"䷷", "The Wanderer", "A time of temporary stay and uncertain circumstances."},
            {"䷸", "The Gentle Wind", "Subtle and gradual influence."},
            {"䷹", "The Joyous Lake", "Shared joy through openhearted connection."},
            {"䷺", "Dispersion", "Dissolving what no longer serves, clearing a blocked path."},
            {"䷻", "Limitation", "Setting boundaries, knowing limits or recognition of constraints."},
            {"䷼", "Inner Truth", "Inner sincerity and being true to yourself."},
            {"䷽", "Small Exceeding", "Emphasis on small actions and restraint in large matters."},
            {"䷾", "After Completion", "Accomplishment, fulfilment and new beginnings."},
            {"䷿", "Before Completion", "The final stages of a cycle or project."}
        };

        // Binary mapping for the 64 hexagrams
        private static final Map<Integer, Integer> BINARY_TO_HEXAGRAM = new HashMap<>();
        static {
            BINARY_TO_HEXAGRAM.put(0b111111, 0);
            BINARY_TO_HEXAGRAM.put(0b000000, 1);
            BINARY_TO_HEXAGRAM.put(0b100010, 2);
            BINARY_TO_HEXAGRAM.put(0b010001, 3);
            BINARY_TO_HEXAGRAM.put(0b111010, 4);
            BINARY_TO_HEXAGRAM.put(0b010111, 5);
            BINARY_TO_HEXAGRAM.put(0b010000, 6);
            BINARY_TO_HEXAGRAM.put(0b000010, 7);
            BINARY_TO_HEXAGRAM.put(0b111011, 8);
            BINARY_TO_HEXAGRAM.put(0b110111, 9);
            BINARY_TO_HEXAGRAM.put(0b111000, 10);
            BINARY_TO_HEXAGRAM.put(0b000111, 11);
            BINARY_TO_HEXAGRAM.put(0b101111, 12);
            BINARY_TO_HEXAGRAM.put(0b111101, 13);
            BINARY_TO_HEXAGRAM.put(0b001000, 14);
            BINARY_TO_HEXAGRAM.put(0b000100, 15);
            BINARY_TO_HEXAGRAM.put(0b100110, 16);
            BINARY_TO_HEXAGRAM.put(0b011001, 17);
            BINARY_TO_HEXAGRAM.put(0b110000, 18);
            BINARY_TO_HEXAGRAM.put(0b000011, 19);
            BINARY_TO_HEXAGRAM.put(0b100101, 20);
            BINARY_TO_HEXAGRAM.put(0b101001, 21);
            BINARY_TO_HEXAGRAM.put(0b000001, 22);
            BINARY_TO_HEXAGRAM.put(0b100000, 23);
            BINARY_TO_HEXAGRAM.put(0b100111, 24);
            BINARY_TO_HEXAGRAM.put(0b111001, 25);
            BINARY_TO_HEXAGRAM.put(0b100001, 26);
            BINARY_TO_HEXAGRAM.put(0b011110, 27);
            BINARY_TO_HEXAGRAM.put(0b010010, 28);
            BINARY_TO_HEXAGRAM.put(0b101101, 29);
            BINARY_TO_HEXAGRAM.put(0b001110, 30);
            BINARY_TO_HEXAGRAM.put(0b011100, 31);
            BINARY_TO_HEXAGRAM.put(0b001111, 32);
            BINARY_TO_HEXAGRAM.put(0b111100, 33);
            BINARY_TO_HEXAGRAM.put(0b000101, 34);
            BINARY_TO_HEXAGRAM.put(0b101000, 35);
            BINARY_TO_HEXAGRAM.put(0b101011, 36);
            BINARY_TO_HEXAGRAM.put(0b110101, 37);
            BINARY_TO_HEXAGRAM.put(0b001010, 38);
            BINARY_TO_HEXAGRAM.put(0b010100, 39);
            BINARY_TO_HEXAGRAM.put(0b110001, 40);
            BINARY_TO_HEXAGRAM.put(0b100011, 41);
            BINARY_TO_HEXAGRAM.put(0b111110, 42);
            BINARY_TO_HEXAGRAM.put(0b011111, 43);
            BINARY_TO_HEXAGRAM.put(0b000110, 44);
            BINARY_TO_HEXAGRAM.put(0b011000, 45);
            BINARY_TO_HEXAGRAM.put(0b010110, 46);
            BINARY_TO_HEXAGRAM.put(0b011010, 47);
            BINARY_TO_HEXAGRAM.put(0b101110, 48);
            BINARY_TO_HEXAGRAM.put(0b011101, 49);
            BINARY_TO_HEXAGRAM.put(0b100100, 50);
            BINARY_TO_HEXAGRAM.put(0b001001, 51);
            BINARY_TO_HEXAGRAM.put(0b001011, 52);
            BINARY_TO_HEXAGRAM.put(0b110100, 53);
            BINARY_TO_HEXAGRAM.put(0b101100, 54);
            BINARY_TO_HEXAGRAM.put(0b001101, 55);
            BINARY_TO_HEXAGRAM.put(0b011011, 56);
            BINARY_TO_HEXAGRAM.put(0b110110, 57);
            BINARY_TO_HEXAGRAM.put(0b010011, 58);
            BINARY_TO_HEXAGRAM.put(0b110010, 59);
            BINARY_TO_HEXAGRAM.put(0b110011, 60);
            BINARY_TO_HEXAGRAM.put(0b001100, 61);
            BINARY_TO_HEXAGRAM.put(0b101010, 62);
            BINARY_TO_HEXAGRAM.put(0b010101, 63);
        }

        /**
         * Each coin toss tries "NativePRNGBlocking". If unavailable,
         * fallback to default SecureRandom.
         */
        public static CoinTossResult generateLine() {
            int[] coinValues = new int[3];
            int sum = 0;
            for (int i = 0; i < 3; i++) {
                SecureRandom coinRandom;
                try {
                    // Attempt to get "NativePRNGBlocking"
                    coinRandom = SecureRandom.getInstance("NativePRNGBlocking");
                } catch (NoSuchAlgorithmException e) {
                    // Fallback to default
                    coinRandom = new SecureRandom();
                }

                int flip = coinRandom.nextInt(2); // 0 or 1
                coinValues[i] = (flip == 0 ? 2 : 3);
                sum += coinValues[i];
            }

            int lineVal;
            switch (sum) {
                case 6: lineVal = 6; break;  // old yin
                case 7: lineVal = 7; break;  // young yang
                case 8: lineVal = 8; break;  // young yin
                default: lineVal = 9;         // old yang
            }
            return new CoinTossResult(coinValues, sum, lineVal);
        }

        public static ArrayList<CoinTossResult> castHexagramFlips() {
            ArrayList<CoinTossResult> lines = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                lines.add(generateLine());
            }
            return lines;
        }

        public static int linesToBinary(ArrayList<CoinTossResult> lines) {
            StringBuilder sb = new StringBuilder();
            for (CoinTossResult c : lines) {
                if (c.lineValue == 6 || c.lineValue == 8) sb.append('0');
                else sb.append('1');
            }
            return Integer.parseInt(sb.toString(), 2);
        }

        public static int getPrimaryHexagramIndex(ArrayList<CoinTossResult> lines) {
            int binValue = linesToBinary(lines);
            return BINARY_TO_HEXAGRAM.get(binValue);
        }

        public static int[] getChangingLinePositions(ArrayList<CoinTossResult> lines) {
            ArrayList<Integer> changes = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++) {
                int lv = lines.get(i).lineValue;
                if (lv == 6 || lv == 9) changes.add(i);
            }
            int[] arr = new int[changes.size()];
            for (int i = 0; i < changes.size(); i++) {
                arr[i] = changes.get(i);
            }
            return arr;
        }

        public static ArrayList<CoinTossResult> getSecondaryLines(ArrayList<CoinTossResult> lines) {
            ArrayList<CoinTossResult> changed = new ArrayList<>();
            for (CoinTossResult c : lines) {
                int newVal = c.lineValue;
                if (newVal == 6) newVal = 7;
                else if (newVal == 9) newVal = 8;
                changed.add(new CoinTossResult(c.coinValues, c.sum, newVal));
            }
            return changed;
        }

        public static SpannableStringBuilder performDivination() {
            CoinSource.Cast cast = CoinSource.obtain(CoinSource::fetch, () -> {
                ArrayList<CoinTossResult> local = castHexagramFlips();
                int[] coins = new int[18];
                for (int i = 0; i < 6; i++)
                    System.arraycopy(local.get(i).coinValues, 0, coins, i * 3, 3);
                return coins;
            });
            ArrayList<CoinTossResult> lines = linesFromCoins(cast.coins);

            int primaryIdx = getPrimaryHexagramIndex(lines);
            int[] changes = getChangingLinePositions(lines);
            Integer secondaryIdx = null;
            if (changes.length > 0) {
                ArrayList<CoinTossResult> secLines = getSecondaryLines(lines);
                secondaryIdx = getPrimaryHexagramIndex(secLines);
            }

            SpannableStringBuilder sb = new SpannableStringBuilder();

            sb.append("Coin Tosses:\n");
            for (int i = 0; i < lines.size(); i++) {
                CoinTossResult c = lines.get(i);
                sb.append("Line ").append(String.valueOf(i + 1))
                  .append(": Coins: [")
                  .append(String.valueOf(c.coinValues[0])).append(", ")
                  .append(String.valueOf(c.coinValues[1])).append(", ")
                  .append(String.valueOf(c.coinValues[2])).append("], Total: ")
                  .append(String.valueOf(c.sum))
                  .append("\n");
            }
            sb.append("\n");

            sb.append("Primary Hexagram: [");
            for (int i = 0; i < lines.size(); i++) {
                sb.append(String.valueOf(lines.get(i).lineValue));
                if (i < lines.size() - 1) sb.append(", ");
            }
            sb.append("]\n\n");

            sb.append("Primary Hexagram:\n");
            String symbol = HEXAGRAMS[primaryIdx][0];
            SpannableStringBuilder symbolSpan = new SpannableStringBuilder(symbol);
            symbolSpan.setSpan(
                new RelativeSizeSpan(2.5f),
                0,
                symbol.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            sb.append("Hexagram: ");
            sb.append(symbolSpan);
            sb.append("\n");

            sb.append("Name: ")
              .append(String.valueOf(primaryIdx + 1)).append(" ")
              .append(HEXAGRAMS[primaryIdx][1]).append("\n");
            sb.append("Represents: ").append(HEXAGRAMS[primaryIdx][2]).append("\n\n");

            if (secondaryIdx != null) {
                sb.append("Secondary Hexagram (after changes):\n");
                String secSymbol = HEXAGRAMS[secondaryIdx][0];
                SpannableStringBuilder secSpan = new SpannableStringBuilder(secSymbol);
                secSpan.setSpan(
                    new RelativeSizeSpan(2.5f),
                    0,
                    secSymbol.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                sb.append("Hexagram: ");
                sb.append(secSpan);
                sb.append("\n");
                sb.append("Name: ")
                  .append(String.valueOf(secondaryIdx + 1)).append(" ")
                  .append(HEXAGRAMS[secondaryIdx][1]).append("\n");
                sb.append("Represents: ").append(HEXAGRAMS[secondaryIdx][2]).append("\n");
            } else {
                sb.append("No changing lines - no secondary hexagram.\n");
            }

            sb.append("\nRandomness source: ").append(cast.source);
            return sb;
        }

        static ArrayList<CoinTossResult> linesFromCoins(int[] coins) {
            ArrayList<CoinTossResult> lines = new ArrayList<>();
            for (int i = 0; i < 18; i += 3) {
                int[] values = {coins[i], coins[i + 1], coins[i + 2]};
                int sum = values[0] + values[1] + values[2];
                lines.add(new CoinTossResult(values, sum, sum));
            }
            return lines;
        }
    }

    private TextView textResult;
    private Button buttonCastHexagram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textResult = findViewById(R.id.textResult);
        buttonCastHexagram = findViewById(R.id.buttonCastHexagram);

        CastModel model = new ViewModelProvider(this).get(CastModel.class);
        if (model.state.getValue() == null && savedInstanceState != null) {
            CharSequence reading = savedInstanceState.getCharSequence("reading", "");
            model.state.setValue(new CastState(false, reading, false));
        }
        model.state.observe(this, state -> {
            buttonCastHexagram.setEnabled(!state.busy);
            buttonCastHexagram.setText(state.busy ? "Contacting RANDOM.ORG..." : getString(R.string.cast_hexagram));
            if (state.reading != null) textResult.setText(state.reading);
            if (state.error) {
                Toast.makeText(this, "Unable to complete this cast. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
        buttonCastHexagram.setOnClickListener(view -> model.cast());
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putCharSequence("reading", textResult.getText());
        super.onSaveInstanceState(state);
    }

    static final class CastState {
        final boolean busy;
        final CharSequence reading;
        final boolean error;
        CastState(boolean busy, CharSequence reading, boolean error) {
            this.busy = busy;
            this.reading = reading;
            this.error = error;
        }
    }

    public static class CastModel extends ViewModel {
        final MutableLiveData<CastState> state = new MutableLiveData<>();
        private final ExecutorService worker = Executors.newSingleThreadExecutor();
        public void cast() {
            CastState previous = state.getValue();
            if (previous != null && previous.busy) return;
            CharSequence reading = previous == null ? null : previous.reading;
            state.setValue(new CastState(true, reading, false));
            worker.execute(() -> {
                try {
                    state.postValue(new CastState(false, IChing.performDivination(), false));
                } catch (RuntimeException failure) {
                    state.postValue(new CastState(false, reading, true));
                }
            });
        }
        @Override protected void onCleared() { worker.shutdownNow(); }
    }
}
