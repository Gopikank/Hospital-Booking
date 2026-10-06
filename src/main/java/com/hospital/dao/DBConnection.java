package com.hospital.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
            if (is != null) {
                props.load(is);
                Class.forName(props.getProperty("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
                url = props.getProperty("jdbc.url");
                username = props.getProperty("jdbc.username");
                password = props.getProperty("jdbc.password");
            } else {
                // Fallback default
                Class.forName("com.mysql.cj.jdbc.Driver");
                url = "jdbc:mysql://localhost:3306/hospital_queue_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
                username = "root";
                password = "Gopika@2006";
            }

            // Cloud deployment override via Environment Variables (e.g. Render / Railway / Docker)
            String envUrl = System.getenv("DB_URL");
            String envUser = System.getenv("DB_USER");
            String envPassword = System.getenv("DB_PASSWORD");

            if (envUrl != null && !envUrl.trim().isEmpty()) {
                url = envUrl;
            }
            if (envUser != null && !envUser.trim().isEmpty()) {
                username = envUser;
            }
            if (envPassword != null) {
                password = envPassword;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize database connection configuration", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
