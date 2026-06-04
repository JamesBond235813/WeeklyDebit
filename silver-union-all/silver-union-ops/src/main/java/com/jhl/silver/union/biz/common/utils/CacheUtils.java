package com.jhl.silver.union.biz.common.utils;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author: qingren
 * @create_time: 2025/3/19
 */
public class CacheUtils {

    /**
     * 构建简易缓存。
     *
     * @param expireMinutes 缓存数据失效时间， 单位分钟
     * @param loadFunc      缓存中无数据时，加载数据的方法
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K extends Serializable, V> LoadingCache<K, Optional<V>> buildBasicLoadingCache(long expireMinutes,
            Function<K, V> loadFunc) {
        LoadingCache<K, Optional<V>> cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expireMinutes, TimeUnit.MINUTES)
                .softValues()
                .maximumSize(100)
                .initialCapacity(10)
                .build(new CacheLoader<K, Optional<V>>() {
                    @Override
                    public Optional<V> load(K key) throws Exception {
                        if (Objects.isNull(key)) {
                            return Optional.empty();
                        }
                        if (key instanceof String s && StringUtils.isBlank(s)) {
                            return Optional.empty();
                        }
                        return Optional.ofNullable(loadFunc.apply(key));
                    }
                });
        return cache;
    }

}
