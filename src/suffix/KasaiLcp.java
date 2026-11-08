package suffix;

public class KasaiLcp {
    public static LcpResult build(String text, int[] suffixArray) {
        if (text == null || suffixArray == null) {
            throw new IllegalArgumentException("Text and suffix array cannot be null");
        }
        if (suffixArray.length != text.length()) {
            throw new IllegalArgumentException("Suffix array length must match text length");
        }

        int length = text.length();
        int[] rank = new int[length];
        boolean[] seen = new boolean[length];
        for (int position = 0; position < suffixArray.length; position++) {
            int suffixStart = suffixArray[position];
            if (suffixStart < 0 || suffixStart >= length || seen[suffixStart]) {
                throw new IllegalArgumentException("Suffix array must contain each suffix index once");
            }
            seen[suffixStart] = true;
            rank[suffixStart] = position;
        }

        int[] lcp = new int[length];
        int commonLength = 0;
        long comparisons = 0;
        for (int start = 0; start < length; start++) {
            int order = rank[start];
            if (order == 0) {
                commonLength = 0;
                continue;
            }

            int previousStart = suffixArray[order - 1];
            while (start + commonLength < length && previousStart + commonLength < length) {
                comparisons++;
                if (text.charAt(start + commonLength) != text.charAt(previousStart + commonLength)) {
                    break;
                }
                commonLength++;
            }
            lcp[order] = commonLength;
            if (commonLength > 0) {
                commonLength--;
            }
        }
        return new LcpResult(lcp, comparisons);
    }
}
