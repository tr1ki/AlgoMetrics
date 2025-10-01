Divide-and-Conquer Algorithms (Assignment 1)
Course: Design and Analysis of Algorithms • Assignment: 1
Author: Balakarev Sergej (SE-2404)

Overview
This project implements four classic divide-and-conquer algorithms and validates their theoretical running times on the JVM. The implementation emphasizes bounded recursion depth, low allocations, and lightweight instrumentation to collect time, recursion depth, comparisons, moves, and allocations.

Algorithms
MergeSort: Θ(n log n). Linear merge, single reusable buffer, small-n cut-off (insertion sort).
QuickSort (randomized): expected Θ(n log n). Recurse on the smaller side, iterate on the larger side (bounded stack).
Deterministic Select (Median-of-Medians, groups of 5): Θ(n). In-place partition, MoM5 pivot.
Closest Pair of Points (2D): Θ(n log n). Maintain y-order and scan a “strip” with constant neighbors.
Architecture notes
Stack safety: JVM has no guaranteed tail-call optimization. QuickSort recurses only on the smaller side; MergeSort and Closest Pair are balanced (depth ≈ log2 n). Small-n cut-off further reduces practical depth.
Allocation control: MergeSort uses one reusable int[] buffer (ALLOC ≈ 1 per run). Closest Pair allocates two clones plus one merge buffer (ALLOC ≈ 3). QuickSort and Select are in-place (ALLOC ≈ 0).
Instrumentation: We record time_ns, max_depth, cmp (key comparisons), mov (data moves; swaps counted as 3; MergeSort also counts buffer copies), alloc (major arrays per run).
Recurrence analysis (theory)
MergeSort: T(n) = 2T(n/2) + cn. By the Master Theorem (Case 2), per-level work is balanced and the number of levels is logarithmic, so T(n) = Θ(n log n). With a cut-off c0, effective depth ≈ log2(n/c0).
QuickSort (randomized, smaller-first): E[T(n)] = E[T(k)] + E[T(n−1−k)] + cn with k roughly uniform. This yields E[T(n)] = Θ(n log n). Recursing only into the smaller side bounds the stack to O(log n) regardless of pivots on a particular run.
Deterministic Select (MoM5): T(n) ≤ T(n/5) + T(7n/10) + cn. In the Akra–Bazzi framework, the balancing exponent p is near 1; comparing g(n)=Θ(n) to n^p gives T(n) = Θ(n). We partition in place and recurse only into the needed side.
Closest Pair (2D): T(n) = 2T(n/2) + cn. The strip check is linear (each point compared to a constant number of neighbors), so by the Master Theorem (Case 2) T(n) = Θ(n log n).
Metrics
Logged per run into bench.csv:

algo, n, time_ns, max_depth, cmp, mov, alloc.
Comparisons and moves are counted in core steps (swaps as 3 moves; MergeSort includes buffer copies by design; Closest Pair counts distance comparisons in the strip, not sort comparator costs).
How to run
Demo checks (prints basic correctness):
java Main
Benchmarks (writes bench.csv):
java Main bench
Measurements (summary from bench.csv)
MergeSort: time ~ n log n; depth grows ~ log2 n (e.g., 7 → 17 for n=2^10..2^20 with cut-off=24). cmp ≈ 1.0·n·log2 n; mov ≈ 1.8·n·log2 n due to buffer copies.
QuickSort: expected time ~ n log n; max_depth grows logarithmically (≈ 6..13 for n=2^10..2^20). cmp ≈ 1.2·n·log2 n; in-place (alloc≈0).
Select (MoM5): time per element (time_ns/n) is roughly constant across sizes, confirming Θ(n). cmp and mov scale linearly with n.
Closest Pair: time ~ n log n with larger constants (sorting and object comparisons). cmp in the strip is Θ(n); alloc = 3 (px, py, buf).
Plots to include
MergeSort/QuickSort/Closest: time vs n and time/(n·log2 n) — should flatten to a plateau.
Depth (MergeSort/QuickSort/Closest): max_depth vs log2 n — approximately linear.
Select: time/n vs n — roughly flat.
Notes and limitations
We focus on core D&C steps in cmp/mov: MergeSort counts buffer copies by design; Closest Pair does not include comparator work from initial sorts in cmp.
Constants matter on the JVM: cut-offs, buffer reuse, and in-place operations reduce allocation and GC overhead; randomized pivots stabilize QuickSort’s performance.
Testing
Sorting (Merge/Quick): compare with Arrays.sort on random and adversarial inputs (duplicates, sorted, reverse).
QuickSort stack bound: for random inputs and smaller-first recursion, max_depth ≲ O(log2 n); verify on powers of two.
Select (MoM5): 100 random trials vs Arrays.sort(a)[k].
Closest Pair: validate against O(n^2) brute force for n ≤ 2000; use only the fast algorithm for larger n.
GitHub workflow
Branches: main (only releases: tag v0.1, v1.0); feature/mergesort, feature/quicksort, feature/select, feature/closest, feature/metrics (and optional feature/cli).
Commit storyline:
init: maven, junit5, readme
feat(metrics): counters, depth tracker, CSV writer
feat(mergesort): baseline + reuse buffer + cutoff + tests
feat(quicksort): smaller-first recursion, randomized pivot + tests
refactor(util): partition, swap, shuffle, guards
feat(select): deterministic select (MoM5) + tests
feat(closest): divide-and-conquer implementation + tests
feat(cli): parse args, run algos, emit CSV
bench(jmh): harness for select vs sort (optional)
docs(report): Master cases & Akra–Bazzi; plots
fix: edge cases (duplicates, tiny arrays)
release: v1.0
Summary
Theory and measurements align:

MergeSort and Closest Pair: Θ(n log n) with logarithmic depth; normalized time is near-constant.
QuickSort: expected Θ(n log n); depth bounded by O(log n) due to smaller-first recursion.
Deterministic Select (MoM5): Θ(n); time per element stabilizes with n, although constants are higher than “sort then pick k”.