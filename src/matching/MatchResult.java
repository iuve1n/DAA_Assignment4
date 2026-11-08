package matching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MatchResult {
    private final List<Integer> indices;
    private final long characterComparisons;

    public MatchResult(List<Integer> indices, long characterComparisons) {
        this.indices = Collections.unmodifiableList(new ArrayList<>(indices));
        this.characterComparisons = characterComparisons;
    }

    public List<Integer> getIndices() {
        return indices;
    }

    public long getCharacterComparisons() {
        return characterComparisons;
    }
}
