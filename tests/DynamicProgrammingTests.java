import dp.EditDistance;
import dp.LongestCommonSubsequence;

public class DynamicProgrammingTests {
    public static void main(String[] args) {
        testLcs();
        testEditDistance();
        System.out.println("Dynamic programming tests passed");
    }

    private static void testLcs() {
        check(LongestCommonSubsequence.length("ABCBDAB", "BDCABA") == 4,
                "known LCS should have length four");
        check(LongestCommonSubsequence.length("same", "same") == 4,
                "identical strings should match fully");
        check(LongestCommonSubsequence.length("abc", "XYZ") == 0,
                "different strings should have no common subsequence");
        check(LongestCommonSubsequence.length("", "abc") == 0,
                "empty string should have an LCS length of zero");
        check(LongestCommonSubsequence.length("abc", "") == 0,
                "empty second string should have an LCS length of zero");
    }

    private static void testEditDistance() {
        check(EditDistance.distance("same", "same") == 0,
                "identical strings should have distance zero");
        check(EditDistance.distance("", "word") == 4,
                "distance from empty string should be the other length");
        check(EditDistance.distance("kitten", "sitting") == 3,
                "known edit distance should be three");
        check(EditDistance.distance("cat", "cats") == 1,
                "one insertion should cost one");
        check(EditDistance.distance("cats", "cat") == 1,
                "one deletion should cost one");
        check(EditDistance.distance("cat", "cut") == 1,
                "one substitution should cost one");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
