package com.jhl.silver.union.commons.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * @author qingren
 * @date 2019/8/22 3:24 PM
 */
public class GsonHelper {

    public final static Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Date.class, new DateToLongTypeAdapter())
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    /**
     * 不处理HTML特殊字符转义的Gson对象
     */
    public static final Gson GSON_NHE = new GsonBuilder()
            .disableHtmlEscaping()
            .registerTypeAdapter(Date.class, new DateToLongTypeAdapter())
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .create();

    /**
     * 生成json 字符串. 不转义 html 特殊字符
     *
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        return toJson(obj, false);
    }

    /**
     * 生成json 字符串.
     *
     * @param obj
     * @param htmlEscaped true:转义 html 特殊字符, false: 不转义
     * @return
     */
    public static String toJson(Object obj, boolean htmlEscaped) {
        if (htmlEscaped) {
            return GSON.toJson(obj);
        }
        return GSON_NHE.toJson(obj);
    }

    public static <T> T fromJson(String json, Class<T> classOfT) {
        return GSON.fromJson(json, classOfT);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return GSON.fromJson(json, typeOfT);
    }

}
