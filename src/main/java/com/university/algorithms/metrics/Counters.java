package com.university.algorithms.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class Counters {
    private static final ThreadLocal<AtomicInteger> currentDepth = ThreadLocal.withInitial(AtomicInteger::new);
    private static final ThreadLocal<AtomicInteger> maxDepth = ThreadLocal.withInitial(AtomicInteger::new);
    private static final ThreadLocal<AtomicLong> comparisons = ThreadLocal.withInitial(AtomicLong::new);
    private static final ThreadLocal<AtomicLong> allocations = ThreadLocal.withInitial(AtomicLong::new);

    private Counters() {}

    public static void reset() {
        currentDepth.get().set(0);
        maxDepth.get().set(0);
        comparisons.get().set(0);
        allocations.get().set(0);
    }

    public static void incDepth() {
        int d = currentDepth.get().incrementAndGet();
        maxDepth.get().accumulateAndGet(d, Math::max);
    }

    public static void decDepth() {
        currentDepth.get().decrementAndGet();
    }

    public static void addComparisons(long c) {
        comparisons.get().addAndGet(c);
    }

    public static void addAllocations(long a) {
        allocations.get().addAndGet(a);
    }

    public static int getMaxDepth() {
        return maxDepth.get().get();
    }

    public static long getComparisons() {
        return comparisons.get().get();
    }

    public static long getAllocations() {
        return allocations.get().get();
    }
}


