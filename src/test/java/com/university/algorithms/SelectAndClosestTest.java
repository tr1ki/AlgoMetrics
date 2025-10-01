package com.university.algorithms;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectAndClosestTest {

    @Test
    void select_matches_sorted_k_over_100_trials() {
        Random rnd = new Random(123);
        for (int t = 0; t < 100; t++) {
            int n = 1000;
            int[] a = rnd.ints(n, -1_000_000, 1_000_000).toArray();
            int k = rnd.nextInt(n);
            int[] b = a.clone();
            Arrays.sort(b);
            int expected = b[k];
            int actual = Main.SelectMoM5.select(a.clone(), k);
            assertEquals(expected, actual);
        }
    }

    @Test
    void closest_pair_matches_quadratic_on_small_n() {
        for (int n : new int[]{2, 3, 10, 100, 500, 2000}) {
            Random rnd = new Random(n);
            Main.Point[] pts = new Main.Point[n];
            for (int i = 0; i < n; i++) pts[i] = new Main.Point(rnd.nextDouble(), rnd.nextDouble());
            double fast = Main.ClosestPair.closest(pts);
            double slow = slowClosest(pts);
            assertTrue(Math.abs(fast - slow) < 1e-9, "n=" + n + " fast=" + fast + " slow=" + slow);
        }
    }

    private static double slowClosest(Main.Point[] pts) {
        double best = Double.POSITIVE_INFINITY;
        for (int i = 0; i < pts.length; i++)
            for (int j = i + 1; j < pts.length; j++)
                best = Math.min(best, dist(pts[i], pts[j]));
        return best;
    }

    private static double dist(Main.Point a, Main.Point b) {
        double dx = a.x() - b.x(), dy = a.y() - b.y();
        return Math.hypot(dx, dy);
    }
}


