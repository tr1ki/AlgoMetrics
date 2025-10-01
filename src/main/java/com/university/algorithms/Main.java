package com.university.algorithms;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    // ------------ Метрики ------------
    static class Timer { long t0; void start(){ t0 = System.nanoTime(); } long stop(){ return System.nanoTime() - t0; } }
    static class Depth { int cur, max; void enter(){ if (++cur > max) max = cur; } void exit(){ cur--; } void reset(){ cur = max = 0; } }
    static final Depth MS = new Depth(), QS = new Depth(), CP = new Depth();
    static long CMP = 0, MOV = 0, ALLOC = 0;
    static void resetAll() { CMP = MOV = ALLOC = 0; MS.reset(); QS.reset(); CP.reset(); }

    // ------------ Общий insertion sort для диапазона ------------
    private static void insertionRange(int[] a, int l, int r) {
        for (int i = l + 1; i <= r; i++) {
            int x = a[i], j = i - 1;
            while (j >= l) {
                CMP++; // сравнение a[j] > x
                if (a[j] <= x) break;
                a[j + 1] = a[j]; MOV++; // перенос
                j--;
            }
            a[j + 1] = x; MOV++;           // финальная вставка тоже запись
        }
    }

    // ------------ MergeSort (буфер + cutoff) ------------
    static class MergeSort {
        private static final int CUTOFF = 24;

        public static void sort(int[] a) {
            if (a == null || a.length < 2) return;
            int[] buf = new int[a.length];
            ALLOC++; // буфер для mergesort
            sort(a, 0, a.length - 1, buf);
        }
        private static void sort(int[] a, int l, int r, int[] buf) {
            MS.enter();
            try {
                if (r - l + 1 <= CUTOFF) { insertionRange(a, l, r); return; }
                int m = (l + r) >>> 1;
                sort(a, l, m, buf);
                sort(a, m + 1, r, buf);
                CMP++;                          // сравнение границы
                if (a[m] <= a[m + 1]) return;   // уже отсортированы
                System.arraycopy(a, l, buf, l, r - l + 1);
                MOV += (r - l + 1);             // опционально: посчитать копирование в буфер
                int i = l, j = m + 1, k = l;
                while (i <= m && j <= r) {
                    CMP++; // сравнение buf[i] <= buf[j]
                    if (buf[i] <= buf[j]) { a[k++] = buf[i++]; MOV++; }
                    else { a[k++] = buf[j++]; MOV++; }
                }
                while (i <= m) { a[k++] = buf[i++]; MOV++; }
                // правый хвост уже на месте
            } finally {
                MS.exit();
            }
        }
    }

    // ------------ QuickSort (рандом pivot, рекурс в меньшую часть) ------------
    static class QuickSort {
        public static void sort(int[] a, long seed) {
            if (a == null || a.length < 2) return;
            quicksort(a, 0, a.length - 1, new Random(seed), 1);
        }
        private static void quicksort(int[] a, int l, int r, Random rnd, int depth) {
            QS.max = Math.max(QS.max, depth); // считаем максимальную глубину
            while (l < r) {
                int p = partitionRandom(a, l, r, rnd);
                int leftSize = p - l;
                int rightSize = r - p;
                if (leftSize < rightSize) {        // рекурс в меньшую (левую)
                    if (l < p - 1) quicksort(a, l, p - 1, rnd, depth + 1);
                    l = p + 1;                      // итерация по правой
                } else {                             // рекурс в меньшую (правую)
                    if (p + 1 < r) quicksort(a, p + 1, r, rnd, depth + 1);
                    r = p - 1;                      // итерация по левой
                }
            }
        }
        private static int partitionRandom(int[] a, int l, int r, Random rnd) {
            int pivotIdx = l + rnd.nextInt(r - l + 1);
            swap(a, pivotIdx, r);
            MOV += 3;
            return partition(a, l, r);
        }
        private static int partition(int[] a, int l, int r) {
            int pivot = a[r], i = l;
            for (int j = l; j < r; j++) {
                CMP++; // сравнение с pivot
                if (a[j] <= pivot) { swap(a, i, j); MOV += 3; i++; }
            }
            swap(a, i, r); MOV += 3;
            return i;
        }
        private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    }

    // ------------ Deterministic Select (Median of Medians, 5) ------------
    static class SelectMoM5 {
        public static int select(int[] a, int k) {
            if (a == null || a.length == 0) throw new IllegalArgumentException("empty");
            if (k < 0 || k >= a.length) throw new IllegalArgumentException("k out of range");
            return select(a, 0, a.length - 1, k);
        }
        private static int select(int[] a, int l, int r, int k) {
            while (true) {
                if (l == r) return a[l];
                int pivotIdx = medianOfMedians(a, l, r);
                int p = partition(a, l, r, pivotIdx);
                int rank = p - l;
                if (k == rank) return a[p];
                else if (k < rank) r = p - 1;
                else { k -= (rank + 1); l = p + 1; }
            }
        }
        private static int medianOfMedians(int[] a, int l, int r) {
            int n = r - l + 1, groups = (n + 4) / 5;
            for (int i = 0; i < groups; i++) {
                int gl = l + i * 5, gr = Math.min(gl + 4, r);
                insertionRange(a, gl, gr);
                int medianIdx = gl + (gr - gl) / 2;
                swap(a, l + i, medianIdx); MOV += 3;
            }
            int mid = (groups - 1) / 2;
            return selectIndex(a, l, l + groups - 1, mid);
        }
        private static int selectIndex(int[] a, int l, int r, int k) {
            while (true) {
                if (l == r) return l;
                int p = partition(a, l, r, medianOfMedians(a, l, r)); // MoM5-пивот
                int rank = p - l;
                if (k == rank) return p;
                else if (k < rank) r = p - 1;
                else { k -= (rank + 1); l = p + 1; }
            }
        }
        private static int partition(int[] a, int l, int r, int pivotIdx) {
            int pivot = a[pivotIdx];
            swap(a, pivotIdx, r);
            int i = l;
            for (int j = l; j < r; j++) {
                CMP++; // a[j] < pivot
                if (a[j] < pivot) { swap(a, i, j); MOV += 3; i++; }
            }
            swap(a, i, r); MOV += 3;
            return i;
        }
        private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    }

    // ------------ Closest Pair of Points (O(n log n)) ------------
    static class Point {
        final double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
        double x() { return x; }
        double y() { return y; }
    }
    static class ClosestPair {
        public static double closest(Point[] pts) {
            if (pts == null || pts.length < 2) return Double.POSITIVE_INFINITY;
            Point[] px = pts.clone(), py = pts.clone();
            ALLOC += 2; // два массива-клона
            Arrays.sort(px, Comparator.comparingDouble(Point::x));
            Arrays.sort(py, Comparator.comparingDouble(Point::y));
            Point[] buf = new Point[pts.length];
            ALLOC++; // буфер для py
            return solve(px, py, buf, 0, pts.length); // [l, r)
        }
        private static double solve(Point[] px, Point[] py, Point[] buf, int l, int r) {
            CP.enter();
            try {
                int n = r - l;
                if (n <= 3) {
                    double best = Double.POSITIVE_INFINITY;
                    for (int i = l; i < r; i++)
                        for (int j = i + 1; j < r; j++)
                            best = Math.min(best, dist(px[i], px[j]));
                    Arrays.sort(py, l, r, Comparator.comparingDouble(Point::y));
                    return best;
                }
                int mid = l + n / 2;
                double midX = px[mid].x();
                int ly = l, ry = mid;
                for (int i = l; i < r; i++) {
                    if (py[i].x() < midX || (py[i].x() == midX && belongsLeft(px, l, mid, py[i]))) buf[ly++] = py[i];
                    else buf[ry++] = py[i];
                }
                System.arraycopy(buf, l, py, l, r - l);
                double dl = solve(px, py, buf, l, mid);
                double dr = solve(px, py, buf, mid, r);
                double d = Math.min(dl, dr);
                mergeByY(py, buf, l, mid, r);
                int sc = 0;
                for (int i = l; i < r; i++) if (Math.abs(py[i].x() - midX) < d) buf[sc++] = py[i];
                for (int i = 0; i < sc; i++)
                    for (int j = i + 1; j < sc && (buf[j].y() - buf[i].y()) < d; j++) {
                        double dij = dist(buf[i], buf[j]); CMP++; // сравнение dij < d
                        if (dij < d) d = dij;
                    }
                return d;
            } finally {
                CP.exit();
            }
        }
        @SuppressWarnings("SameParameterValue")
        private static boolean belongsLeft(Point[] px, int l, int mid, Point p) {
            for (int i = l; i < mid; i++) if (px[i] == p) return true;
            return false;
        }
        private static void mergeByY(Point[] py, Point[] tmp, int l, int m, int r) {
            int i = l, j = m, k = l;
            while (i < m && j < r) tmp[k++] = (py[i].y() <= py[j].y()) ? py[i++] : py[j++];
            while (i < m) tmp[k++] = py[i++];
            while (j < r) tmp[k++] = py[j++];
            System.arraycopy(tmp, l, py, l, r - l);
        }
        private static double dist(Point a, Point b) {
            double dx = a.x() - b.x(), dy = a.y() - b.y();
            return Math.hypot(dx, dy);
        }
    }

    // ------------ Bench в CSV ------------
    private static void bench() throws Exception {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"algo","n","time_ns","max_depth","cmp","mov","alloc"});

        // MergeSort
        for (int k = 10; k <= 20; k++) {
            int n = 1 << k; resetAll();
            int[] a = ThreadLocalRandom.current().ints(n).toArray();
            Timer t = new Timer(); t.start(); MergeSort.sort(a); long ns = t.stop();
            rows.add(new String[]{"mergesort", String.valueOf(n), String.valueOf(ns),
                    String.valueOf(MS.max), String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
        }
        // QuickSort
        for (int k = 10; k <= 20; k++) {
            int n = 1 << k; resetAll();
            int[] a = ThreadLocalRandom.current().ints(n).toArray();
            Timer t = new Timer(); t.start(); QuickSort.sort(a, 42); long ns = t.stop();
            rows.add(new String[]{"quicksort", String.valueOf(n), String.valueOf(ns),
                    String.valueOf(QS.max), String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
        }
        // Select (k = n/2)
        for (int k = 14; k <= 21; k++) {
            int n = 1 << k;
            resetAll();
            int[] a = ThreadLocalRandom.current().ints(n).toArray();
            Timer t = new Timer(); t.start(); int v = SelectMoM5.select(a, n/2); long ns = t.stop();
            if (v == Integer.MIN_VALUE) System.out.print("");
            rows.add(new String[]{"select_mom5", String.valueOf(n), String.valueOf(ns), "0",
                    String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
        }
        // Closest Pair
        for (int k = 10; k <= 17; k++) {
            int n = 1 << k; resetAll();
            Random rnd = new Random(7);
            Point[] pts = new Point[n];
            for (int i = 0; i < n; i++) pts[i] = new Point(rnd.nextDouble(), rnd.nextDouble());
            Timer t = new Timer(); t.start(); double d = ClosestPair.closest(pts); long ns = t.stop();
            if (d < 0) System.out.print("");
            rows.add(new String[]{"closest_pair", String.valueOf(n), String.valueOf(ns), String.valueOf(CP.max),
                    String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
        }
        try (var w = java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get("bench.csv"))) {
            for (var r : rows) { w.write(String.join(",", r)); w.newLine(); }
        }
        System.out.println("Wrote bench.csv (" + (rows.size()-1) + " rows)");
    }

    // ------------ Demo/entrypoint ------------
    public static void main(String[] args) throws Exception {
		if (args.length > 0 && args[0].equalsIgnoreCase("bench")) { bench(); return; }

		// Простые флаги: --algo, --n, --trials, --seed, --csv
		Map<String, String> flags = parseFlags(args);
		String algo = flags.getOrDefault("algo", "demo").toLowerCase(Locale.ROOT);
		int n = Integer.parseInt(flags.getOrDefault("n", String.valueOf(1 << 16)));
		int trials = Integer.parseInt(flags.getOrDefault("trials", "1"));
		long seed = Long.parseLong(flags.getOrDefault("seed", "42"));
		String csvPath = flags.get("csv");

		if (algo.equals("demo")) {
			// Старый демо-режим
			int[] arr1 = ThreadLocalRandom.current().ints(n, -1_000_000, 1_000_000).toArray();
			int[] arr2 = arr1.clone();
			MergeSort.sort(arr1);
			System.out.println("MergeSort ok? " + isSorted(arr1));
			QuickSort.sort(arr2, seed);
			System.out.println("QuickSort ok? " + isSorted(arr2));
			int[] arr3 = arr2.clone();
			int k = n / 2;
			int kth = SelectMoM5.select(arr3, k);
			System.out.println("Select (k = " + k + ") = " + kth);
			int m = Math.min(1 << 12, Math.max(4, n >>> 4));
			Point[] pts = new Point[m];
			Random rnd = new Random(7);
			for (int i = 0; i < m; i++) pts[i] = new Point(rnd.nextDouble(), rnd.nextDouble());
			double d = ClosestPair.closest(pts);
			System.out.println("Closest pair distance = " + d);
			return;
		}

		List<String[]> rows = new ArrayList<>();
		rows.add(new String[]{"algo","n","time_ns","max_depth","cmp","mov","alloc"});
		Random rnd = new Random(seed);
		for (int t = 0; t < trials; t++) {
			resetAll();
			Timer timer = new Timer();
			long timeNs;
			switch (algo) {
				case "mergesort": {
					int[] a = rnd.ints(n).toArray();
					timer.start();
					MergeSort.sort(a);
					timeNs = timer.stop();
					rows.add(new String[]{"mergesort", String.valueOf(n), String.valueOf(timeNs), String.valueOf(MS.max), String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
					break;
				}
				case "quicksort": {
					int[] a = rnd.ints(n).toArray();
					timer.start();
					QuickSort.sort(a, rnd.nextLong());
					timeNs = timer.stop();
					rows.add(new String[]{"quicksort", String.valueOf(n), String.valueOf(timeNs), String.valueOf(QS.max), String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
					break;
				}
				case "select": {
					int[] a = rnd.ints(n).toArray();
					int k = n / 2;
					timer.start();
					int v = SelectMoM5.select(a, k);
					timeNs = timer.stop();
					if (v == Integer.MIN_VALUE) System.out.print("");
					rows.add(new String[]{"select_mom5", String.valueOf(n), String.valueOf(timeNs), "0", String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
					break;
				}
				case "closest": {
					Point[] pts = new Point[n];
					for (int i = 0; i < n; i++) pts[i] = new Point(rnd.nextDouble(), rnd.nextDouble());
					timer.start();
					double d = ClosestPair.closest(pts);
					timeNs = timer.stop();
					if (d < 0) System.out.print("");
					rows.add(new String[]{"closest_pair", String.valueOf(n), String.valueOf(timeNs), String.valueOf(CP.max), String.valueOf(CMP), String.valueOf(MOV), String.valueOf(ALLOC)});
					break;
				}
				default:
					throw new IllegalArgumentException("Unknown --algo: " + algo);
			}
		}

		if (csvPath != null && !csvPath.isEmpty()) {
			try (var w = java.nio.file.Files.newBufferedWriter(java.nio.file.Paths.get(csvPath))) {
				for (var r : rows) { w.write(String.join(",", r)); w.newLine(); }
			}
			System.out.println("Wrote " + csvPath + " (" + (rows.size()-1) + " rows)");
		} else {
			for (var r : rows) System.out.println(String.join(",", r));
		}
    }

	private static Map<String, String> parseFlags(String[] args) {
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < args.length; i++) {
			String s = args[i];
			if (s.startsWith("--")) {
				String key = s.substring(2);
				String val = "true";
				if (i + 1 < args.length && !args[i + 1].startsWith("--")) { val = args[++i]; }
				map.put(key, val);
			}
		}
		return map;
	}

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }
}


