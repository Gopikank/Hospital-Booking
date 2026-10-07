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

            // Cloud deployment override via Environment Variables (e.g. Render / Railway / Docker / Aiven)
            String envUrl = System.getenv("DB_URL");
            String envUser = System.getenv("DB_USER");
            String envPassword = System.getenv("DB_PASSWORD");

            if (envUrl != null && !envUrl.trim().isEmpty()) {
                envUrl = envUrl.trim();

                // If user pasted a full cloud URI e.g. mysql://user:pass@host:port/dbname
                if (envUrl.contains("@")) {
                    try {
                        String clean = envUrl;
                        if (clean.startsWith("jdbc:")) {
                            clean = clean.substring(5);
                        }
                        java.net.URI uri = new java.net.URI(clean);
                        String userInfo = uri.getUserInfo();
                        if (userInfo != null && userInfo.contains(":")) {
                            String[] parts = userInfo.split(":", 2);
                            if (envUser == null || envUser.trim().isEmpty()) {
                                username = parts[0];
                            }
                            if (envPassword == null || envPassword.trim().isEmpty()) {
                                password = parts[1];
                            }
                        }
                        String host = uri.getHost();
                        int port = uri.getPort();
                        String path = uri.getPath();
                        if (path == null || path.isEmpty() || "/".equals(path)) {
                            path = "/hospital_queue_db";
                        }
                        envUrl = "jdbc:mysql://" + host + (port > 0 ? (":" + port) : ":3306") + path;
                    } catch (Exception ex) {
                        // ignore and continue
                    }
                }

                if (envUrl.startsWith("mysql://")) {
                    envUrl = "jdbc:" + envUrl;
                } else if (!envUrl.startsWith("jdbc:mysql://") && (envUrl.contains("aivencloud.com") || envUrl.contains(":"))) {
                    envUrl = "jdbc:mysql://" + envUrl;
                }

                // If connecting to cloud/Aiven, ensure SSL settings and connection timeout
                if (envUrl.contains("aivencloud.com") && !envUrl.contains("verifyServerCertificate")) {
                    envUrl += (envUrl.contains("?") ? "&" : "?") + "verifyServerCertificate=false&useSSL=true&allowPublicKeyRetrieval=true&connectTimeout=5000&socketTimeout=15000";
                }
                url = envUrl;
            }
            if (envUser != null && !envUser.trim().isEmpty()) {
                username = envUser.trim();
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
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("[DBConnection Error] Failed to connect to URL: " + url + " with user: " + username + " - " + e.getMessage());
            throw e;
        }
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
