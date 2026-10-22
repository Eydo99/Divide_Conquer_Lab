# Divide and Conquer Lab

CSE321 Analysis and Design of Algorithms, Fall 2026 · Assignment 1 · Eyad (ID 23010312)

Two problems solved in Java with divide-and-conquer techniques:

1. **Median selection.** Randomized select, deterministic select (median of medians) and sort-and-index are implemented and compared experimentally.
2. **Minimum-perimeter triangle.** Given up to 10⁵ points, find the triangle (collinear triples allowed) with the smallest perimeter in O(n log n) time.

The full write-up (approach, analysis, complexity, code) is in the report, `Report.tex`.

## Project structure

```text
.
├── .github/workflows/check.yml     # "Check my submission" workflow (GitHub Actions)
├── src/
│   ├── Part1/                      # Problem 1
│   │   ├── Algorithms/
│   │   │   ├── OrderStatistics.java       # interface: int select(int[] arr, int i)
│   │   │   ├── RandomizedSelect.java      # randomized divide and conquer [CLRS 9.2]
│   │   │   ├── DeterministicSelect.java   # median of medians [CLRS 9.3]
│   │   │   └── NaiveSelect.java           # Arrays.sort + index
│   │   ├── utils/
│   │   │   ├── ArrayGenerator.java        # random test arrays
│   │   │   └── Util.java                  # swap, three-way partition
│   │   ├── MedianBenchmark.java           # timing experiment, writes median_benchmark.csv
│   │   └── SelectTester.java              # correctness tests against Arrays.sort
│   └── part2/                      # Problem 2
│       ├── Main.java                      # reads test cases from stdin, prints answers
│       ├── MinTrianglePerimeter.java      # the divide-and-conquer algorithm
│       ├── MinTriangleTester.java         # correctness tests (brute force, closed forms)
│       └── test                           # sample input file with 19 test cases
├── answer.txt                      # expected output for src/part2/test
├── median_benchmark.csv            # benchmark results used in the report
├── submissions/
│   └── 23010312.jar                # Problem 2 jar, checked by the workflow
├── Report.tex                      # report for both problems (needs the two .png files)
├── part1_time_vs_n.png             # figure used in the report
└── part1_time_per_element.png      # figure used in the report
```

## Requirements

- **JDK 21 or newer** (Problem 1 uses `Math.clamp`). Developed with OpenJDK 23.0.2.
- No external libraries.
- A LaTeX distribution (or Overleaf) only if you want to build the report.

## Problem 1: median selection

**Methods.** All three implement `OrderStatistics.select(arr, i)` (1-based rank; the median is rank ⌈n/2⌉).

| Method | Time |
| --- | --- |
| Randomized select | O(n) expected, O(n²) worst case |
| Median of medians | O(n) worst case, larger constant |
| Sort and index | O(n log n) |

Both selection algorithms use a three-way partition, so duplicates are handled safely.

**Build and run** (from the `src` folder; PowerShell or any shell):

```powershell
javac -d . -sourcepath . Part1\MedianBenchmark.java Part1\SelectTester.java

java Part1.SelectTester       # correctness tests, expect "ALL PASSED" for each method
java Part1.MedianBenchmark    # benchmark, writes median_benchmark.csv
```

`MedianBenchmark` asks for the number of array sizes (`0` = the default 13 sizes from 10³ to 10⁷), then the value bound (`0` = default ±10⁹). Each point is the average of 5–100 runs on fresh random arrays, and all three methods get identical copies.

**Results** (average ms per call, random arrays; full table in the report):

| n | Randomized select | Median of medians | Sort and index |
| --- | --- | --- | --- |
| 1,000 | 0.02574 | 0.06815 | 0.06415 |
| 100,000 | 1.938 | 6.183 | 10.86 |
| 1,000,000 | 21.69 | 66.81 | 144.3 |
| 10,000,000 | 187.9 | 594.3 | 1633 |

Randomized select is fastest at every measured size (about 3× faster than median of medians and up to 8.7× faster than sorting at n = 10⁷). Its time per element and that of median of medians stay flat (linear time), while sorting's time per element keeps growing (n log n).

## Problem 2: minimum-perimeter triangle

**Idea.** Presort the points by x and by y, then split by x-rank, solve both halves recursively and let δ be the better result. A triangle that beats δ must have points on both sides of the dividing line and lie within a strip of width δ. For each point of the strip only the points less than δ/2 above it (its *window*) need to be examined, and a packing argument shows that a window never holds more than 15 points.

**Complexity.** T(n) = 2T(n/2) + O(n) gives **O(n log n)** time and O(n) space, compared with O(n³) for checking all triples.

**Input / output.** Test cases are read from standard input until end of file: `n` on one line, then `n` lines `x y`. One line per test case is printed with exactly three decimals.

**Build and run** (from the `src` folder):

```powershell
javac -d . -sourcepath . part2\Main.java part2\MinTriangleTester.java

# run on the provided test file and compare with the expected answers (prints nothing if identical)
Compare-Object (Get-Content ..\answer.txt) (Get-Content part2\test | java part2.Main)

# correctness tests, expect "30363 checks, 0 failures -> ALL PASSED"
java part2.MinTriangleTester
```

In `cmd.exe` or a Unix shell the program can also be fed with `java part2.Main < part2\test`.

## Building the jars

The assignment asks for one jar per problem, each containing the source files. From the `src` folder:

```powershell
# Problem 1  (main class: Part1.MedianBenchmark)
javac -d . -sourcepath . Part1\MedianBenchmark.java Part1\SelectTester.java
jar cfe ..\Part1.jar Part1.MedianBenchmark Part1

# Problem 2  (main class: part2.Main)
javac -d . part2\Main.java part2\MinTrianglePerimeter.java
jar cfe ..\submissions\23010312.jar part2.Main part2\Main.class part2\MinTrianglePerimeter.class part2\Main.java part2\MinTrianglePerimeter.java
```

Check a jar with `jar tf <file>.jar`, and run Problem 2 with `Get-Content part2\test | java -jar ..\submissions\23010312.jar`.

## Automated check (GitHub Actions)

`.github/workflows/check.yml` defines the **Check my submission** workflow for Problem 2. After pushing, open the repository on GitHub, go to **Actions → Check my submission → Run workflow**, keep the branch and enter `submissions/23010312.jar` as the jar path (it may be left empty when `submissions/` contains exactly one jar). Keep only the Problem 2 jar in `submissions/`.

## Notes

- `.gitignore` excludes `*.class` and `*.jar`. To commit the submission jar, add the exceptions `!submissions/` and `!submissions/*.jar` to `.gitignore`, or use `git add -f submissions/23010312.jar`.
- `ArrayGenerator` is deliberately unseeded, so benchmark inputs differ from run to run; `SelectTester` and `MinTriangleTester` use fixed seeds (12345 and 2026) for their sampling.
- **Building the report:** put `Report.tex` and the two `.png` files in one folder and run `pdflatex Report.tex` twice.