package org.LudoHeritage.stress;

import org.LudoHeritage.model.GameRepository;
import org.LudoHeritage.service.GameService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Stress tests for the in-memory game catalog layer.
 * No Spring context, no database — pure concurrent load on GameService.
 */
class GameServiceStressTest {

    static GameService gameService;

    @BeforeAll
    static void setUp() {
        GameRepository repo = mock(GameRepository.class);
        when(repo.findAll()).thenReturn(Collections.emptyList());
        gameService = new GameService(repo);
    }

    // ── Test 1: getAllGames under 50 concurrent threads ────────────────────────

    @Test
    void stress_getAllGames_50threads_100iterationsEach_zeroErrors() throws InterruptedException {
        int threads = 50;
        int iters   = 100;

        AtomicInteger errors = new AtomicInteger(0);
        List<Long> nanosPerCall = Collections.synchronizedList(new ArrayList<>(threads * iters));

        ExecutorService pool    = Executors.newFixedThreadPool(threads);
        CountDownLatch  ready   = new CountDownLatch(threads);
        CountDownLatch  go      = new CountDownLatch(1);
        CountDownLatch  done    = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                ready.countDown();
                try { go.await(); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

                for (int i = 0; i < iters; i++) {
                    long t0 = System.nanoTime();
                    try {
                        List<?> result = gameService.getAllGames(null, null, null, null);
                        if (result.isEmpty()) errors.incrementAndGet();
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    nanosPerCall.add(System.nanoTime() - t0);
                }
                done.countDown();
            });
        }

        ready.await();
        go.countDown();
        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        printResults("getAllGames (50 threads × 100 calls)", threads, iters, errors, nanosPerCall);

        assertThat(finished).as("All threads completed within 30 s").isTrue();
        assertThat(errors.get()).as("Zero errors under concurrent load").isZero();
        double avgMs = avg(nanosPerCall);
        assertThat(avgMs).as("Average latency < 50 ms").isLessThan(50.0);
    }

    // ── Test 2: mixed operations under 50 concurrent threads ──────────────────

    @Test
    void stress_mixedOperations_50threads_zeroErrors() throws InterruptedException {
        int threads = 50;
        int iters   = 60;

        AtomicInteger errors = new AtomicInteger(0);
        List<Long> nanosPerCall = Collections.synchronizedList(new ArrayList<>(threads * iters));

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch  done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            int tid = t;
            pool.submit(() -> {
                for (int i = 0; i < iters; i++) {
                    long t0 = System.nanoTime();
                    try {
                        switch (i % 5) {
                            case 0 -> gameService.getAllGames(null, null, null, null);
                            case 1 -> gameService.getGameById((long)(tid % 20) + 1);
                            case 2 -> gameService.getRegions();
                            case 3 -> gameService.getPeriods();
                            case 4 -> gameService.getGameOfTheDay();
                        }
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    nanosPerCall.add(System.nanoTime() - t0);
                }
                done.countDown();
            });
        }

        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        printResults("Mixed ops (50 threads × 60 calls)", threads, iters, errors, nanosPerCall);

        assertThat(finished).as("All threads completed within 30 s").isTrue();
        assertThat(errors.get()).as("Zero errors in mixed concurrent operations").isZero();
    }

    // ── Test 3: search under concurrent load ──────────────────────────────────

    @Test
    void stress_searchAndFilter_30threads_zeroErrors() throws InterruptedException {
        int threads = 30;
        int iters   = 80;

        AtomicInteger errors = new AtomicInteger(0);
        List<Long> nanosPerCall = Collections.synchronizedList(new ArrayList<>(threads * iters));

        String[] queries  = {"Awale", "Go", "Chess", "Backgammon", null};
        String[] regions  = {"Afrique", "Asie", "Europe", null};

        Random rng = new Random(2025);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch  done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                for (int i = 0; i < iters; i++) {
                    long t0 = System.nanoTime();
                    try {
                        String q      = queries[rng.nextInt(queries.length)];
                        String region = regions[rng.nextInt(regions.length)];
                        gameService.getAllGames(q, region, null, null);
                    } catch (Exception e) {
                        errors.incrementAndGet();
                    }
                    nanosPerCall.add(System.nanoTime() - t0);
                }
                done.countDown();
            });
        }

        boolean finished = done.await(30, TimeUnit.SECONDS);
        pool.shutdownNow();

        printResults("Search/filter (30 threads × 80 calls)", threads, iters, errors, nanosPerCall);

        assertThat(finished).as("All threads completed within 30 s").isTrue();
        assertThat(errors.get()).as("Zero errors in concurrent search").isZero();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static double avg(List<Long> nanos) {
        return nanos.stream().mapToLong(Long::longValue).average().orElse(0) / 1_000_000.0;
    }

    private static void printResults(String label, int threads, int iters,
                                     AtomicInteger errors, List<Long> nanos) {
        long total = (long) threads * iters;
        double avgMs = avg(nanos);
        long[] sorted = nanos.stream().mapToLong(Long::longValue).sorted().toArray();
        double p95Ms = sorted[(int) (sorted.length * 0.95)] / 1_000_000.0;
        double p99Ms = sorted[(int) (sorted.length * 0.99)] / 1_000_000.0;

        System.out.printf("%n─── %s ──────────────%n", label);
        System.out.printf("  Total calls  : %,d%n", total);
        System.out.printf("  Errors       : %d (%.2f%%)%n", errors.get(), 100.0 * errors.get() / total);
        System.out.printf("  Avg latency  : %.3f ms%n", avgMs);
        System.out.printf("  P95 latency  : %.3f ms%n", p95Ms);
        System.out.printf("  P99 latency  : %.3f ms%n", p99Ms);
    }
}
