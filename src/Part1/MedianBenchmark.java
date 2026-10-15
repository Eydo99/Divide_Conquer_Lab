package Part1;

import Part1.Algorithms.DeterministicSelect;
import Part1.Algorithms.OrderStatistics;
import Part1.Algorithms.RandomizedSelect;
import Part1.Algorithms.NaiveSelect;
import Part1.utils.ArrayGenerator;

import java.io.IOException;
import java.io.PrintWriter;

public class MedianBenchmark {

    static final int BOUND=1_000_000_000;

    static final int[] SIZES={
            1_000, 2_000, 5_000,
            10_000, 20_000, 50_000,
            100_000, 200_000, 500_000,
            1_000_000, 2_000_000, 5_000_000,
            10_000_000
    };

    static volatile int sink;   // stops the JIT from discarding the results

    static int repsFor(int n) {
        return Math.clamp(20_000_000 / n, 5, 100);
    }

    public static void main(String[] args) throws IOException {
        String[] names = {"RandomizedSelect", "MedianOfMedians", "SortSelect"};
        OrderStatistics[] algos = {new RandomizedSelect(), new DeterministicSelect(), new NaiveSelect()};

        System.out.println("Warming up...");
        warmUp(algos);

        double[][] avgMs = new double[SIZES.length][algos.length];
        int[] repsUsed = new int[SIZES.length];
        boolean mismatch = false;

        System.out.println("Benchmarking (median = rank ceil(n/2), average time in ms per call)\n");
        System.out.printf("%12s %6s", "n", "reps");
        for (String name : names) System.out.printf(" %18s", name);
        System.out.println();

        for (int s = 0; s < SIZES.length; s++) {
            int n = SIZES[s];
            int reps = repsFor(n);
            repsUsed[s] = reps;
            int rank = (n + 1) / 2;
            long[] totalNs = new long[algos.length];

            for (int rep = 0; rep < reps; rep++) {
                int[] original = ArrayGenerator.generate(n, BOUND);
                if (n >= 1_000_000) System.gc();

                int first = 0;
                for (int k = 0; k < algos.length; k++) {
                    int idx = (rep + k) % algos.length;       // rotate the order
                    int[] copy = original.clone();            // outside the timer

                    long t0 = System.nanoTime();
                    int result = algos[idx].select(copy, rank);
                    long t1 = System.nanoTime();

                    totalNs[idx] += t1 - t0;
                    sink = result;

                    if (k == 0) {
                        first = result;
                    } else if (result != first) {
                        mismatch = true;
                        System.out.println("  !! MISMATCH at n=" + n + " rep=" + rep
                                + ": " + names[idx] + " returned " + result + " but another method returned " + first);
                    }
                }
            }

            System.out.printf("%12d %6d", n, reps);
            for (int a = 0; a < algos.length; a++) {
                avgMs[s][a] = totalNs[a] / (double) reps / 1_000_000.0;
                System.out.printf(" %18.3f", avgMs[s][a]);
            }
            System.out.println();
        }

        writeCsv("median_benchmark.csv", names, avgMs, repsUsed);
        System.out.println("\nWrote median_benchmark.csv" + (mismatch ? "   (WARNING: mismatches were reported above)" : ""));
    }

    static void warmUp(OrderStatistics[] algos) {
        int[] warmSizes = {1_000, 100_000};
        for (int n : warmSizes) {
            int rank = (n + 1) / 2;
            for (int rep = 0; rep < 30; rep++) {
                int[] original = ArrayGenerator.generate(n, BOUND);
                for (OrderStatistics a : algos) sink = a.select(original.clone(), rank);
            }
        }
    }

    static void writeCsv(String file, String[] names, double[][] avgMs, int[] reps) throws IOException {
        try (PrintWriter out = new PrintWriter(file)) {
            out.print("n,reps");
            for (String name : names) out.print("," + name + "_ms");
            out.println();
            for (int s = 0; s < SIZES.length; s++) {
                out.print(SIZES[s] + "," + reps[s]);
                for (int a = 0; a < names.length; a++) out.print("," + avgMs[s][a]);
                out.println();
            }
        }
    }
}