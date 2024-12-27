package org.util;


import com.google.gson.Gson;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class GsonUtil {

    private static Gson gson = new Gson();

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    @Nullable
    public static <T> T fromJson(String json, Class<T> t) {
        return gson.fromJson(json, t);
    }

    public static <T> T fromJson(String json, Type typeOfT) {
        return gson.fromJson(json, typeOfT);
    }
}
