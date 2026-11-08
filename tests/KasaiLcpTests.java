import java.util.Arrays;
import suffix.KasaiLcp;
import suffix.LcpResult;

public class KasaiLcpTests {
    public static void main(String[] args) {
        testBanana();
        testEmptyString();
        testInputValidation();
        System.out.println("Kasai LCP tests passed");
    }

    private static void testBanana() {
        String text = "banana";
        int[] suffixArray = {5, 3, 1, 0, 4, 2};
        int[] expected = {0, 1, 3, 0, 0, 2};
        LcpResult result = KasaiLcp.build(text, suffixArray);
        check(Arrays.equals(result.getValues(), expected), "banana LCP array should match expected values");
    }

    private static void testEmptyString() {
        LcpResult result = KasaiLcp.build("", new int[0]);
        check(result.getValues().length == 0, "empty text should have an empty LCP array");
    }

    private static void testInputValidation() {
        expectInvalid("abc", new int[]{0, 1});
        expectInvalid("abc", new int[]{0, 1, 1});
        expectInvalid("abc", new int[]{0, 1, 3});
    }

    private static void expectInvalid(String text, int[] suffixArray) {
        boolean rejected = false;
        try {
            KasaiLcp.build(text, suffixArray);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "invalid suffix array should be rejected");
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
