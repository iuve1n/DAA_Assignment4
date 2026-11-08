import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import matching.KmpMatcher;
import matching.MatchResult;
import matching.NaiveMatcher;
import matching.RabinKarpMatcher;

public class StringMatchingTests {
    public static void main(String[] args) {
        testBeginningMiddleAndEnd();
        testMultipleOverlappingMatches();
        testAbsentAndLongerPattern();
        testEmptyPattern();
        testRabinKarpCollisionVerification();
        System.out.println("String matching tests passed");
    }

    private static void testBeginningMiddleAndEnd() {
        checkAll("abcdefgh", "abc", Arrays.asList(0));
        checkAll("abcdefgh", "def", Arrays.asList(3));
        checkAll("abcdefgh", "fgh", Arrays.asList(5));
    }

    private static void testMultipleOverlappingMatches() {
        checkAll("aaaaa", "aa", Arrays.asList(0, 1, 2, 3));
        checkAll("ababa", "aba", Arrays.asList(0, 2));
    }

    private static void testAbsentAndLongerPattern() {
        checkAll("abcdefgh", "xyz", Collections.emptyList());
        checkAll("ab", "abcd", Collections.emptyList());
    }

    private static void testEmptyPattern() {
        checkAll("abc", "", Arrays.asList(0, 1, 2, 3));
        checkAll("", "", Arrays.asList(0));
    }

    private static void testRabinKarpCollisionVerification() {
        String text = "\u0000\u0101";
        String pattern = "\u0001\u0000";
        checkAll(text, pattern, Collections.emptyList());
    }

    private static void checkAll(String text, String pattern, List<Integer> expected) {
        MatchResult naive = NaiveMatcher.search(text, pattern);
        MatchResult kmp = KmpMatcher.search(text, pattern);
        MatchResult rabinKarp = RabinKarpMatcher.search(text, pattern);
        check(naive.getIndices().equals(expected), "naive matcher returned wrong indices");
        check(kmp.getIndices().equals(expected), "KMP returned wrong indices");
        check(rabinKarp.getIndices().equals(expected), "Rabin-Karp returned wrong indices");
        check(naive.getIndices().equals(kmp.getIndices()), "naive and KMP results should agree");
        check(naive.getIndices().equals(rabinKarp.getIndices()), "naive and Rabin-Karp results should agree");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
