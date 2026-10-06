package com.hospital.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .serializeNulls()
            .create();

    public static Gson getGson() {
        return gson;
    }

    public static void sendJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    public static void sendSuccess(HttpServletResponse response, String message, Object data) throws IOException {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("message", message);
        map.put("data", data);
        sendJson(response, map);
    }

    public static void sendSuccess(HttpServletResponse response, String message) throws IOException {
        sendSuccess(response, message, null);
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        sendJson(response, map);
    }
}
