package com.hospital.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CookieUtil {

    private static final int DEFAULT_MAX_AGE = 30 * 24 * 60 * 60; // 30 days

    public static void setCookie(HttpServletResponse response, String name, String value, int maxAge) {
        try {
            String encodedValue = value != null ? URLEncoder.encode(value, StandardCharsets.UTF_8) : "";
            Cookie cookie = new Cookie(name, encodedValue);
            cookie.setPath("/");
            cookie.setMaxAge(maxAge);
            cookie.setHttpOnly(false); // Accessible to JavaScript DOM if needed for preferences
            response.addCookie(cookie);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setCookie(HttpServletResponse response, String name, String value) {
        setCookie(response, name, value, DEFAULT_MAX_AGE);
    }

    public static String getCookieValue(HttpServletRequest request, String name) {
        if (request == null || request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                try {
                    return URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
