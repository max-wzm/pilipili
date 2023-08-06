package org.wzm.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

public final class JsonUtil {
    private static final Gson GSON                       = new GsonBuilder().create();
    private static final Gson GSON_WITHOUT_HTML_ESCAPING =  new GsonBuilder().disableHtmlEscaping().create();

    private JsonUtil() {
    }

    public static String toJson(Object object) {
        return GSON.toJson(object);
    }

    public static String toJsonWithoutHtmlEscaping(Object object) {
        return GSON_WITHOUT_HTML_ESCAPING.toJson(object);
    }

    public static <T> T fromJson(String doc, Class<T> clazz) {
        return GSON.fromJson(doc, clazz);
    }

    /**
     * 利用JSON深拷贝对象，大量调用时注意性能损耗
     */
    public static <T> T deepCopy(T src, Class<T> clazz) {
        return GSON.fromJson(toJson(src), clazz);
    }

    public static <T> T fromJson(String json, Type type) {
        return GSON.fromJson(json, type);
    }
}