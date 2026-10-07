package com.hospital.dao;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class DBConnection {

    private static String url;
    private static String username;
    private static String password;

    private static volatile boolean useFallback = false;
    private static final Object INIT_LOCK = new Object();
    private static volatile boolean schemaInitialized = false;

    private static final String FALLBACK_URL = "jdbc:h2:./data/hospital_queue_db;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;AUTO_SERVER=TRUE";
    private static final String FALLBACK_USER = "sa";
    private static final String FALLBACK_PASS = "";

    static {
        initConfiguration();
    }

    public static void initConfiguration() {
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
            System.err.println("[DBConnection Init Warning] " + e.getMessage());
        }
    }

    public static void setExplicitConnection(String newUrl, String newUser, String newPass) {
        url = newUrl;
        username = newUser;
        password = newPass;
        useFallback = false;
        checkedPrimaryReachability = false;
    }

    private static volatile boolean checkedPrimaryReachability = false;

    private static boolean isPrimaryReachable() {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        try {
            String temp = url.trim();
            if (temp.startsWith("jdbc:mysql://")) {
                temp = temp.substring("jdbc:mysql://".length());
            } else if (temp.startsWith("mysql://")) {
                temp = temp.substring("mysql://".length());
            }
            int slashIdx = temp.indexOf('/');
            String hostPort = (slashIdx > 0) ? temp.substring(0, slashIdx) : temp;
            int qIdx = hostPort.indexOf('?');
            if (qIdx > 0) {
                hostPort = hostPort.substring(0, qIdx);
            }
            String host = hostPort;
            int port = 3306;
            if (hostPort.contains(":")) {
                String[] parts = hostPort.split(":", 2);
                host = parts[0];
                try {
                    port = Integer.parseInt(parts[1]);
                } catch (NumberFormatException ignored) {
                }
            }

            java.net.InetAddress addr = java.net.InetAddress.getByName(host);
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(addr, port), 2000);
                return true;
            } catch (Exception socketEx) {
                System.err.println("[DBConnection] Primary DB host " + host + ":" + port + " unreachable: " + socketEx.getMessage());
                return false;
            }
        } catch (Exception dnsEx) {
            System.err.println("[DBConnection] Primary DB DNS resolution failed: " + dnsEx.getMessage());
            return false;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (useFallback) {
            return getFallbackConnection();
        }

        if (!checkedPrimaryReachability) {
            checkedPrimaryReachability = true;
            if (!isPrimaryReachable()) {
                System.err.println("[DBConnection Alert] Primary MySQL is unreachable. Activating Embedded Database Engine fallback.");
                useFallback = true;
                return getFallbackConnection();
            }
        }

        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("[DBConnection Alert] Primary MySQL connection failed (" + url + "): " + e.getMessage());
            System.err.println("[DBConnection] Activating Embedded Database Engine fallback to guarantee 100% uptime...");
            useFallback = true;
            return getFallbackConnection();
        }
    }

    private static Connection getFallbackConnection() throws SQLException {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("H2 driver not available for fallback", e);
        }

        File dir = new File("./data");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Connection conn = DriverManager.getConnection(FALLBACK_URL, FALLBACK_USER, FALLBACK_PASS);
        if (!schemaInitialized) {
            synchronized (INIT_LOCK) {
                if (!schemaInitialized) {
                    initFallbackSchema(conn);
                    schemaInitialized = true;
                }
            }
        }
        return conn;
    }

    private static void initFallbackSchema(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 1. users
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL UNIQUE, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "role ENUM('PATIENT', 'DOCTOR', 'ADMIN') NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 2. departments
            stmt.execute("CREATE TABLE IF NOT EXISTS departments (" +
                    "department_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "department_name VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 3. rooms
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "room_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "room_number VARCHAR(20) NOT NULL UNIQUE, " +
                    "department_id INT, " +
                    "status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE')");

            // 4. doctors
            stmt.execute("CREATE TABLE IF NOT EXISTS doctors (" +
                    "doctor_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "user_id INT NOT NULL UNIQUE, " +
                    "department_id INT NOT NULL, " +
                    "room_id INT, " +
                    "specialization VARCHAR(100), " +
                    "status ENUM('AVAILABLE', 'BUSY', 'OFFLINE') DEFAULT 'AVAILABLE', " +
                    "average_consultation_minutes INT DEFAULT 5, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 5. appointments
            stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                    "appointment_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "patient_id INT NOT NULL, " +
                    "doctor_id INT NOT NULL, " +
                    "appointment_date DATE NOT NULL, " +
                    "appointment_time TIME DEFAULT '09:00:00', " +
                    "booking_source ENUM('ONLINE', 'WALK_IN') NOT NULL DEFAULT 'ONLINE', " +
                    "status ENUM('BOOKED', 'CHECKED_IN', 'CANCELLED', 'COMPLETED', 'ABSENT') DEFAULT 'BOOKED', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

            // 6. queues
            stmt.execute("CREATE TABLE IF NOT EXISTS queues (" +
                    "queue_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "appointment_id INT, " +
                    "patient_id INT NOT NULL, " +
                    "doctor_id INT NOT NULL, " +
                    "token_number INT NOT NULL, " +
                    "queue_date DATE NOT NULL, " +
                    "status ENUM('BOOKED', 'CHECKED_IN', 'WAITING', 'CALLED', 'IN_CONSULTATION', 'COMPLETED', 'CANCELLED', 'ABSENT') DEFAULT 'BOOKED', " +
                    "booking_source ENUM('ONLINE', 'WALK_IN') NOT NULL DEFAULT 'ONLINE', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "checked_in_at TIMESTAMP NULL, " +
                    "called_at TIMESTAMP NULL, " +
                    "consultation_started_at TIMESTAMP NULL, " +
                    "completed_at TIMESTAMP NULL)");

            // 7. queue_settings
            stmt.execute("CREATE TABLE IF NOT EXISTS queue_settings (" +
                    "setting_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "doctor_id INT NOT NULL, " +
                    "queue_date DATE NOT NULL, " +
                    "starting_token INT DEFAULT 1, " +
                    "maximum_tokens INT DEFAULT 30, " +
                    "current_token INT DEFAULT 0, " +
                    "average_consultation_minutes INT DEFAULT 5)");

            // 8. doctor_schedules
            stmt.execute("CREATE TABLE IF NOT EXISTS doctor_schedules (" +
                    "schedule_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "doctor_id INT NOT NULL, " +
                    "day_of_week ENUM('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY') NOT NULL, " +
                    "start_time TIME NOT NULL DEFAULT '09:00:00', " +
                    "end_time TIME NOT NULL DEFAULT '17:00:00', " +
                    "maximum_patients INT DEFAULT 30, " +
                    "status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE')");

            // Seed departments
            stmt.execute("MERGE INTO departments (department_id, department_name, description, status) KEY (department_id) VALUES " +
                    "(1, 'General Medicine', 'Primary healthcare, internal medicine, fever and routine consultations', 'ACTIVE'), " +
                    "(2, 'Cardiology', 'Heart health, cardiac checkups, hypertension and ECG services', 'ACTIVE'), " +
                    "(3, 'Orthopedics', 'Bone, joint, spine care, fractures and orthopedic surgery consultations', 'ACTIVE'), " +
                    "(4, 'Pediatrics', 'Comprehensive childcare, vaccinations and pediatric emergencies', 'ACTIVE')");

            // Seed rooms
            stmt.execute("MERGE INTO rooms (room_id, room_number, department_id, status) KEY (room_id) VALUES " +
                    "(1, '101', 1, 'AVAILABLE'), " +
                    "(2, '102', 2, 'AVAILABLE'), " +
                    "(3, '103', 3, 'AVAILABLE'), " +
                    "(4, '104', 4, 'AVAILABLE')");

            // Seed users (Admin, Doctors, Demo Patients)
            stmt.execute("MERGE INTO users (user_id, full_name, email, phone, password_hash, role) KEY (user_id) VALUES " +
                    "(1, 'Hospital Administrator', 'admin@hospital.com', '9876543210', 'NYHEOUNQ5H9dt6H/qM7/5Q==:voJSDBRO2/vl9leO8tT9LUnLaCZQ5JS30P5fm1uzk8E=', 'ADMIN'), " +
                    "(2, 'Dr. Kumar', 'kumar@hospital.com', '9876543211', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'), " +
                    "(3, 'Dr. Priya', 'priya@hospital.com', '9876543212', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'), " +
                    "(4, 'Dr. Arun', 'arun@hospital.com', '9876543213', 'lYbBuC2IdVLpvINO0/aJ8g==:V5Y2DNRKC5inQVTQ02zCfCMZ2IDkKwBdKLJ1dCYQjf4=', 'DOCTOR'), " +
                    "(5, 'Arun', 'arun.patient@gmail.com', '9123456780', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT'), " +
                    "(6, 'Meena Ramesh', 'meena.patient@gmail.com', '9123456781', 'NUOaKXUp1lIkaRwVkGDsMw==:JAO2ndchT6z28Ueok7iDSWdL7ZYKKP1tuO9InrSu4tU=', 'PATIENT')");

            // Seed doctors
            stmt.execute("MERGE INTO doctors (doctor_id, user_id, department_id, room_id, specialization, status, average_consultation_minutes) KEY (doctor_id) VALUES " +
                    "(1, 2, 1, 1, 'General Physician & Diabetologist', 'AVAILABLE', 5), " +
                    "(2, 3, 2, 2, 'Interventional Cardiologist', 'AVAILABLE', 8), " +
                    "(3, 4, 3, 3, 'Joint Replacement & Spine Specialist', 'AVAILABLE', 7)");

            // Seed schedules
            String[] days = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
            int schedId = 1;
            for (int d = 1; d <= 3; d++) {
                for (String day : days) {
                    stmt.execute("MERGE INTO doctor_schedules (schedule_id, doctor_id, day_of_week, start_time, end_time, maximum_patients, status) KEY (schedule_id) VALUES " +
                            "(" + schedId++ + ", " + d + ", '" + day + "', '08:00:00', '20:00:00', 30, 'ACTIVE')");
                }
            }

            // Seed today's queue setting for Dr. Kumar
            stmt.execute("MERGE INTO queue_settings (setting_id, doctor_id, queue_date, starting_token, maximum_tokens, current_token, average_consultation_minutes) KEY (setting_id) VALUES " +
                    "(1, 1, CURRENT_DATE(), 1, 30, 20, 5)");

            // Seed initial queue items for Dr. Kumar
            stmt.execute("MERGE INTO appointments (appointment_id, patient_id, doctor_id, appointment_date, appointment_time, booking_source, status) KEY (appointment_id) VALUES " +
                    "(1, 6, 1, CURRENT_DATE(), '09:30:00', 'ONLINE', 'CHECKED_IN'), " +
                    "(2, 5, 1, CURRENT_DATE(), '10:10:00', 'ONLINE', 'CHECKED_IN')");

            stmt.execute("MERGE INTO queues (queue_id, appointment_id, patient_id, doctor_id, token_number, queue_date, status, booking_source) KEY (queue_id) VALUES " +
                    "(1, 1, 6, 1, 21, CURRENT_DATE(), 'WAITING', 'ONLINE'), " +
                    "(2, 2, 5, 1, 22, CURRENT_DATE(), 'WAITING', 'ONLINE')");

            System.out.println("[DBConnection] Embedded database initialized with complete schema and seed data.");
        } catch (SQLException e) {
            System.err.println("[DBConnection Init Error] " + e.getMessage());
            e.printStackTrace();
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
