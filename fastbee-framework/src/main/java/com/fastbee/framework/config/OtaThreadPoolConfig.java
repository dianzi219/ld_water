package com.fastbee.framework.config;

import com.fastbee.common.constant.FastBeeConstant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class OtaThreadPoolConfig
{
    // 核心线程池大小
    private int corePoolSize = 10;
    // 最大可创建的线程数
    private int maxPoolSize = 20;
    // 队列最大长度
    private int queueCapacity = 10000;
    // 线程池维护线程所允许的空闲时间
    private int keepAliveSeconds = 30;
    // 配置线程池的前缀
    private static final String threadNamePrefix = "ota-";

    @Bean(name = FastBeeConstant.TASK.OTA_THREAD_POOL)
    public ThreadPoolTaskExecutor threadPoolTaskExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setMaxPoolSize(maxPoolSize);
        executor.setCorePoolSize(corePoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix(threadNamePrefix);
        // 线程池对拒绝任务(无线程可用)的处理策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 进行加载
        executor.initialize();
        return executor;
    }
}
