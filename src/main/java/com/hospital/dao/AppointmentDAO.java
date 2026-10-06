package com.hospital.dao;

import com.hospital.model.Appointment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public int createAppointment(Appointment appt) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, booking_source, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, appt.getPatientId());
            ps.setInt(2, appt.getDoctorId());
            ps.setDate(3, appt.getAppointmentDate());
            ps.setTime(4, appt.getAppointmentTime() != null ? appt.getAppointmentTime() : Time.valueOf("09:00:00"));
            ps.setString(5, appt.getBookingSource() != null ? appt.getBookingSource() : "ONLINE");
            ps.setString(6, appt.getStatus() != null ? appt.getStatus() : "BOOKED");

            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return -1;
    }

    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
                     "       a.booking_source, a.status, a.created_at, " +
                     "       u.full_name AS doctor_name, dept.department_name, r.room_number, q.token_number " +
                     "FROM appointments a " +
                     "JOIN doctors doc ON a.doctor_id = doc.doctor_id " +
                     "JOIN users u ON doc.user_id = u.user_id " +
                     "JOIN departments dept ON doc.department_id = dept.department_id " +
                     "LEFT JOIN rooms r ON doc.room_id = r.room_id " +
                     "LEFT JOIN queues q ON a.appointment_id = q.appointment_id " +
                     "WHERE a.patient_id = ? " +
                     "ORDER BY a.appointment_date DESC, a.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, patientId);
            rs = ps.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setAppointmentId(rs.getInt("appointment_id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getDate("appointment_date"));
                a.setAppointmentTime(rs.getTime("appointment_time"));
                a.setBookingSource(rs.getString("booking_source"));
                a.setStatus(rs.getString("status"));
                a.setCreatedAt(rs.getTimestamp("created_at"));

                a.setDoctorName(rs.getString("doctor_name"));
                a.setDepartmentName(rs.getString("department_name"));
                a.setRoomNumber(rs.getString("room_number"));
                a.setTokenNumber((Integer) rs.getObject("token_number"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Appointment> getAllAppointments() {
        List<Appointment> list = new ArrayList<>();
        String sql = "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
                     "       a.booking_source, a.status, a.created_at, " +
                     "       pu.full_name AS patient_name, " +
                     "       du.full_name AS doctor_name, dept.department_name, r.room_number, q.token_number " +
                     "FROM appointments a " +
                     "JOIN users pu ON a.patient_id = pu.user_id " +
                     "JOIN doctors doc ON a.doctor_id = doc.doctor_id " +
                     "JOIN users du ON doc.user_id = du.user_id " +
                     "JOIN departments dept ON doc.department_id = dept.department_id " +
                     "LEFT JOIN rooms r ON doc.room_id = r.room_id " +
                     "LEFT JOIN queues q ON a.appointment_id = q.appointment_id " +
                     "ORDER BY a.appointment_date DESC, a.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Appointment a = new Appointment();
                a.setAppointmentId(rs.getInt("appointment_id"));
                a.setPatientId(rs.getInt("patient_id"));
                a.setDoctorId(rs.getInt("doctor_id"));
                a.setAppointmentDate(rs.getDate("appointment_date"));
                a.setAppointmentTime(rs.getTime("appointment_time"));
                a.setBookingSource(rs.getString("booking_source"));
                a.setStatus(rs.getString("status"));
                a.setCreatedAt(rs.getTimestamp("created_at"));

                a.setPatientName(rs.getString("patient_name"));
                a.setDoctorName(rs.getString("doctor_name"));
                a.setDepartmentName(rs.getString("department_name"));
                a.setRoomNumber(rs.getString("room_number"));
                a.setTokenNumber((Integer) rs.getObject("token_number"));
                list.add(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean updateStatus(int appointmentId, String status) throws SQLException {
        String sql = "UPDATE appointments SET status = ? WHERE appointment_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }
}
