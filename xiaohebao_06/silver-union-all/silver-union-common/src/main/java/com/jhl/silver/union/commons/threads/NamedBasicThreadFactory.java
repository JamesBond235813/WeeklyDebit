package com.jhl.silver.union.commons.threads;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义名称的线程工厂,基础款
 *
 * @author qingren
 */
public class NamedBasicThreadFactory implements ThreadFactory {
    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;
    private static final String DEFAULT_PREFIX = "SU-POOL";

    public NamedBasicThreadFactory(String prefix) {
        Thread.currentThread().getThreadGroup();
        if (StringUtils.isBlank(prefix)) {
            prefix = DEFAULT_PREFIX;
        }
        namePrefix = prefix + "-" + poolNumber.getAndIncrement() + "-thread-";
    }

    public NamedBasicThreadFactory() {
        this(DEFAULT_PREFIX);
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
        if (t.isDaemon()) {
            t.setDaemon(false);
        }
        if (t.getPriority() != Thread.NORM_PRIORITY) {
            t.setPriority(Thread.NORM_PRIORITY);
        }
        return t;
    }
}
