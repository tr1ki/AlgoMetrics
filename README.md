## Divide-and-Conquer Algorithms (Assignment 1)
Course: Design and Analysis of Algorithms • Assignment: 1  
Author: Balakarev Sergej (SE-2404)

### Overview
Four classic divide-and-conquer algorithms on the JVM with safe recursion, low allocations, and lightweight metrics. Theory (Master Theorem, Akra–Bazzi) is validated with measurements.

### Algorithms
- MergeSort — Θ(n log n): linear merge, single reusable buffer, small-n cut-off (insertion sort).
- QuickSort (randomized) — expected Θ(n log n): recurse on smaller side, iterate larger (bounded stack).
- Select (Median-of-Medians, 5) — Θ(n): in-place partition, MoM5 pivot, recurse only where needed.
- Closest Pair (2D) — Θ(n log n): sort by x, maintain y-order, “strip” with constant neighbors.

### Architecture
- Stack safety: JVM has no guaranteed TCO. Smaller-first QuickSort keeps depth ≈ O(log n); MergeSort/Closest are balanced (≈ log2 n). Cut-off reduces depth.
- Allocation control: MergeSort ALLOC=1 (buffer); Closest ALLOC=3 (px, py, buf); QuickSort/Select in-place (ALLOC≈0).

### Metrics (bench.csv)
- Columns: algo, n, time_ns, max_depth, cmp, mov, alloc.
- Notes: swaps = 3 moves; MergeSort mov includes buffer copies; Closest cmp counts only strip comparisons (not initial sorts).

### How to run
- CLI (fat-jar via Maven):
    - Build: `mvn -q -DskipTests package`
    - Run demo: `java -jar target/algorithms-1.0-SNAPSHOT.jar`
    - Run bench csv: `java -jar target/algorithms-1.0-SNAPSHOT.jar bench`
- CLI flags:
    - `--algo mergesort|quicksort|select|closest|demo` (default: demo)
    - `--n <size>` размер входа (default: 65536)
    - `--trials <t>` число прогонов (default: 1)
    - `--seed <s>` начальное зерно (default: 42)
    - `--csv <path>` путь для вывода CSV (если не указан — печать в stdout)
  Примеры:
    - `java -jar target/algorithms-1.0-SNAPSHOT.jar --algo mergesort --n 1048576 --trials 3 --csv out.csv`
    - `java -jar target/algorithms-1.0-SNAPSHOT.jar --algo closest --n 131072`

### Testing & Benchmarks
- Tests (JUnit5): `mvn -q test`
- JMH (Select vs sort-then-pick):
    - `mvn -q -DskipTests package && java -cp target/algorithms-1.0-SNAPSHOT.jar org.openjdk.jmh.Main com.university.algorithms.bench.SelectBench`
    - или запустить `SelectBench.main`

### Plots
1) Depth vs log2 n  
   ![depth](plots/depth.png)  
   MergeSort/Closest: depth ≈ log2 n. QuickSort: smaller constants (~6→13 for 2^10..2^20).

2) Normalized time (n log n algorithms)  
   ![nlogn](plots/nlogn_norm.png)  
   MergeSort/QuickSort: time/(n·log2 n) → plateau (Θ(n log n)). Closest stabilizes with higher constants.

3) Select: linear behavior  
   ![select](plots/select_norm.png)  
   time/n ≈ constant (Θ(n)); MoM5 has higher constants than “sort-then-pick”.

### Theory → Data (recurrences)
- MergeSort: T(n)=2T(n/2)+cn → Master Case 2 → Θ(n log n). Matches normalized plateau; depth ~ log2 n.
- QuickSort: randomized E[T(n)]=Θ(n log n); smaller-first → stack O(log n). Matches plateau and shallow depth.
- Select (MoM5): T(n)≤T(n/5)+T(7n/10)+cn → Akra–Bazzi intuition → Θ(n). Matches time/n ≈ const.
- Closest Pair: T(n)=2T(n/2)+cn → Master Case 2 → Θ(n log n). Matches plateau; depth ~ log2 n.

- MergeSort: Два равных подзадачных вызова и линейное слияние дают Case 2 Мастера. Баланс даёт глубину ≈ ⌊log₂ n⌋; отсечка на малых n снижает константы и глубину. Буфер переиспользуется, экономя аллокации и улучшая локальность кэша.
- QuickSort: В среднем раскладке ожидание Θ(n log n); стратегия «сначала меньшая рекурсия, большая — итерация» ограничивает стек O(log n) даже при неидеальных пивотах. Рандомизация пивота нивелирует худшие входы; лишние свапы влияют на константы и ветвления.
- Select (MoM5): Медиана медиан гарантирует, что отбрасывается ≥30% элементов, рецидив доминируется линейной частью → Θ(n) по Акра–Баззи. Итеративное продвижение только в нужную сторону убирает лишнюю глубину стека; константы выше, чем у «отсортировать и взять k».
- Closest Pair: Стабильная поддержка порядка по y и ограниченный просмотр соседей в «полосе» дают линейную работу на этапе объединения. Итог Case 2 Мастера; сортировки по x/y — доминируют только на старте и не влияют на асимптотику решения.

### Constant factors (cache/GC/branches)
- MergeSort выигрывает от последовательного доступа и переиспользования буфера (меньше GC). QuickSort страдает от ветвлений и случайного доступа, но малы константы копирования. Select (MoM5) линейный, но константы повышены из‑за локальных сортировок групп и перестановок. В ClosestPair выделение массивов px/py/buf — фиксированное, основная стоимость — сортировки и вычисления гипотенузы.

### Summary
Theory and measurements align:
- MergeSort & Closest: Θ(n log n), logarithmic depth.
- QuickSort: expected Θ(n log n), bounded stack via smaller-first recursion.
- Select (MoM5): Θ(n) with larger constants.
