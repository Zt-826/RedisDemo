package com.example.redisdemo.utils;

import com.google.gson.Gson;

public class GsonUtil {

    /**
     * Obj转Json
     *
     * @param obj obj
     * @return Json
     */
    public static String toJson(Object obj) {
        return getGson().toJson(obj);
    }

    /**
     * Json转Object
     *
     * @param json  json
     * @param clazz class对象
     * @param <T>   泛型
     * @return Object
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        return getGson().fromJson(json, clazz);
    }

    /**
     * 静态内部类，延迟创建对象
     */
    private static class InnerGson {
        private static final Gson gson = new Gson();
    }

    public static Gson getGson() {
        return InnerGson.gson;
    }
}
