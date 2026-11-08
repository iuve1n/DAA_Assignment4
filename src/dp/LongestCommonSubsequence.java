package dp;

public class LongestCommonSubsequence {
    public static int length(String first, String second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Strings cannot be null");
        }

        int[][] table = new int[first.length() + 1][second.length() + 1];
        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    table[i][j] = table[i - 1][j - 1] + 1;
                } else {
                    table[i][j] = Math.max(table[i - 1][j], table[i][j - 1]);
                }
            }
        }
        return table[first.length()][second.length()];
    }
}
