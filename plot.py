import csv, math, os
import matplotlib.pyplot as plt

# читаем CSV
rows = []
with open('bench.csv', newline='') as f:
    r = csv.DictReader(f)
    rows = list(r)

def S(algo):
    s = [(int(x['n']), float(x['time_ns']), int(x['max_depth']))
         for x in rows if x['algo'] == algo]
    return sorted(s, key=lambda t: t[0])

os.makedirs('plots', exist_ok=True)

# 1) time/(n log2 n) — для n log n алгоритмов
plt.figure()
for algo in ['mergesort', 'quicksort', 'closest_pair']:
    s = S(algo)
    if not s:
        continue
    xs = [n for n, _, _ in s]
    ys = [t / (n * math.log2(n)) for n, t, _ in s]
    plt.plot(xs, ys, marker='o', label=algo)
plt.xscale('log', base=2)
plt.grid(True); plt.legend()
plt.title('time/(n log2 n)')
plt.xlabel('n'); plt.ylabel('time_ns / (n log2 n)')
plt.tight_layout()
plt.savefig('plots/nlogn_norm.png', dpi=160)

# 2) select: time/n — линейность
plt.figure()
s = S('select_mom5')
xs = [n for n, _, _ in s]
ys = [t / n for n, t, _ in s]
plt.xscale('log', base=2); plt.grid(True)
plt.plot(xs, ys, marker='o', label='select_mom5')
plt.legend()
plt.title('select: time/n')
plt.xlabel('n'); plt.ylabel('time_ns / n')
plt.tight_layout()
plt.savefig('plots/select_norm.png', dpi=160)

# 3) глубина vs log2 n
plt.figure()
for algo in ['mergesort', 'quicksort', 'closest_pair']:
    s = S(algo)
    if not s:
        continue
    xs = [math.log2(n) for n, _, _ in s]
    ys = [d for _, _, d in s]
    plt.plot(xs, ys, marker='o', label=algo)
plt.grid(True); plt.legend()
plt.title('max_depth vs log2 n')
plt.xlabel('log2 n'); plt.ylabel('max_depth')
plt.tight_layout()
plt.savefig('plots/depth.png', dpi=160)