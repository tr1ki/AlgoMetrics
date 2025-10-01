package com.university.algorithms.bench;

import com.university.algorithms.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.ChainedOptionsBuilder;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(value = 1)
public class SelectBench {

    @State(Scope.Thread)
    public static class DataState {
        @Param({"1024", "4096", "16384", "65536"})
        public int n;

        @Param({"7"})
        public long seed;

        int[] data;

        @Setup(Level.Trial)
        public void setup() {
            Random rnd = new Random(seed);
            data = rnd.ints(n, Integer.MIN_VALUE, Integer.MAX_VALUE).toArray();
        }
    }

    @Benchmark
    public int selectMid(DataState s) {
        int[] a = s.data.clone();
        int k = a.length / 2;
        return Main.SelectMoM5.select(a, k);
    }

    @Benchmark
    public int sortThenPick(DataState s) {
        int[] a = s.data.clone();
        Arrays.sort(a);
        return a[a.length / 2];
    }

    public static void main(String[] args) throws Exception {
        ChainedOptionsBuilder base = new OptionsBuilder()
                .include(SelectBench.class.getSimpleName())
                .detectJvmArgs()
                .addProfiler(GCProfiler.class);
        Options opt = base.build();
        new Runner(opt).run();
    }
}


