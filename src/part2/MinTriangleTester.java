package part2;

import java.util.*;

/**
 * Tests MinTrianglePerimeter.getMinTrianglePerimeter(arr_x, arr_y) against:
 *   - the assignment's sample cases (checked as 3-decimal strings),
 *   - an O(n^3) brute force on small/medium inputs,
 *   - closed-form answers on large structured inputs (collinear lines, grids).
 *
 * prepare() builds the two arrays exactly the way the solver expects them:
 *   rows are {x, y, xIndex}; arr_x is sorted by x only (ties left arbitrary on purpose),
 *   xIndex is assigned right after that sort, and arr_y holds the SAME row objects sorted by y.
 * Your real main() must prepare its input the same way.
 */
public class MinTriangleTester {

    static final long SEED = 2026L;
    static final Random gen = new Random(SEED);
    static final int MAX_PRINTED_FAILURES = 8;

    static int checks, failures;

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(null, MinTriangleTester::runAll, "tester", 1L << 28);
        t.start();
        t.join();
    }

    static void runAll() {
        System.out.println("Seed = " + SEED);
        time("1. assignment sample cases (3-decimal output)", MinTriangleTester::samples);
        time("2. hand-made cases vs brute force", MinTriangleTester::handMade);
        time("3. tiny n (3..8) many repeats vs brute force", MinTriangleTester::tinySizes);
        time("4. random small, many ranges vs brute force", MinTriangleTester::randomSmall);
        time("5. repeated x or repeated y vs brute force", MinTriangleTester::repeatedCoordinates);
        time("6. optimum crosses the dividing line vs brute", MinTriangleTester::crossingOptimum);
        time("7. medium random n=100..400 vs brute force", MinTriangleTester::mediumRandom);
        time("8. large collinear lines (closed form)", MinTriangleTester::largeCollinear);
        time("9. large grid, huge x/y duplicates (closed form)", MinTriangleTester::largeGrid);
        time("10. large random n=100000 (upper-bound check)", MinTriangleTester::largeRandom);

        System.out.println("\n" + checks + " checks, " + failures + " failures"
                + (failures == 0 ? "  -> ALL PASSED" : "  -> FAILED"));
    }

    // ------------------------------------------------------------------ sections

    static void samples() {
        int[][][] inputs = {
                {{0, 0}, {1, 0}, {2, 0}, {1, 3}},
                {{0, 0}, {3, 0}, {0, 4}, {10, 10}, {11, 10}, {10, 11}},
                {{0, 0}, {1, 0}, {3, 0}},
                {{0, 0}, {6, 0}, {0, 8}},
        };
        String[] expected = {"4.000", "3.414", "6.000", "24.000"};
        for (int i = 0; i < inputs.length; i++) {
            checks++;
            String got = fmt(solve(inputs[i]));
            if (!got.equals(expected[i])) report("sample " + (i + 1), inputs[i], "expected " + expected[i] + " but got " + got);
        }
    }

    static void handMade() {
        int[][][] cases = {
                // the case where looking only at 3 consecutive candidates would miss the best triangle
                {{-200, 50}, {-10, 1}, {-1, 0}, {1, 2}, {1, 3}, {200, 50}},
                // vertical line, y order not the same as input order
                {{5, 0}, {5, 9}, {5, 4}},
                {{5, 9}, {5, 2}, {5, 4}},
                {{5, 9}, {5, 2}, {5, 4}, {5, 100}, {5, -30}},
                // horizontal line
                {{9, 1}, {0, 1}, {4, 1}, {20, 1}},
                // all x equal, many points
                {{7, 1}, {7, 50}, {7, 3}, {7, 44}, {7, 6}, {7, 100}, {7, -8}, {7, 20}},
                // all y equal, many points
                {{1, 7}, {50, 7}, {3, 7}, {44, 7}, {6, 7}, {100, 7}, {-8, 7}, {20, 7}},
                // 2x2 and 3x3 grids
                {{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                {{0, 0}, {0, 1}, {0, 2}, {1, 0}, {1, 1}, {1, 2}, {2, 0}, {2, 1}, {2, 2}},
                // extreme coordinates
                {{-1000000, -1000000}, {1000000, 1000000}, {-1000000, 1000000}, {1000000, -1000000}},
                {{-1000000, -1000000}, {1000000, 1000000}, {0, 0}},
                // four points: both halves are too small to hold a triangle (the 2 + 2 split)
                {{0, 0}, {1, 5}, {2, 1}, {3, 4}},
                {{0, 0}, {0, 1}, {1, 0}, {1, 1}},
                // two tight clusters far apart
                {{0, 0}, {1, 0}, {0, 1}, {1000, 1000}, {1001, 1000}, {1000, 1001}},
                // negatives and a long thin shape
                {{-5, -5}, {-4, 3}, {2, -7}, {9, 9}, {-9, 9}, {0, 0}},
        };
        for (int[][] c : cases)
            for (int rep = 0; rep < 5; rep++) checkAgainstBrute("hand-made", shuffled(c));
    }

    static void tinySizes() {
        // n = 4 is the only size where both halves return "no triangle", so test it heavily.
        for (int n = 3; n <= 8; n++)
            for (int rep = 0; rep < 2000; rep++)
                checkAgainstBrute("tiny n=" + n, randomPoints(n, -20, 20, -20, 20));
    }

    static void randomSmall() {
        int[] ranges = {2, 3, 5, 10, 50, 1000, 1_000_000};
        for (int n = 3; n <= 40; n++)
            for (int r : ranges)
                for (int rep = 0; rep < 30; rep++)
                    checkAgainstBrute("random n=" + n + " range=" + r, randomPoints(n, -r, r, -r, r));
    }

    static void repeatedCoordinates() {
        for (int n = 3; n <= 60; n++)
            for (int rep = 0; rep < 40; rep++) {
                // few distinct x values, wide y: many points share a median x
                checkAgainstBrute("few x n=" + n, randomPoints(n, 0, 2, -1_000_000, 1_000_000));
                checkAgainstBrute("few x n=" + n, randomPoints(n, 0, 4, -30, 30));
                // few distinct y values, wide x
                checkAgainstBrute("few y n=" + n, randomPoints(n, -1_000_000, 1_000_000, 0, 2));
                checkAgainstBrute("few y n=" + n, randomPoints(n, -30, 30, 0, 4));
            }
    }

    static void crossingOptimum() {
        // Far-away points spread to both sides, plus a tight triangle around x = 0,
        // so the dividing line cuts through the best triangle.
        for (int n = 7; n <= 200; n += 3)
            for (int rep = 0; rep < 15; rep++) {
                int far = n - 3;
                List<int[]> pts = new ArrayList<>();
                Set<Long> seen = new HashSet<>();
                int[][] tight = {{-1, 0}, {1, 0}, {0, 1}};
                for (int[] t : tight) { pts.add(t); seen.add(key(t[0], t[1])); }
                while (pts.size() < n) {
                    int side = pts.size() % 2 == 0 ? 1 : -1;
                    int x = side * (1000 + gen.nextInt(900_000));
                    int y = -1_000_000 + gen.nextInt(2_000_001);
                    if (seen.add(key(x, y))) pts.add(new int[]{x, y});
                }
                checkAgainstBrute("crossing n=" + n, shuffled(pts.toArray(new int[0][])));
            }
    }

    static void mediumRandom() {
        int[] sizes = {100, 150, 200, 300, 400};
        int[] ranges = {10, 100, 1_000_000};
        for (int n : sizes)
            for (int r : ranges)
                for (int rep = 0; rep < 3; rep++)
                    checkAgainstBrute("medium n=" + n + " range=" + r, randomPoints(n, -r, r, -r, r));
    }

    static void largeCollinear() {
        int n = 100_000;
        // Closed form: on a line, best = 2 * (smallest span of three consecutive points).
        int[] coords = distinctSorted(n, 10);
        long minSpan = Long.MAX_VALUE;
        for (int i = 0; i + 2 < n; i++) minSpan = Math.min(minSpan, (long) coords[i + 2] - coords[i]);
        double expected = 2.0 * minSpan;

        int[][] vertical = new int[n][];
        int[][] horizontal = new int[n][];
        for (int i = 0; i < n; i++) {
            vertical[i] = new int[]{42, coords[i]};
            horizontal[i] = new int[]{coords[i], -7};
        }
        checkValue("large vertical line", shuffled(vertical), expected);
        checkValue("large horizontal line", shuffled(horizontal), expected);
    }

    static void largeGrid() {
        // 300 x 300 grid: every x and every y value repeats 300 times.
        // The best triangle is a unit right triangle: 1 + 1 + sqrt(2).
        int side = 300;
        int[][] pts = new int[side * side][];
        int k = 0;
        for (int x = 0; x < side; x++)
            for (int y = 0; y < side; y++)
                pts[k++] = new int[]{x * 3 - 400, y * 3 - 400};   // spacing 3 -> expected 3 * (2 + sqrt 2)
        checkValue("large grid", shuffled(pts), 3 * (2 + Math.sqrt(2)));
    }

    static void largeRandom() {
        int n = 100_000;
        int[][] pts = randomPoints(n, -1_000_000, 1_000_000, -1_000_000, 1_000_000);
        checks++;
        double got;
        try {
            got = solve(pts);
        } catch (Throwable t) {
            report("large random", pts, "threw " + t);
            return;
        }
        // No brute force is possible here. A sampled minimum is only an UPPER bound on the answer,
        // so this catches a result that is too big (a missed triangle) but not one that is too small.
        double sampled = Double.POSITIVE_INFINITY;
        for (int s = 0; s < 3_000_000; s++) {
            int a = gen.nextInt(n), b = gen.nextInt(n), c = gen.nextInt(n);
            if (a == b || b == c || a == c) continue;
            sampled = Math.min(sampled, perimeter(pts[a], pts[b], pts[c]));
        }
        if (!(got <= sampled + 1e-6) || Double.isInfinite(got) || got <= 0)
            report("large random", pts, "got " + got + " but a sampled triple has perimeter " + sampled);
    }

    // ------------------------------------------------------------------ checking

    static void checkAgainstBrute(String label, int[][] pts) {
        if (pts.length < 3) return;
        checkValue(label, pts, brute(pts));
    }

    static void checkValue(String label, int[][] pts, double expected) {
        checks++;
        double got;
        try {
            got = solve(pts);
        } catch (Throwable t) {
            report(label, pts, "threw " + t);
            return;
        }
        if (Double.isNaN(got) || Math.abs(got - expected) > 1e-6 * Math.max(1.0, expected))
            report(label, pts, "expected " + expected + " but got " + got);
    }

    static double solve(int[][] pts) {
        double[][][] prepared = prepare(pts);
        return MinTrianglePerimeter.getMinTrianglePerimeter(prepared[0], prepared[1]);
    }

    /** Rows {x, y, xIndex}. arr_x sorted by x only; arr_y holds the same row objects sorted by y. */
    static double[][][] prepare(int[][] pts) {
        int n = pts.length;
        double[][] arrX = new double[n][];
        for (int i = 0; i < n; i++) arrX[i] = new double[]{pts[i][0], pts[i][1], 0};
        Arrays.sort(arrX, (a, b) -> Double.compare(a[0], b[0]));
        for (int i = 0; i < n; i++) arrX[i][2] = i;
        double[][] arrY = arrX.clone();                    // shallow: same row objects
        Arrays.sort(arrY, (a, b) -> Double.compare(a[1], b[1]));
        return new double[][][]{arrX, arrY};
    }

    static double brute(int[][] p) {
        double best = Double.POSITIVE_INFINITY;
        int n = p.length;
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j < n; j++)
                for (int k = j + 1; k < n; k++)
                    best = Math.min(best, perimeter(p[i], p[j], p[k]));
        return best;
    }

    static double perimeter(int[] a, int[] b, int[] c) {
        return dist(a, b) + dist(b, c) + dist(c, a);
    }

    static double dist(int[] a, int[] b) {
        long dx = (long) a[0] - b[0], dy = (long) a[1] - b[1];
        return Math.sqrt((double) (dx * dx + dy * dy));
    }

    static String fmt(double v) {
        return String.format(Locale.US, "%.3f", v);   // Locale.US: avoids a decimal comma on some systems
    }

    static void report(String label, int[][] pts, String msg) {
        failures++;
        if (failures > MAX_PRINTED_FAILURES) return;
        String data = pts.length <= 12 ? Arrays.deepToString(pts) : "(n = " + pts.length + ")";
        System.out.println("  FAIL [" + label + "] " + msg + "\n       input: " + data);
        if (failures == MAX_PRINTED_FAILURES) System.out.println("  ... further failures not printed");
    }

    static void time(String name, Runnable r) {
        long start = System.nanoTime();
        int before = failures;
        r.run();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.printf("  %-52s %7d ms  %s%n", name, ms, failures == before ? "ok" : "FAILED");
    }

    // ------------------------------------------------------------------ generators

    static long key(int x, int y) {
        return (long) (x + 1_000_000) * 2_000_001L + (y + 1_000_000);
    }

    /** n distinct points with coordinates inside the given inclusive ranges (n is capped if the box is too small). */
    static int[][] randomPoints(int n, int xLo, int xHi, int yLo, int yHi) {
        long cells = (long) (xHi - xLo + 1) * (yHi - yLo + 1);
        n = (int) Math.min(n, cells);
        Set<Long> seen = new HashSet<>();
        int[][] p = new int[n][];
        int k = 0;
        while (k < n) {
            int x = xLo + gen.nextInt(xHi - xLo + 1);
            int y = yLo + gen.nextInt(yHi - yLo + 1);
            if (seen.add(key(x, y))) p[k++] = new int[]{x, y};
        }
        return p;
    }

    static int[][] shuffled(int[][] pts) {
        int[][] p = pts.clone();
        for (int i = p.length - 1; i > 0; i--) {
            int j = gen.nextInt(i + 1);
            int[] t = p[i]; p[i] = p[j]; p[j] = t;
        }
        return p;
    }

    /** n strictly increasing coordinates with random gaps in [1, maxGap]. */
    static int[] distinctSorted(int n, int maxGap) {
        int[] c = new int[n];
        int v = -500_000;
        for (int i = 0; i < n; i++) {
            v += 1 + gen.nextInt(maxGap);
            c[i] = v;
        }
        return c;
    }
}