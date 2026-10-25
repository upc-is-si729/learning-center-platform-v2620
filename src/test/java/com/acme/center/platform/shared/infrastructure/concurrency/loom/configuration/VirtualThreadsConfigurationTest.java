package com.acme.center.platform.shared.infrastructure.concurrency.loom.configuration;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VirtualThreadsConfigurationTest {

    @Test
    void applicationTaskExecutorExecutesTasksOnVirtualThreads() throws ExecutionException, InterruptedException {
        var configuration = new VirtualThreadsConfiguration();
        var executor = configuration.applicationTaskExecutor();

        assertNotNull(executor);

        var future = new CompletableFuture<Boolean>();
        executor.execute(() -> future.complete(Thread.currentThread().isVirtual()));

        assertTrue(future.get(), "Task should execute on a Project Loom virtual thread");
    }
}
