package com.jhl.silver.union.commons.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @description: 枚举工具类
 */
public class EnumUtils {

    /**
     * 将枚举转成map
     *
     * @param <V>       map key的类型
     * @param <E>       map value(枚举)的类型
     * @param enumArray 枚举数组
     * @param getKey    从枚举中取出KEY的方法
     * @return
     */
    public static <V, E> Map<V, E> enum2Map(E[] enumArray, Function<E, V> getKey) {
        Map<V, E> repo = new HashMap<>();
        for (E element : enumArray) {
            repo.put(getKey.apply(element), element);
        }
        return repo;
    }
}
