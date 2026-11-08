package matching;

import java.util.ArrayList;
import java.util.List;

public class KmpMatcher {
    public static MatchResult search(String text, String pattern) {
        checkInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        if (pattern.isEmpty()) {
            for (int index = 0; index <= text.length(); index++) {
                matches.add(index);
            }
            return new MatchResult(matches, 0);
        }

        long[] comparisons = {0};
        int[] lps = buildLps(pattern, comparisons);
        int textIndex = 0;
        int patternIndex = 0;
        while (textIndex < text.length()) {
            comparisons[0]++;
            if (text.charAt(textIndex) == pattern.charAt(patternIndex)) {
                textIndex++;
                patternIndex++;
                if (patternIndex == pattern.length()) {
                    matches.add(textIndex - patternIndex);
                    patternIndex = lps[patternIndex - 1];
                }
            } else if (patternIndex > 0) {
                patternIndex = lps[patternIndex - 1];
            } else {
                textIndex++;
            }
        }
        return new MatchResult(matches, comparisons[0]);
    }

    private static int[] buildLps(String pattern, long[] comparisons) {
        int[] lps = new int[pattern.length()];
        int prefixLength = 0;
        int index = 1;
        while (index < pattern.length()) {
            comparisons[0]++;
            if (pattern.charAt(index) == pattern.charAt(prefixLength)) {
                prefixLength++;
                lps[index] = prefixLength;
                index++;
            } else if (prefixLength > 0) {
                prefixLength = lps[prefixLength - 1];
            } else {
                lps[index] = 0;
                index++;
            }
        }
        return lps;
    }

    private static void checkInputs(String text, String pattern) {
        if (text == null || pattern == null) {
            throw new IllegalArgumentException("Text and pattern cannot be null");
        }
    }
}
