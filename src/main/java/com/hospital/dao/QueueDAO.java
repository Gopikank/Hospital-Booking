package com.hospital.dao;

import com.hospital.model.QueueItem;
import com.hospital.model.QueueSetting;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueueDAO {

    /**
     * Retrieves or initializes today's queue setting for a doctor.
     */
    public QueueSetting getOrCreateQueueSetting(Connection conn, int doctorId, Date queueDate) throws SQLException {
        String selectSql = "SELECT setting_id, doctor_id, queue_date, starting_token, maximum_tokens, current_token, average_consultation_minutes " +
                           "FROM queue_settings WHERE doctor_id = ? AND queue_date = ?";
        PreparedStatement ps = conn.prepareStatement(selectSql);
        ps.setInt(1, doctorId);
        ps.setDate(2, queueDate);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            QueueSetting qs = new QueueSetting();
            qs.setSettingId(rs.getInt("setting_id"));
            qs.setDoctorId(rs.getInt("doctor_id"));
            qs.setQueueDate(rs.getDate("queue_date"));
            qs.setStartingToken(rs.getInt("starting_token"));
            qs.setMaximumTokens(rs.getInt("maximum_tokens"));
            qs.setCurrentToken(rs.getInt("current_token"));
            qs.setAverageConsultationMinutes(rs.getInt("average_consultation_minutes"));
            rs.close();
            ps.close();
            return qs;
        }
        rs.close();
        ps.close();

        // Get average consultation minutes from doctor profile
        int avgMinutes = 5;
        String docSql = "SELECT average_consultation_minutes FROM doctors WHERE doctor_id = ?";
        PreparedStatement docPs = conn.prepareStatement(docSql);
        docPs.setInt(1, doctorId);
        ResultSet docRs = docPs.executeQuery();
        if (docRs.next()) {
            avgMinutes = docRs.getInt("average_consultation_minutes");
        }
        docRs.close();
        docPs.close();

        String insertSql = "INSERT INTO queue_settings (doctor_id, queue_date, starting_token, maximum_tokens, current_token, average_consultation_minutes) " +
                           "VALUES (?, ?, 1, 30, 0, ?)";
        PreparedStatement insertPs = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        insertPs.setInt(1, doctorId);
        insertPs.setDate(2, queueDate);
        insertPs.setInt(3, avgMinutes);
        insertPs.executeUpdate();
        ResultSet genRs = insertPs.getGeneratedKeys();

        QueueSetting qs = new QueueSetting();
        if (genRs.next()) {
            qs.setSettingId(genRs.getInt(1));
        }
        genRs.close();
        insertPs.close();

        qs.setDoctorId(doctorId);
        qs.setQueueDate(queueDate);
        qs.setStartingToken(1);
        qs.setMaximumTokens(30);
        qs.setCurrentToken(0);
        qs.setAverageConsultationMinutes(avgMinutes);
        return qs;
    }

    /**
     * Get available token numbers for a doctor on a specific date.
     */
    public List<Integer> getAvailableTokens(int doctorId, Date date) {
        List<Integer> available = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            QueueSetting setting = getOrCreateQueueSetting(conn, doctorId, date);
            int maxTokens = setting.getMaximumTokens();
            int currentToken = setting.getCurrentToken();

            // Find booked tokens that are not cancelled
            Set<Integer> bookedTokens = new HashSet<>();
            String sql = "SELECT token_number FROM queues WHERE doctor_id = ? AND queue_date = ? AND status != 'CANCELLED'";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, date);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bookedTokens.add(rs.getInt("token_number"));
                    }
                }
            }

            // Available tokens are those > currentToken (if date is today) and not currently booked
            boolean isToday = date.toLocalDate().isEqual(LocalDate.now());
            int start = isToday ? Math.max(1, currentToken + 1) : 1;
            for (int i = start; i <= maxTokens; i++) {
                if (!bookedTokens.contains(i)) {
                    available.add(i);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn);
        }
        return available;
    }

    /**
     * Books a token with full transaction safety and constraint checks.
     */
    public QueueItem bookToken(int patientId, int doctorId, Date date, int requestedToken, String source) throws Exception {
        LocalDate bookDate = date.toLocalDate();
        if (bookDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book an appointment for a past date.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Check doctor status
            String checkDocSql = "SELECT status FROM doctors WHERE doctor_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(checkDocSql)) {
                ps.setInt(1, doctorId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || "OFFLINE".equalsIgnoreCase(rs.getString("status"))) {
                        throw new IllegalStateException("Doctor is currently unavailable.");
                    }
                }
            }

            // 2. Check patient active bookings limit (max 3 active tokens)
            String checkLimitSql = "SELECT COUNT(*) FROM queues WHERE patient_id = ? AND queue_date >= CURDATE() AND status IN ('BOOKED', 'CHECKED_IN', 'WAITING', 'CALLED', 'IN_CONSULTATION')";
            try (PreparedStatement ps = conn.prepareStatement(checkLimitSql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) >= 3) {
                        throw new IllegalStateException("You already have the maximum permitted active bookings (3).");
                    }
                }
            }

            // 3. Ensure queue setting exists and capacity not exceeded
            QueueSetting setting = getOrCreateQueueSetting(conn, doctorId, date);
            if (requestedToken < 1 || requestedToken > setting.getMaximumTokens()) {
                throw new IllegalArgumentException("Requested token is outside daily capacity (1 to " + setting.getMaximumTokens() + ").");
            }
            if (bookDate.isEqual(LocalDate.now()) && requestedToken <= setting.getCurrentToken()) {
                throw new IllegalStateException("Requested token " + requestedToken + " has already been passed today.");
            }

            // 4. Lock and check if token is already booked for this doctor/date
            String checkTokenSql = "SELECT queue_id, status FROM queues WHERE doctor_id = ? AND queue_date = ? AND token_number = ? FOR UPDATE";
            try (PreparedStatement ps = conn.prepareStatement(checkTokenSql)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, date);
                ps.setInt(3, requestedToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String st = rs.getString("status");
                        if (!"CANCELLED".equalsIgnoreCase(st)) {
                            throw new IllegalStateException("Token " + requestedToken + " has already been booked by another patient.");
                        } else {
                            // If previously cancelled, delete or overwrite the cancelled row
                            String delSql = "DELETE FROM queues WHERE queue_id = ?";
                            try (PreparedStatement delPs = conn.prepareStatement(delSql)) {
                                delPs.setInt(1, rs.getInt("queue_id"));
                                delPs.executeUpdate();
                            }
                        }
                    }
                }
            }

            // 5. Create Appointment entry
            String apptSql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, booking_source, status) " +
                             "VALUES (?, ?, ?, '09:00:00', ?, 'BOOKED')";
            int appointmentId = -1;
            try (PreparedStatement ps = conn.prepareStatement(apptSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, patientId);
                ps.setInt(2, doctorId);
                ps.setDate(3, date);
                ps.setString(4, source != null ? source : "ONLINE");
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        appointmentId = rs.getInt(1);
                    }
                }
            }

            // 6. Create Queue entry
            // If WALK_IN, automatically enters WAITING status; if ONLINE, enters BOOKED status until check-in.
            String initialStatus = "WALK_IN".equalsIgnoreCase(source) ? "WAITING" : "BOOKED";
            String queueSql = "INSERT INTO queues (appointment_id, patient_id, doctor_id, token_number, queue_date, status, booking_source, created_at, checked_in_at) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), ?)";
            int queueId = -1;
            try (PreparedStatement ps = conn.prepareStatement(queueSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, appointmentId);
                ps.setInt(2, patientId);
                ps.setInt(3, doctorId);
                ps.setInt(4, requestedToken);
                ps.setDate(5, date);
                ps.setString(6, initialStatus);
                ps.setString(7, source != null ? source : "ONLINE");
                if ("WALK_IN".equalsIgnoreCase(source)) {
                    ps.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                } else {
                    ps.setNull(8, Types.TIMESTAMP);
                }
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        queueId = rs.getInt(1);
                    }
                }
            }

            conn.commit();
            return getQueueItemById(queueId);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                conn.close();
            }
        }
    }

    /**
     * Patient Check-in: transitions ONLINE booking from BOOKED to WAITING.
     */
    public boolean checkIn(int queueId, int patientId) throws SQLException {
        String sql = "UPDATE queues SET status = 'WAITING', checked_in_at = NOW() " +
                     "WHERE queue_id = ? AND patient_id = ? AND status = 'BOOKED'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                ps.setInt(2, patientId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    // Update linked appointment
                    String apptSql = "UPDATE appointments a JOIN queues q ON a.appointment_id = q.appointment_id " +
                                     "SET a.status = 'CHECKED_IN' WHERE q.queue_id = ?";
                    try (PreparedStatement apptPs = conn.prepareStatement(apptSql)) {
                        apptPs.setInt(1, queueId);
                        apptPs.executeUpdate();
                    }
                    return true;
                }
            }
        } finally {
            DBConnection.close(conn);
        }
        return false;
    }

    /**
     * Patient Cancel Token: only allowed if token is not CALLED or IN_CONSULTATION or COMPLETED.
     */
    public boolean cancelToken(int queueId, int patientId) throws SQLException {
        String sql = "UPDATE queues SET status = 'CANCELLED' " +
                     "WHERE queue_id = ? AND patient_id = ? AND status IN ('BOOKED', 'CHECKED_IN', 'WAITING')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                ps.setInt(2, patientId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    String apptSql = "UPDATE appointments a JOIN queues q ON a.appointment_id = q.appointment_id " +
                                     "SET a.status = 'CANCELLED' WHERE q.queue_id = ?";
                    try (PreparedStatement apptPs = conn.prepareStatement(apptSql)) {
                        apptPs.setInt(1, queueId);
                        apptPs.executeUpdate();
                    }
                    return true;
                }
            }
        } finally {
            DBConnection.close(conn);
        }
        return false;
    }

    /**
     * Doctor calls next patient:
     * Advances current queue to the next eligible patient in WAITING status.
     */
    public QueueItem callNextPatient(int doctorId, Date date) throws Exception {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            // Find next waiting or booked patient ordered by check-in priority then token_number ASC
            String findSql = "SELECT queue_id, token_number FROM queues " +
                             "WHERE doctor_id = ? AND queue_date = ? AND status IN ('WAITING', 'CHECKED_IN', 'BOOKED') " +
                             "ORDER BY FIELD(status, 'WAITING', 'CHECKED_IN', 'BOOKED'), token_number ASC LIMIT 1 FOR UPDATE";
            int nextQueueId = -1;
            int nextToken = -1;
            try (PreparedStatement ps = conn.prepareStatement(findSql)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, date);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        nextQueueId = rs.getInt("queue_id");
                        nextToken = rs.getInt("token_number");
                    }
                }
            }

            if (nextQueueId == -1) {
                conn.commit();
                return null; // No waiting patients
            }

            // Update queue item to CALLED
            String updateQueueSql = "UPDATE queues SET status = 'CALLED', called_at = NOW() WHERE queue_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateQueueSql)) {
                ps.setInt(1, nextQueueId);
                ps.executeUpdate();
            }

            // Update queue_settings current_token
            String updateSettingSql = "UPDATE queue_settings SET current_token = ? WHERE doctor_id = ? AND queue_date = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSettingSql)) {
                ps.setInt(1, nextToken);
                ps.setInt(2, doctorId);
                ps.setDate(3, date);
                ps.executeUpdate();
            }

            conn.commit();
            return getQueueItemById(nextQueueId);
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                conn.close();
            }
        }
    }

    /**
     * Start consultation: status becomes IN_CONSULTATION.
     */
    public boolean startConsultation(int queueId) throws SQLException {
        String sql = "UPDATE queues SET status = 'IN_CONSULTATION', consultation_started_at = NOW() " +
                     "WHERE queue_id = ? AND status = 'CALLED'";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                return ps.executeUpdate() > 0;
            }
        } finally {
            DBConnection.close(conn);
        }
    }

    /**
     * Complete consultation: status becomes COMPLETED.
     */
    public boolean completeConsultation(int queueId) throws SQLException {
        String sql = "UPDATE queues SET status = 'COMPLETED', completed_at = NOW() " +
                     "WHERE queue_id = ? AND status IN ('CALLED', 'IN_CONSULTATION')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    String apptSql = "UPDATE appointments a JOIN queues q ON a.appointment_id = q.appointment_id " +
                                     "SET a.status = 'COMPLETED' WHERE q.queue_id = ?";
                    try (PreparedStatement apptPs = conn.prepareStatement(apptSql)) {
                        apptPs.setInt(1, queueId);
                        apptPs.executeUpdate();
                    }
                    return true;
                }
            }
        } finally {
            DBConnection.close(conn);
        }
        return false;
    }

    /**
     * Mark patient as absent.
     */
    public boolean markAbsent(int queueId) throws SQLException {
        String sql = "UPDATE queues SET status = 'ABSENT' WHERE queue_id = ? AND status IN ('BOOKED', 'CHECKED_IN', 'WAITING', 'CALLED')";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    String apptSql = "UPDATE appointments a JOIN queues q ON a.appointment_id = q.appointment_id " +
                                     "SET a.status = 'ABSENT' WHERE q.queue_id = ?";
                    try (PreparedStatement apptPs = conn.prepareStatement(apptSql)) {
                        apptPs.setInt(1, queueId);
                        apptPs.executeUpdate();
                    }
                    return true;
                }
            }
        } finally {
            DBConnection.close(conn);
        }
        return false;
    }

    /**
     * Retrieves full queue item by ID with join details.
     */
    public QueueItem getQueueItemById(int queueId) {
        String sql = "SELECT q.*, u.full_name AS patient_name, u.phone AS patient_phone, " +
                     "       du.full_name AS doctor_name, dept.department_name, r.room_number, " +
                     "       qs.current_token, qs.average_consultation_minutes " +
                     "FROM queues q " +
                     "JOIN users u ON q.patient_id = u.user_id " +
                     "JOIN doctors doc ON q.doctor_id = doc.doctor_id " +
                     "JOIN users du ON doc.user_id = du.user_id " +
                     "JOIN departments dept ON doc.department_id = dept.department_id " +
                     "LEFT JOIN rooms r ON doc.room_id = r.room_id " +
                     "LEFT JOIN queue_settings qs ON (q.doctor_id = qs.doctor_id AND q.queue_date = qs.queue_date) " +
                     "WHERE q.queue_id = ?";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, queueId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapQueueItem(rs, conn);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn);
        }
        return null;
    }

    /**
     * Gets today's active queue item for a patient.
     */
    public QueueItem getTodayPatientQueue(int patientId) {
        String sql = "SELECT q.*, u.full_name AS patient_name, u.phone AS patient_phone, " +
                     "       du.full_name AS doctor_name, dept.department_name, r.room_number, " +
                     "       qs.current_token, qs.average_consultation_minutes " +
                     "FROM queues q " +
                     "JOIN users u ON q.patient_id = u.user_id " +
                     "JOIN doctors doc ON q.doctor_id = doc.doctor_id " +
                     "JOIN users du ON doc.user_id = du.user_id " +
                     "JOIN departments dept ON doc.department_id = dept.department_id " +
                     "LEFT JOIN rooms r ON doc.room_id = r.room_id " +
                     "LEFT JOIN queue_settings qs ON (q.doctor_id = qs.doctor_id AND q.queue_date = qs.queue_date) " +
                     "WHERE q.patient_id = ? AND (q.queue_date = CURDATE() OR q.status IN ('BOOKED', 'CHECKED_IN', 'WAITING', 'CALLED', 'IN_CONSULTATION')) " +
                     "ORDER BY (q.queue_date = CURDATE()) DESC, q.queue_date DESC, q.token_number DESC LIMIT 1";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, patientId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapQueueItem(rs, conn);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn);
        }
        return null;
    }

    /**
     * Retrieves the entire daily queue for a doctor.
     */
    public List<QueueItem> getDailyQueueForDoctor(int doctorId, Date date) {
        List<QueueItem> list = new ArrayList<>();
        String sql = "SELECT q.*, u.full_name AS patient_name, u.phone AS patient_phone, " +
                     "       du.full_name AS doctor_name, dept.department_name, r.room_number, " +
                     "       qs.current_token, qs.average_consultation_minutes " +
                     "FROM queues q " +
                     "JOIN users u ON q.patient_id = u.user_id " +
                     "JOIN doctors doc ON q.doctor_id = doc.doctor_id " +
                     "JOIN users du ON doc.user_id = du.user_id " +
                     "JOIN departments dept ON doc.department_id = dept.department_id " +
                     "LEFT JOIN rooms r ON doc.room_id = r.room_id " +
                     "LEFT JOIN queue_settings qs ON (q.doctor_id = qs.doctor_id AND q.queue_date = qs.queue_date) " +
                     "WHERE q.doctor_id = ? AND q.queue_date = ? " +
                     "ORDER BY q.token_number ASC";
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, doctorId);
                ps.setDate(2, date);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapQueueItem(rs, conn));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn);
        }
        return list;
    }

    /**
     * Helper to map ResultSet and compute peopleAhead, estimatedWaitMinutes, and notificationMessage.
     */
    private QueueItem mapQueueItem(ResultSet rs, Connection conn) throws SQLException {
        QueueItem item = new QueueItem();
        item.setQueueId(rs.getInt("queue_id"));
        item.setAppointmentId((Integer) rs.getObject("appointment_id"));
        item.setPatientId(rs.getInt("patient_id"));
        item.setDoctorId(rs.getInt("doctor_id"));
        item.setTokenNumber(rs.getInt("token_number"));
        item.setQueueDate(rs.getDate("queue_date"));
        item.setStatus(rs.getString("status"));
        item.setBookingSource(rs.getString("booking_source"));
        item.setCreatedAt(rs.getTimestamp("created_at"));
        item.setCheckedInAt(rs.getTimestamp("checked_in_at"));
        item.setCalledAt(rs.getTimestamp("called_at"));
        item.setConsultationStartedAt(rs.getTimestamp("consultation_started_at"));
        item.setCompletedAt(rs.getTimestamp("completed_at"));

        item.setPatientName(rs.getString("patient_name"));
        item.setPatientPhone(rs.getString("patient_phone"));
        item.setDoctorName(rs.getString("doctor_name"));
        item.setDepartmentName(rs.getString("department_name"));
        item.setRoomNumber(rs.getString("room_number") != null ? rs.getString("room_number") : "101");

        int currentToken = rs.getInt("current_token");
        int avgMinutes = rs.getInt("average_consultation_minutes");
        if (avgMinutes <= 0) avgMinutes = 5;
        item.setCurrentToken(currentToken);

        // Calculate people ahead: number of active waiting patients ahead with smaller token_number
        int peopleAhead = 0;
        if ("WAITING".equalsIgnoreCase(item.getStatus()) || "BOOKED".equalsIgnoreCase(item.getStatus()) || "CHECKED_IN".equalsIgnoreCase(item.getStatus())) {
            String countSql = "SELECT COUNT(*) FROM queues " +
                              "WHERE doctor_id = ? AND queue_date = ? AND token_number < ? AND status IN ('WAITING', 'CHECKED_IN')";
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setInt(1, item.getDoctorId());
                ps.setDate(2, item.getQueueDate());
                ps.setInt(3, item.getTokenNumber());
                try (ResultSet countRs = ps.executeQuery()) {
                    if (countRs.next()) {
                        peopleAhead = countRs.getInt(1);
                    }
                }
            }
        }
        item.setPeopleAhead(peopleAhead);
        item.setEstimatedWaitMinutes(peopleAhead * avgMinutes);

        // Formulate real-time notification message
        if ("CALLED".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("YOUR TOKEN IS CALLED. PLEASE PROCEED TO ROOM " + item.getRoomNumber() + ".");
        } else if ("IN_CONSULTATION".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("You are currently in consultation with Dr. " + item.getDoctorName() + ".");
        } else if ("COMPLETED".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("Consultation completed. Thank you!");
        } else if ("ABSENT".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("Token was marked absent. Please contact the front desk.");
        } else if ("CANCELLED".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("This token was cancelled.");
        } else if ("BOOKED".equalsIgnoreCase(item.getStatus())) {
            item.setNotificationMessage("Online token booked. Please click CHECK IN after arriving at the hospital.");
        } else {
            // WAITING or CHECKED_IN
            if (peopleAhead == 0 || (currentToken + 1 == item.getTokenNumber())) {
                item.setNotificationMessage("YOU ARE NEXT");
            } else {
                item.setNotificationMessage("Please wait. " + peopleAhead + " patient" + (peopleAhead > 1 ? "s are" : " is") + " ahead of you.");
            }
        }

        return item;
    }

    /**
     * Admin dashboard overall statistics.
     */
    public Map<String, Object> getAdminStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();

            // Total patients today
            String q1 = "SELECT COUNT(*) AS total_patients, " +
                        "SUM(CASE WHEN booking_source = 'ONLINE' THEN 1 ELSE 0 END) AS online_bookings, " +
                        "SUM(CASE WHEN booking_source = 'WALK_IN' THEN 1 ELSE 0 END) AS walkin_tokens, " +
                        "SUM(CASE WHEN status IN ('WAITING', 'CHECKED_IN') THEN 1 ELSE 0 END) AS waiting_patients, " +
                        "SUM(CASE WHEN status = 'IN_CONSULTATION' THEN 1 ELSE 0 END) AS in_consultation, " +
                        "SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed, " +
                        "SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) AS cancelled, " +
                        "SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) AS absent " +
                        "FROM queues WHERE queue_date = CURDATE()";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(q1)) {
                if (rs.next()) {
                    stats.put("totalPatients", rs.getInt("total_patients"));
                    stats.put("onlineBookings", rs.getInt("online_bookings"));
                    stats.put("walkinTokens", rs.getInt("walkin_tokens"));
                    stats.put("waitingPatients", rs.getInt("waiting_patients"));
                    stats.put("inConsultation", rs.getInt("in_consultation"));
                    stats.put("completed", rs.getInt("completed"));
                    stats.put("cancelled", rs.getInt("cancelled"));
                    stats.put("absent", rs.getInt("absent"));
                }
            }

            // Active doctors count
            String q2 = "SELECT COUNT(*) FROM doctors WHERE status != 'OFFLINE'";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(q2)) {
                if (rs.next()) {
                    stats.put("activeDoctors", rs.getInt(1));
                }
            }

            // Department-wise waiting breakdown
            List<Map<String, Object>> deptStats = new ArrayList<>();
            String q3 = "SELECT dept.department_name, " +
                        "COUNT(CASE WHEN q.status IN ('WAITING', 'CHECKED_IN') THEN 1 END) AS waiting_count, " +
                        "COUNT(q.queue_id) AS total_count " +
                        "FROM departments dept " +
                        "LEFT JOIN doctors doc ON dept.department_id = doc.department_id " +
                        "LEFT JOIN queues q ON doc.doctor_id = q.doctor_id AND q.queue_date = CURDATE() " +
                        "WHERE dept.status = 'ACTIVE' " +
                        "GROUP BY dept.department_id, dept.department_name " +
                        "ORDER BY dept.department_name";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(q3)) {
                while (rs.next()) {
                    Map<String, Object> dMap = new HashMap<>();
                    dMap.put("departmentName", rs.getString("department_name"));
                    dMap.put("waitingCount", rs.getInt("waiting_count"));
                    dMap.put("totalCount", rs.getInt("total_count"));
                    deptStats.add(dMap);
                }
            }
            stats.put("departmentStats", deptStats);

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(conn);
        }
        return stats;
    }
}
