package dp;

public class EditDistance {
    public static int distance(String first, String second) {
        if (first == null || second == null) {
            throw new IllegalArgumentException("Strings cannot be null");
        }

        int[][] table = new int[first.length() + 1][second.length() + 1];
        for (int i = 0; i <= first.length(); i++) {
            table[i][0] = i;
        }
        for (int j = 0; j <= second.length(); j++) {
            table[0][j] = j;
        }

        for (int i = 1; i <= first.length(); i++) {
            for (int j = 1; j <= second.length(); j++) {
                int cost = first.charAt(i - 1) == second.charAt(j - 1) ? 0 : 1;
                int deletion = table[i - 1][j] + 1;
                int insertion = table[i][j - 1] + 1;
                int substitution = table[i - 1][j - 1] + cost;
                table[i][j] = Math.min(deletion, Math.min(insertion, substitution));
            }
        }
        return table[first.length()][second.length()];
    }
}
