package Part1;

import Part1.Algorithms.DeterministicSelect;
import Part1.Algorithms.OrderStatistics;
import Part1.Algorithms.RandomizedSelect;
import Part1.Algorithms.NaiveSelect;
import Part1.utils.ArrayGenerator; // Imported ArrayGenerator

import java.util.*;

/**
 * Tests Part1.Algorithms.OrderStatistics implementations against Arrays.sort.
 * Rank is 1-based: select(arr, 1) is the minimum.
 */
public class SelectTester {

    static final long SEED = 12345L;
    static final Random gen = new Random(SEED);
    static final int MAX_PRINTED_FAILURES = 8;

    static int checks, failures;

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(null, SelectTester::runAll, "tester", 1L << 29);
        t.start();
        t.join();
    }

    static void runAll() {
        Map<String, OrderStatistics> algos = new LinkedHashMap<>();
        algos.put("Part1.Algorithms.RandomizedSelect", new RandomizedSelect());
        algos.put("MedianOfMedians", new DeterministicSelect());
        algos.put("Part1.Algorithms.NaiveSelect", new NaiveSelect());

        System.out.println("Seed = " + SEED + " (Note: ArrayGenerator uses unseeded Random)");
        for (Map.Entry<String, OrderStatistics> e : algos.entrySet()) {
            System.out.println("\n==================== " + e.getKey() + " ====================");
            checks = 0;
            failures = 0;
            OrderStatistics a = e.getValue();

            time("1. fixed hand-made cases", () -> fixedCases(a));
            time("2. median convention n=1..12", () -> medianConvention(a));
            time("3. exhaustive small sizes, all ranks", () -> exhaustiveSmall(a));
            time("4. structured inputs (sorted, reverse, organ pipe)", () -> structured(a));
            time("5. two-level recursion sizes (>25)", () -> mediumRandom(a));
            time("6. large random (1e5, 1e6)", () -> large(a));
            time("7. duplicate stress", () -> duplicateStress(a));
            time("8. extreme values (MIN/MAX int)", () -> extremeValues(a));

            System.out.println("\n" + e.getKey() + ": " + checks + " checks, " + failures + " failures"
                    + (failures == 0 ? "  -> ALL PASSED" : "  -> FAILED"));
        }
    }

    // ------------------------------------------------------------------ sections

    static void fixedCases(OrderStatistics a) {
        int[][] cases = {
                {7}, {2, 1}, {1, 4, 2, 5, 3}, {1, 4, 2, 5, 3, 0},
                {9, 8, 7, 6, 5, 4}, {10, 9, 8, 7, 6, 5, 4, 3, 2, 1},
                seq(1, 11, false), seq(1, 12, true),
                {-5, 3, -1, 0, 8, -9, 2}, {3, 1, 3, 2, 3, 3, 1, 2},
                {4, 2, 9, 1, 7, 3, 8, 6, 5, 10, 12, 11},
                fill(12, 5), fill(5, 0),
                {0, 0, 0, 0, 1}, {1, 0, 0, 0, 0},
        };
        for (int[] c : cases) {
            for (int rep = 0; rep < 20; rep++) checkAllRanks(a, c, "fixed");
        }
    }

    static void medianConvention(OrderStatistics a) {
        for (int n = 1; n <= 12; n++) {
            for (int rep = 0; rep < 50; rep++) {
                int[] arr = randomArray(n, 1000);
                int[] sorted = arr.clone();
                Arrays.sort(sorted);
                checkRank(a, arr, sorted, (n + 1) / 2, "median n=" + n);
            }
        }
    }

    static void exhaustiveSmall(OrderStatistics a) {
        int[] ranges = {2, 4, 10, 1000, Integer.MAX_VALUE};
        for (int n = 1; n <= 80; n++)
            for (int range : ranges)
                for (int rep = 0; rep < 15; rep++)
                    checkAllRanks(a, randomArray(n, range), "random n=" + n + " range=" + range);
    }

    static void structured(OrderStatistics a) {
        int[] sizes = {5, 6, 10, 11, 25, 26, 27, 100, 101, 1000, 1001};
        for (int n : sizes) {
            checkSampleRanks(a, seq(1, n, false), "sorted n=" + n);
            checkSampleRanks(a, seq(1, n, true), "reverse n=" + n);
            checkSampleRanks(a, organPipe(n), "organ pipe n=" + n);
            checkSampleRanks(a, sawtooth(n, 7), "sawtooth n=" + n);
        }
    }

    static void mediumRandom(OrderStatistics a) {
        int[] sizes = {26, 27, 30, 51, 100, 124, 125, 126, 250, 777, 1000, 5000};
        for (int n : sizes)
            for (int rep = 0; rep < 30; rep++) {
                checkSampleRanks(a, randomArray(n, 1_000_000), "random n=" + n);
                checkSampleRanks(a, shuffled(n), "permutation n=" + n);
            }
    }

    static void large(OrderStatistics a) {
        for (int n : new int[]{100_000, 1_000_000}) {
            int[] arr = randomArray(n, Integer.MAX_VALUE);
            int[] sorted = arr.clone();
            Arrays.sort(sorted);
            int[] ranks = {1, n / 4, (n + 1) / 2, 3 * n / 4, n};
            for (int r : ranks) checkRank(a, arr, sorted, Math.max(1, r), "large n=" + n);
        }
    }

    static void duplicateStress(OrderStatistics a) {
        int[] sizes = {100, 500, 20_000};
        for (int n : sizes) {
            checkSampleRanks(a, fill(n, 5), "all equal n=" + n);
            checkSampleRanks(a, randomArray(n, 2), "few distinct values n=" + n);
            checkSampleRanks(a, randomArray(n, 5), "more distinct values n=" + n);
        }
    }

    static void extremeValues(OrderStatistics a) {
        int[] arr = new int[60];
        for (int i = 0; i < arr.length; i++) {
            int pick = gen.nextInt(3);
            arr[i] = pick == 0 ? Integer.MIN_VALUE : pick == 1 ? Integer.MAX_VALUE : gen.nextInt();
        }
        for (int rep = 0; rep < 50; rep++) checkAllRanks(a, arr, "extreme values");
    }

    // ------------------------------------------------------------------ checking

    static void checkAllRanks(OrderStatistics a, int[] arr, String label) {
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        for (int r = 1; r <= arr.length; r++) checkRank(a, arr, sorted, r, label);
    }

    static void checkSampleRanks(OrderStatistics a, int[] arr, String label) {
        int n = arr.length;
        int[] sorted = arr.clone();
        Arrays.sort(sorted);
        Set<Integer> ranks = new TreeSet<>(Arrays.asList(1, n, (n + 1) / 2, Math.max(1, n / 3), 1 + gen.nextInt(n)));
        for (int r : ranks) checkRank(a, arr, sorted, r, label);
    }

    static void checkRank(OrderStatistics a, int[] original, int[] sorted, int rank, String label) {
        checks++;
        int expected = sorted[rank - 1];
        int got;
        try {
            got = a.select(original.clone(), rank);
        } catch (Throwable t) {
            report(label, original, rank, "threw " + t);
            return;
        }
        if (got != expected) report(label, original, rank, "expected " + expected + " but got " + got);
    }

    static void report(String label, int[] arr, int rank, String msg) {
        failures++;
        if (failures > MAX_PRINTED_FAILURES) return;
        String data = arr.length <= 30 ? Arrays.toString(arr) : "(length " + arr.length + ")";
        System.out.println("  FAIL [" + label + "] rank=" + rank + " " + msg + "\n       input: " + data);
        if (failures == MAX_PRINTED_FAILURES) System.out.println("  ... further failures not printed");
    }

    static void time(String name, Runnable r) {
        long start = System.nanoTime();
        int before = failures;
        r.run();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("  %-55s %6d ms  %s%n", name, ms, failures == before ? "ok" : "FAILED");
    }

    // ------------------------------------------------------------------ generators

    static int[] randomArray(int n, int range) {
        // Prevent overflow in ArrayGenerator's rand.nextInt(-bound, bound+1) when range is MAX_VALUE[cite: 5]
        if (range == Integer.MAX_VALUE) {
            return ArrayGenerator.generate(n, Integer.MAX_VALUE - 1);
        }
        // Use the existing ArrayGenerator[cite: 5]
        return ArrayGenerator.generate(n, range);
    }

    static int[] shuffled(int n) {
        int[] a = seq(1, n, false);
        for (int i = n - 1; i > 0; i--) {
            int j = gen.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
        return a;
    }

    static int[] seq(int from, int n, boolean descending) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = descending ? from + n - 1 - i : from + i;
        return a;
    }

    static int[] fill(int n, int value) {
        int[] a = new int[n];
        Arrays.fill(a, value);
        return a;
    }

    static int[] organPipe(int n) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = Math.min(i, n - 1 - i);
        return a;
    }

    static int[] sawtooth(int n, int period) {
        int[] a = new int[n];
        for (int i = 0; i < n; i++) a[i] = i % period;
        return a;
    }
}