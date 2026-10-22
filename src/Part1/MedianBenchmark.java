package Part1;

import Part1.Algorithms.DeterministicSelect;
import Part1.Algorithms.OrderStatistics;
import Part1.Algorithms.RandomizedSelect;
import Part1.Algorithms.NaiveSelect;
import Part1.utils.ArrayGenerator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;

public class MedianBenchmark {

    //default bound =1e9
    static final int DEFAULT_BOUND=1_000_000_000;

    //default array sizes to benchmark on
    static final int[] DEFAULT_SIZES={
            1_000, 2_000, 5_000,
            10_000, 20_000, 50_000,
            100_000, 200_000, 500_000,
            1_000_000, 2_000_000, 5_000_000,
            10_000_000
    };

    static volatile int sink;   // stops the JIT from discarding the results

    //clamps no of repetition w.r.t the array size to prevent very high reps for very small sizes and 0 reps for very high size
    static int repsFor(int n) {
        return Math.clamp(20_000_000 / n, 5, 100);
    }

    public static void main(String[] args) throws IOException {

        //user input can be taken for array bounds and array sizes to benchmark on if wanted
        Scanner in = new Scanner(System.in);

        //take the sizes the user want to benchmark on
        System.out.print("Enter the no of Arrays you want in benchmark (0 for default): ");
        int size = in.nextInt();
        while (size<0) {
            System.out.println("number of arrays must be 0 for default or greater.");
            System.out.print("Enter the no of Arrays you want in benchmark (0 for default): ");
            size = in.nextInt();
        }
        int[] SIZES;
        if (size==0) {
            SIZES = DEFAULT_SIZES;
        }
        else {
            SIZES=new int[size];
            for(int i =0;i<size;i++) {
                System.out.print("Enter the size of "+(i+1)+" array: ");
                SIZES[i]=in.nextInt();
                while(SIZES[i]<=0) {
                    System.out.println("size must be greater than 0");
                    System.out.print("Enter the size of"+(i+1)+"array: ");
                    SIZES[i]=in.nextInt();
                }
            }
            Arrays.sort(SIZES);
        }

        //take the bounds of the arrays that the user wants
        System.out.print("Enter bound (0 for default): ");
        long input = in.nextLong();
        while(input<0) {
            System.out.println("bound must be greater than 0");
            System.out.print("Enter bound (0 for default): ");
            input=in.nextLong();
        }
        while(input>Integer.MAX_VALUE) {
            System.out.println("bound is too big greater than Integer.MAX_VALUE");
            System.out.print("Enter bound (0 for default): ");
            input=in.nextLong();
        }
        int BOUND= (int) input;
        if (BOUND==0) {
            BOUND=DEFAULT_BOUND;
        }

        //array holding name of arrays and other holding the algorithms itself
        String[] names = {"RandomizedSelect", "MedianOfMedians", "SortSelect"};
        OrderStatistics[] algos = {new RandomizedSelect(), new DeterministicSelect(), new NaiveSelect()};

        //warmup the jvm before starting real benchmark for accurate results and timings
        System.out.println("Warming up...");
        warmUp(algos);

        //initialize the arrays and flags used during benchmark
        double[][] avgMs = new double[SIZES.length][algos.length];
        int[] repsUsed = new int[SIZES.length];
        boolean mismatch = false;

        System.out.println("Benchmarking (median = rank ceil(n/2), average time in ms per call)\n");
        System.out.printf("%12s %6s", "n", "reps");
        for (String name : names) System.out.printf(" %18s", name);
        System.out.println();

        //the following will be done for each size
        for (int s = 0; s < SIZES.length; s++) {

            //get the current array size
            int n = SIZES[s];

            //see how many reps this size is going to run according to the clamp
            int reps = repsFor(n);

            //save its reps in the reps array
            repsUsed[s] = reps;

            //get the rank of the median in the current array
            int rank = (n + 1) / 2;
            long[] totalNs = new long[algos.length];

            //run the current array size reps time according to its computed reps
            for (int rep = 0; rep < reps; rep++) {

                //each rep result in a different array
                int[] original = ArrayGenerator.generate(n, BOUND);

                //if n is too large don't save it
                if (n >= 1_000_000) System.gc();

                int first = 0;

                //process array through all the algos we have
                for (int k = 0; k < algos.length; k++) {

                    // rotate the order
                    int idx = (rep + k) % algos.length;

                    //take copy of the array to prevent manipulating the original one and ensure that all algos take
                    //the same array as input and it is outside the timer because it is external work
                    int[] copy = original.clone();

                    //compute the runtime of the algorithm running now
                    long t0 = System.nanoTime();
                    int result = algos[idx].select(copy, rank);
                    long t1 = System.nanoTime();

                    //add the runtime of the run algorithm to this previous algorithm run
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

            //print he size of array with the reps run on that array
            System.out.printf("%12d %6d", n, reps);

            //take the average if runs for each algorithm by dividing it by no of reps and then converting it to be in ms
            for (int a = 0; a < algos.length; a++) {
                avgMs[s][a] = totalNs[a] / (double) reps / 1_000_000.0;
                System.out.printf(" %18.3f", avgMs[s][a]);
            }
            System.out.println();
        }

        //write the benchmark array into a csv file
        writeCsv("median_benchmark.csv", names, avgMs, repsUsed,SIZES);
        System.out.println("\nWrote median_benchmark.csv" + (mismatch ? "   (WARNING: mismatches were reported above)" : ""));
    }

    //warmup up  for jvm before real benchmark for accurate timings and results
    static void warmUp(OrderStatistics[] algos) {

        //array sizes to warmup with,we can increase no of arrays,sizes of arrays,no of reps
        int[] warmSizes = {1_000, 100_000};

        for (int n : warmSizes) {

            //get the median rank in the current array size
            int rank = (n + 1) / 2;

            //30 reps for each size so 60 reps total
            for (int rep = 0; rep < 30; rep++) {

                //the array is generated with the default bound for consistency
                int[] original = ArrayGenerator.generate(n, DEFAULT_BOUND);

                //stored in sink which is volatile so that it is not saved
                for (OrderStatistics a : algos) sink = a.select(original.clone(), rank);
            }
        }
    }

    static void writeCsv(String file, String[] names, double[][] avgMs, int[] reps,int[] SIZES) throws IOException {
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