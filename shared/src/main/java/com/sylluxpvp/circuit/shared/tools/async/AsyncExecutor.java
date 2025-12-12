package com.sylluxpvp.circuit.shared.tools.async;

import lombok.experimental.UtilityClass;
import com.sylluxpvp.circuit.shared.CircuitShared;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

@UtilityClass
public class AsyncExecutor {

    private static final int THREAD_POOL_SIZE = 4;
    private static ExecutorService executor;

    public void init() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE, r -> {
                Thread thread = new Thread(r, "Circuit-Async-" + System.currentTimeMillis());
                thread.setDaemon(true);
                return thread;
            });
        }
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                    CircuitShared.getInstance().getLogger().warn("Async executor forced shutdown after timeout.");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        ensureInitialized();
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public CompletableFuture<Void> runAsync(Runnable runnable) {
        ensureInitialized();
        return CompletableFuture.runAsync(runnable, executor);
    }

    public <T> void supplyAsync(Supplier<T> supplier, Consumer<T> callback) {
        ensureInitialized();
        CompletableFuture.supplyAsync(supplier, executor)
                .thenAccept(callback)
                .exceptionally(throwable -> {
                    CircuitShared.getInstance().getLogger().error("Async operation failed: " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }

    public void runAsync(Runnable runnable, Runnable onComplete) {
        ensureInitialized();
        CompletableFuture.runAsync(runnable, executor)
                .thenRun(onComplete)
                .exceptionally(throwable -> {
                    CircuitShared.getInstance().getLogger().error("Async operation failed: " + throwable.getMessage());
                    throwable.printStackTrace();
                    return null;
                });
    }

    private void ensureInitialized() {
        if (executor == null || executor.isShutdown()) {
            init();
        }
    }
}
