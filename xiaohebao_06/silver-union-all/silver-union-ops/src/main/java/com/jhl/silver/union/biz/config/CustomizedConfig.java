package com.jhl.silver.union.biz.config;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author: qingren
 * @create_time: 2025/3/16
 */
@Configuration
@EnableAsync
public class CustomizedConfig {

    @Resource
    private BeanFactory beanFactory;

    @Bean
    @ConfigurationProperties(prefix = "login")
    public LoginLockConfig loginLockConfig() {
        return new LoginLockConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "biz")
    public BizProperty bizProperty() {
        return new BizProperty();
    }

    @Bean("tracedTaskExecutor")
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(10);        // 最大线程数
        executor.setQueueCapacity(20);      // 队列容量
        executor.setKeepAliveSeconds(60);   // 临时线程空闲回收时间
        executor.setThreadNamePrefix("su-crm-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setTaskDecorator(new MDCContextTaskDecorator());
        executor.initialize();
        return executor;
    }
}
