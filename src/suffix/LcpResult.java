package suffix;

public class LcpResult {
    private final int[] values;
    private final long characterComparisons;

    public LcpResult(int[] values, long characterComparisons) {
        this.values = values.clone();
        this.characterComparisons = characterComparisons;
    }

    public int[] getValues() {
        return values.clone();
    }

    public long getCharacterComparisons() {
        return characterComparisons;
    }
}
