package com.university.algorithms.metrics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CountersTest {
    @Test
    void depthTracksMax() {
        Counters.reset();
        assertEquals(0, Counters.getMaxDepth());

        Counters.incDepth(); // 1
        Counters.incDepth(); // 2
        Counters.incDepth(); // 3
        Counters.decDepth(); // 2
        Counters.decDepth(); // 1
        Counters.incDepth(); // 2
        assertEquals(3, Counters.getMaxDepth());
    }

    @Test
    void comparisonsAndAllocationsAccumulate() {
        Counters.reset();
        Counters.addComparisons(5);
        Counters.addAllocations(2);
        Counters.addComparisons(7);
        Counters.addAllocations(3);
        assertEquals(12, Counters.getComparisons());
        assertEquals(5, Counters.getAllocations());
    }
}


