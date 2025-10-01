package com.university.algorithms;

import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SortingTest {

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }

    @Test
    void mergeSort_correctness_random_and_adversarial() {
        int[] sizes = {0, 1, 2, 3, 10, 100, 10_000};
        for (int n : sizes) {
            // random
            int[] r = ThreadLocalRandom.current().ints(n, -1_000_000, 1_000_000).toArray();
            Main.MergeSort.sort(r);
            assertTrue(isSorted(r));

            // sorted
            int[] s = new int[n];
            for (int i = 0; i < n; i++) s[i] = i;
            Main.MergeSort.sort(s);
            assertTrue(isSorted(s));

            // reversed
            int[] rev = new int[n];
            for (int i = 0; i < n; i++) rev[i] = n - i;
            Main.MergeSort.sort(rev);
            assertTrue(isSorted(rev));

            // duplicates
            int[] dup = new int[n];
            for (int i = 0; i < n; i++) dup[i] = (i % 5);
            Main.MergeSort.sort(dup);
            assertTrue(isSorted(dup));
        }
    }

    @Test
    void quickSort_correctness_random_and_adversarial() {
        long seed = 123456789L;
        int[] sizes = {0, 1, 2, 3, 10, 100, 10_000};
        for (int n : sizes) {
            // random
            int[] r = new Random(seed).ints(n, -1_000_000, 1_000_000).toArray();
            Main.QuickSort.sort(r, seed);
            assertTrue(isSorted(r));

            // sorted
            int[] s = new int[n];
            for (int i = 0; i < n; i++) s[i] = i;
            Main.QuickSort.sort(s, seed);
            assertTrue(isSorted(s));

            // reversed
            int[] rev = new int[n];
            for (int i = 0; i < n; i++) rev[i] = n - i;
            Main.QuickSort.sort(rev, seed);
            assertTrue(isSorted(rev));

            // duplicates
            int[] dup = new int[n];
            for (int i = 0; i < n; i++) dup[i] = (i % 5);
            Main.QuickSort.sort(dup, seed);
            assertTrue(isSorted(dup));
        }
    }

    @Test
    void quickSort_depth_is_bounded_on_random_inputs() {
        int n = 1 << 20; // 1,048,576
        int[] a = ThreadLocalRandom.current().ints(n).toArray();
        Main.resetAll();
        Main.QuickSort.sort(a, 987654321L);
        int log2n = 31 - Integer.numberOfLeadingZeros(n);
        int bound = 2 * log2n + 32; // щедрая константа
        assertTrue(Main.QS.max <= bound, "QS depth too large: " + Main.QS.max + " > " + bound);
        assertTrue(isSorted(a));
    }
}


