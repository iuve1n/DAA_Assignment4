package benchmark;

import dp.EditDistance;
import dp.LongestCommonSubsequence;
import matching.KmpMatcher;
import matching.MatchResult;
import matching.NaiveMatcher;
import matching.RabinKarpMatcher;
import suffix.KasaiLcp;
import suffix.LcpResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class Benchmark {
    private static final int[] DP_SIZES = {100, 500, 1000};
    private static final int[] MATCH_TEXT_SIZES = {10_000, 50_000, 100_000};
    private static final int[] KASAI_SIZES = {1_000, 5_000, 10_000};
    private static final int PATTERN_LENGTH = 12;
    private static final int WARMUP_RUNS = 5;
    private static final int MEASURED_RUNS = 5;
    private static final long SEED = 4_202_611_08L;
    private static final String ALPHABET = "ACGT";
    private static volatile long resultSink;

    private static class BenchmarkRow {
        private final String task;
        private final String testCase;
        private final int inputSize;
        private final String algorithm;
        private final double averageTimeNanos;
        private final String operation;
        private final double averageOperationCount;
        private final int matchCount;

        private BenchmarkRow(String task, String testCase, int inputSize, String algorithm,
                             double averageTimeNanos, String operation,
                             double averageOperationCount, int matchCount) {
            this.task = task;
            this.testCase = testCase;
            this.inputSize = inputSize;
            this.algorithm = algorithm;
            this.averageTimeNanos = averageTimeNanos;
            this.operation = operation;
            this.averageOperationCount = averageOperationCount;
            this.matchCount = matchCount;
        }
    }

    private static class TimedResult<T> {
        private final double averageNanos;
        private final double averageOperations;
        private final T result;

        private TimedResult(double averageNanos, double averageOperations, T result) {
            this.averageNanos = averageNanos;
            this.averageOperations = averageOperations;
            this.result = result;
        }
    }

    private interface IntOperation {
        int run();
    }

    private interface MatchOperation {
        MatchResult run();
    }

    private interface LcpOperation {
        LcpResult run();
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("results/tables/benchmark.csv") : Path.of(args[0]);
        List<BenchmarkRow> rows = new ArrayList<>();
        runDynamicProgrammingExperiments(rows);
        runStringMatchingExperiments(rows);
        runKasaiExperiments(rows);
        writeResults(output, rows);
        System.out.println("Benchmark results saved to " + output);
    }

    private static void runDynamicProgrammingExperiments(List<BenchmarkRow> rows) {
        for (int size : DP_SIZES) {
            Random random = new Random(SEED + size);
            String first = randomString(size, random);
            String second = randomString(size, random);

            TimedResult<Integer> lcs = timeIntOperation(
                    () -> LongestCommonSubsequence.length(first, second), size * (double) size);
            rows.add(new BenchmarkRow("dynamic_programming", "random", size, "LCS",
                    lcs.averageNanos, "dp_cells", lcs.averageOperations, -1));

            TimedResult<Integer> edit = timeIntOperation(
                    () -> EditDistance.distance(first, second), size * (double) size);
            rows.add(new BenchmarkRow("dynamic_programming", "random", size, "EditDistance",
                    edit.averageNanos, "dp_cells", edit.averageOperations, -1));
        }
    }

    private static void runStringMatchingExperiments(List<BenchmarkRow> rows) {
        for (int size : MATCH_TEXT_SIZES) {
            String text = randomString(size, new Random(SEED + 10_000L + size));
            int presentStart = new Random(SEED + size).nextInt(size - PATTERN_LENGTH + 1);
            String presentPattern = text.substring(presentStart, presentStart + PATTERN_LENGTH);
            String absentPattern = makeAbsentPattern(text, new Random(SEED + 20_000L + size));
            runMatchingCase(rows, text, presentPattern, "present", size);
            runMatchingCase(rows, text, absentPattern, "absent", size);
        }
    }

    private static void runMatchingCase(List<BenchmarkRow> rows, String text, String pattern,
                                        String testCase, int size) {
        MatchResult naiveCheck = NaiveMatcher.search(text, pattern);
        MatchResult kmpCheck = KmpMatcher.search(text, pattern);
        MatchResult rabinKarpCheck = RabinKarpMatcher.search(text, pattern);
        if (!naiveCheck.getIndices().equals(kmpCheck.getIndices())
                || !naiveCheck.getIndices().equals(rabinKarpCheck.getIndices())) {
            throw new IllegalStateException("String matching algorithms returned different indices");
        }

        addMatchRow(rows, "Naive", testCase, size,
                timeMatchOperation(() -> NaiveMatcher.search(text, pattern)));
        addMatchRow(rows, "KMP", testCase, size,
                timeMatchOperation(() -> KmpMatcher.search(text, pattern)));
        addMatchRow(rows, "RabinKarp", testCase, size,
                timeMatchOperation(() -> RabinKarpMatcher.search(text, pattern)));
    }

    private static void addMatchRow(List<BenchmarkRow> rows, String algorithm,
                                    String testCase, int size, TimedResult<MatchResult> result) {
        rows.add(new BenchmarkRow("string_matching", testCase, size, algorithm,
                result.averageNanos, "character_comparisons", result.averageOperations,
                result.result.getIndices().size()));
    }

    private static void runKasaiExperiments(List<BenchmarkRow> rows) {
        for (int size : KASAI_SIZES) {
            String text = randomString(size, new Random(SEED + 30_000L + size));
            int[] suffixArray = buildSuffixArray(text);
            TimedResult<LcpResult> result = timeLcpOperation(
                    () -> KasaiLcp.build(text, suffixArray));
            rows.add(new BenchmarkRow("kasai", "random", size, "Kasai",
                    result.averageNanos, "character_comparisons", result.averageOperations, -1));
        }
    }

    private static TimedResult<Integer> timeIntOperation(IntOperation operation, double operationCount) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            resultSink += operation.run();
        }
        long totalNanos = 0;
        int result = 0;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long start = System.nanoTime();
            result = operation.run();
            totalNanos += System.nanoTime() - start;
            resultSink += result;
        }
        return new TimedResult<>(totalNanos / (double) MEASURED_RUNS, operationCount, result);
    }

    private static TimedResult<MatchResult> timeMatchOperation(MatchOperation operation) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            resultSink += operation.run().getIndices().size();
        }
        long totalNanos = 0;
        long totalComparisons = 0;
        MatchResult result = null;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long start = System.nanoTime();
            result = operation.run();
            totalNanos += System.nanoTime() - start;
            totalComparisons += result.getCharacterComparisons();
            resultSink += result.getIndices().size();
        }
        return new TimedResult<>(totalNanos / (double) MEASURED_RUNS,
                totalComparisons / (double) MEASURED_RUNS, result);
    }

    private static TimedResult<LcpResult> timeLcpOperation(LcpOperation operation) {
        for (int i = 0; i < WARMUP_RUNS; i++) {
            resultSink += operation.run().getValues().length;
        }
        long totalNanos = 0;
        long totalComparisons = 0;
        LcpResult result = null;
        for (int i = 0; i < MEASURED_RUNS; i++) {
            long start = System.nanoTime();
            result = operation.run();
            totalNanos += System.nanoTime() - start;
            totalComparisons += result.getCharacterComparisons();
            resultSink += result.getValues().length;
        }
        return new TimedResult<>(totalNanos / (double) MEASURED_RUNS,
                totalComparisons / (double) MEASURED_RUNS, result);
    }

    private static String makeAbsentPattern(String text, Random random) {
        String pattern;
        do {
            pattern = randomString(PATTERN_LENGTH, random);
        } while (text.indexOf(pattern) >= 0);
        return pattern;
    }

    private static String randomString(int length, Random random) {
        StringBuilder value = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            value.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return value.toString();
    }

    private static int[] buildSuffixArray(String text) {
        Integer[] suffixes = new Integer[text.length()];
        for (int i = 0; i < suffixes.length; i++) {
            suffixes[i] = i;
        }
        Arrays.sort(suffixes, (first, second) -> compareSuffixes(text, first, second));
        int[] result = new int[suffixes.length];
        for (int i = 0; i < suffixes.length; i++) {
            result[i] = suffixes[i];
        }
        return result;
    }

    private static int compareSuffixes(String text, int first, int second) {
        int firstIndex = first;
        int secondIndex = second;
        while (firstIndex < text.length() && secondIndex < text.length()) {
            int comparison = Character.compare(text.charAt(firstIndex), text.charAt(secondIndex));
            if (comparison != 0) {
                return comparison;
            }
            firstIndex++;
            secondIndex++;
        }
        if (firstIndex == text.length() && secondIndex == text.length()) {
            return 0;
        }
        return firstIndex == text.length() ? -1 : 1;
    }

    private static void writeResults(Path output, List<BenchmarkRow> rows) throws IOException {
        Path parent = output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        List<String> lines = new ArrayList<>();
        lines.add("task,case,input_size,algorithm,average_time_ns,operation,average_operation_count,match_count");
        for (BenchmarkRow row : rows) {
            lines.add(String.format(Locale.ROOT, "%s,%s,%d,%s,%.1f,%s,%.1f,%d",
                    row.task, row.testCase, row.inputSize, row.algorithm, row.averageTimeNanos,
                    row.operation, row.averageOperationCount, row.matchCount));
        }
        Files.write(output, lines);
    }
}
