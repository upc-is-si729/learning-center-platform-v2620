package com.acme.center.platform.shared.infrastructure.concurrency.loom.configuration;

import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Loom Concurrency Configuration.
 * <p>
 * Configures asynchronous task execution using OpenJDK Project Loom virtual threads.
 * </p>
 */
@Configuration
@EnableAsync
@NullMarked
public class VirtualThreadsConfiguration {

    /**
     * Creates a {@link TaskExecutor} bean backed by Project Loom virtual threads.
     *
     * @return task executor configured with virtual threads
     */
    @Bean(name = "applicationTaskExecutor")
    public TaskExecutor applicationTaskExecutor() {
        var executor = new SimpleAsyncTaskExecutor("loom-exec-");
        executor.setVirtualThreads(true);
        return executor;
    }
}
