package com.university.algorithms.metrics;

public final class Metrics {
    public final String algorithm;
    public final int n;
    public final int runId;
    public final long timeMs;
    public final int recursionDepth;
    public final long comparisons;
    public final long allocations;

    public Metrics(String algorithm,
                   int n,
                   int runId,
                   long timeMs,
                   int recursionDepth,
                   long comparisons,
                   long allocations) {
        this.algorithm = algorithm;
        this.n = n;
        this.runId = runId;
        this.timeMs = timeMs;
        this.recursionDepth = recursionDepth;
        this.comparisons = comparisons;
        this.allocations = allocations;
    }

    public String toCsvRow() {
        return String.join(",",
                escape(algorithm),
                Integer.toString(n),
                Integer.toString(runId),
                Long.toString(timeMs),
                Integer.toString(recursionDepth),
                Long.toString(comparisons),
                Long.toString(allocations)
        );
    }

    public static String csvHeader() {
        return "algorithm,n,run_id,time_ms,recursion_depth,comparisons,allocations";
    }

    private static String escape(String value) {
        if (value == null) return "";
        boolean needsQuotes = value.contains(",") || value.contains("\"") || value.contains("\n");
        String escaped = value.replace("\"", "\"\"");
        return needsQuotes ? "\"" + escaped + "\"" : escaped;
    }
}


