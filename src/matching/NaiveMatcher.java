package matching;

import java.util.ArrayList;
import java.util.List;

public class NaiveMatcher {
    public static MatchResult search(String text, String pattern) {
        checkInputs(text, pattern);
        List<Integer> matches = new ArrayList<>();
        long comparisons = 0;

        if (pattern.isEmpty()) {
            for (int index = 0; index <= text.length(); index++) {
                matches.add(index);
            }
            return new MatchResult(matches, comparisons);
        }

        for (int start = 0; start <= text.length() - pattern.length(); start++) {
            int offset = 0;
            while (offset < pattern.length()) {
                comparisons++;
                if (text.charAt(start + offset) != pattern.charAt(offset)) {
                    break;
                }
                offset++;
            }
            if (offset == pattern.length()) {
                matches.add(start);
            }
        }
        return new MatchResult(matches, comparisons);
    }

    private static void checkInputs(String text, String pattern) {
        if (text == null || pattern == null) {
            throw new IllegalArgumentException("Text and pattern cannot be null");
        }
    }
}
