package matching;

import java.util.ArrayList;
import java.util.List;

public class RabinKarpMatcher {
    private static final long BASE = 257;
    private static final long MODULUS = 1_000_000_007;

    public static MatchResult search(String text, String pattern) {
        checkInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        int textLength = text.length();
        int patternLength = pattern.length();

        if (patternLength == 0) {
            for (int index = 0; index <= textLength; index++) {
                matches.add(index);
            }
            return new MatchResult(matches, 0);
        }
        if (patternLength > textLength) {
            return new MatchResult(matches, 0);
        }

        long highestPower = 1;
        for (int i = 1; i < patternLength; i++) {
            highestPower = (highestPower * BASE) % MODULUS;
        }

        long patternHash = hash(pattern);
        long windowHash = hash(text.substring(0, patternLength));
        long comparisons = 0;
        for (int start = 0; start <= textLength - patternLength; start++) {
            if (windowHash == patternHash) {
                int offset = 0;
                while (offset < patternLength) {
                    comparisons++;
                    if (text.charAt(start + offset) != pattern.charAt(offset)) {
                        break;
                    }
                    offset++;
                }
                if (offset == patternLength) {
                    matches.add(start);
                }
            }

            if (start < textLength - patternLength) {
                long withoutFirst = (windowHash - text.charAt(start) * highestPower % MODULUS + MODULUS)
                        % MODULUS;
                windowHash = (withoutFirst * BASE + text.charAt(start + patternLength)) % MODULUS;
            }
        }
        return new MatchResult(matches, comparisons);
    }

    private static long hash(String value) {
        long result = 0;
        for (int i = 0; i < value.length(); i++) {
            result = (result * BASE + value.charAt(i)) % MODULUS;
        }
        return result;
    }

    private static void checkInputs(String text, String pattern) {
        if (text == null || pattern == null) {
            throw new IllegalArgumentException("Text and pattern cannot be null");
        }
    }
}
