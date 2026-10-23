package com.fivesense.api.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Dedicated thread pool for e-mail delivery, so a slow or unreachable SMTP server never blocks the
 * HTTP request that triggered the message. E-mail stays best effort: when the queue is full the
 * message is dropped and logged instead of delaying the caller.
 */
@Configuration
@EnableAsync
public class AsyncConfiguration {
    public static final String MAIL_EXECUTOR="mailExecutor";
    private static final Logger log=LoggerFactory.getLogger(AsyncConfiguration.class);

    @Bean(name=MAIL_EXECUTOR)
    ThreadPoolTaskExecutor mailExecutor(){
        ThreadPoolTaskExecutor executor=new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("mail-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setRejectedExecutionHandler((task,pool)->log.warn("Mail queue is full; message dropped"));
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(15);
        return executor;
    }
}
