# Algorithms with Metrics

### Build & Test
- Build: `mvn package`
- Tests: `mvn test`

### Structure
- `src/main/java` — implementations and CLI
- `src/test/java` — JUnit 5 tests
- `results/` — CSV metrics output
- `report/` — plots and report artifacts

### Roadmap
- Metrics counters, CSV writer
- MergeSort (reuse buffer + cutoff)
- QuickSort (shuffle + smaller-first)
- Deterministic Select (MoM5)
- Closest Pair (2D D&C)
- CLI to run benches and emit CSV
