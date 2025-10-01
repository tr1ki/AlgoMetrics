import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    // ---------- Shared tiny insertion sort for ranges ----------
    private static void insertionRange(int[] a, int l, int r) {
        for (int i = l + 1; i <= r; i++) {
            int x = a[i], j = i - 1;
            while (j >= l && a[j] > x) { a[j + 1] = a[j]; j--; }
            a[j + 1] = x;
        }
    }

    // ---------- MergeSort (buffer + cutoff) ----------
    static class MergeSort {
        private static final int CUTOFF = 24;

        public static void sort(int[] a) {
            if (a == null || a.length < 2) return;
            int[] buf = new int[a.length];
            sort(a, 0, a.length - 1, buf);
        }
        private static void sort(int[] a, int l, int r, int[] buf) {
            if (r - l + 1 <= CUTOFF) { insertionRange(a, l, r); return; }
            int m = (l + r) >>> 1;
            sort(a, l, m, buf);
            sort(a, m + 1, r, buf);
            if (a[m] <= a[m + 1]) return; // already sorted
            System.arraycopy(a, l, buf, l, r - l + 1);
            int i = l, j = m + 1, k = l;
            while (i <= m && j <= r) a[k++] = (buf[i] <= buf[j]) ? buf[i++] : buf[j++];
            while (i <= m) a[k++] = buf[i++];
            // right tail already in place
        }
    }

    // ---------- QuickSort (random pivot, recurse smaller side) ----------
    static class QuickSort {
        public static void sort(int[] a, long seed) {
            if (a == null || a.length < 2) return;
            quicksort(a, 0, a.length - 1, new Random(seed));
        }
        private static void quicksort(int[] a, int l, int r, Random rnd) {
            while (l < r) {
                int p = partitionRandom(a, l, r, rnd);
                int leftSize = p - l;
                int rightSize = r - p;
                if (leftSize < rightSize) {        // recurse left
                    if (l < p - 1) quicksort(a, l, p - 1, rnd);
                    l = p + 1;                      // iterate right
                } else {                             // recurse right
                    if (p + 1 < r) quicksort(a, p + 1, r, rnd);
                    r = p - 1;                      // iterate left
                }
            }
        }
        private static int partitionRandom(int[] a, int l, int r, Random rnd) {
            int pivotIdx = l + rnd.nextInt(r - l + 1);
            swap(a, pivotIdx, r);
            return partition(a, l, r);
        }
        private static int partition(int[] a, int l, int r) {
            int pivot = a[r], i = l;
            for (int j = l; j < r; j++) if (a[j] <= pivot) { swap(a, i, j); i++; }
            swap(a, i, r);
            return i;
        }
        private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    }

    // ---------- Deterministic Select (Median of Medians, 5) ----------
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
                swap(a, l + i, medianIdx);
            }
            int mid = (groups - 1) / 2;
            return selectIndex(a, l, l + groups - 1, mid);
        }
        private static int selectIndex(int[] a, int l, int r, int k) {
            while (true) {
                if (l == r) return l;
                int p = partition(a, l, r, l + (r - l) / 2);
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
            for (int j = l; j < r; j++) if (a[j] < pivot) { swap(a, i, j); i++; }
            swap(a, i, r);
            return i;
        }
        private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }
    }

    // ---------- Closest Pair of Points (O(n log n)) ----------
    record Point(double x, double y) {}
    static class ClosestPair {
        public static double closest(Point[] pts) {
            if (pts == null || pts.length < 2) return Double.POSITIVE_INFINITY;
            Point[] px = pts.clone(), py = pts.clone();
            Arrays.sort(px, Comparator.comparingDouble(Point::x));
            Arrays.sort(py, Comparator.comparingDouble(Point::y));
            Point[] buf = new Point[pts.length];
            return solve(px, py, buf, 0, pts.length); // [l, r)
        }
        private static double solve(Point[] px, Point[] py, Point[] buf, int l, int r) {
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

            // split py into left/right by x; tie-break ensures exact sizes
            int ly = l, ry = mid;
            for (int i = l; i < r; i++) {
                if (py[i].x() < midX || (py[i].x() == midX && belongsLeft(px, l, mid, py[i]))) {
                    buf[ly++] = py[i];
                } else {
                    buf[ry++] = py[i];
                }
            }
            System.arraycopy(buf, l, py, l, r - l);

            double dl = solve(px, py, buf, l, mid);
            double dr = solve(px, py, buf, mid, r);
            double d = Math.min(dl, dr);

            mergeByY(py, buf, l, mid, r);

            int sc = 0;
            for (int i = l; i < r; i++) if (Math.abs(py[i].x() - midX) < d) buf[sc++] = py[i];
            for (int i = 0; i < sc; i++) {
                for (int j = i + 1; j < sc && (buf[j].y() - buf[i].y()) < d; j++) {
                    d = Math.min(d, dist(buf[i], buf[j]));
                }
            }
            return d;
        }

        // IntelliJ sometimes says l is always 0 — not true (recursion uses non-zero l).
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

    // ---------- Demo ----------
    public static void main(String[] args) {
        int n = 1 << 16;
        int[] arr1 = ThreadLocalRandom.current().ints(n, -1_000_000, 1_000_000).toArray();
        int[] arr2 = arr1.clone();

        MergeSort.sort(arr1);
        System.out.println("MergeSort ok? " + isSorted(arr1));

        QuickSort.sort(arr2, 42L);
        System.out.println("QuickSort ok? " + isSorted(arr2));

        int[] arr3 = arr2.clone();
        int k = n / 2;
        int kth = SelectMoM5.select(arr3, k);
        System.out.println("Select (k = " + k + ") = " + kth);

        int m = 1 << 12;
        Point[] pts = new Point[m];
        Random rnd = new Random(7);
        for (int i = 0; i < m; i++) pts[i] = new Point(rnd.nextDouble(), rnd.nextDouble());
        double d = ClosestPair.closest(pts);
        System.out.println("Closest pair distance = " + d);
    }

    private static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) if (a[i - 1] > a[i]) return false;
        return true;
    }
}