package com.hospital.dao;

import com.hospital.model.Doctor;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    private Doctor mapResultSet(ResultSet rs) throws SQLException {
        Doctor d = new Doctor();
        d.setDoctorId(rs.getInt("doctor_id"));
        d.setUserId(rs.getInt("user_id"));
        d.setDepartmentId(rs.getInt("department_id"));
        d.setRoomId((Integer) rs.getObject("room_id"));
        d.setSpecialization(rs.getString("specialization"));
        d.setStatus(rs.getString("status"));
        d.setAverageConsultationMinutes(rs.getInt("average_consultation_minutes"));
        d.setCreatedAt(rs.getTimestamp("created_at"));

        d.setDoctorName(rs.getString("doctor_name"));
        d.setEmail(rs.getString("email"));
        d.setPhone(rs.getString("phone"));
        d.setDepartmentName(rs.getString("department_name"));
        d.setRoomNumber(rs.getString("room_number"));
        return d;
    }

    private static final String BASE_QUERY =
            "SELECT doc.doctor_id, doc.user_id, doc.department_id, doc.room_id, doc.specialization, " +
            "       doc.status, doc.average_consultation_minutes, doc.created_at, " +
            "       u.full_name AS doctor_name, u.email, u.phone, " +
            "       dept.department_name, r.room_number " +
            "FROM doctors doc " +
            "JOIN users u ON doc.user_id = u.user_id " +
            "JOIN departments dept ON doc.department_id = dept.department_id " +
            "LEFT JOIN rooms r ON doc.room_id = r.room_id ";

    public List<Doctor> getAllDoctors() {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY + "ORDER BY u.full_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Doctor> getDoctorsByDepartment(int departmentId) {
        List<Doctor> list = new ArrayList<>();
        String sql = BASE_QUERY + "WHERE doc.department_id = ? AND doc.status != 'OFFLINE' ORDER BY u.full_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, departmentId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Doctor getDoctorById(int doctorId) {
        String sql = BASE_QUERY + "WHERE doc.doctor_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return null;
    }

    public Doctor getDoctorByUserId(int userId) {
        String sql = BASE_QUERY + "WHERE doc.user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return null;
    }

    public int addDoctor(Doctor doc) throws SQLException {
        String sql = "INSERT INTO doctors (user_id, department_id, room_id, specialization, status, average_consultation_minutes) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, doc.getUserId());
            ps.setInt(2, doc.getDepartmentId());
            if (doc.getRoomId() != null && doc.getRoomId() > 0) {
                ps.setInt(3, doc.getRoomId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setString(4, doc.getSpecialization());
            ps.setString(5, doc.getStatus() != null ? doc.getStatus() : "AVAILABLE");
            ps.setInt(6, doc.getAverageConsultationMinutes() > 0 ? doc.getAverageConsultationMinutes() : 5);
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

    public boolean updateDoctor(Doctor doc) throws SQLException {
        String sql = "UPDATE doctors SET department_id = ?, room_id = ?, specialization = ?, " +
                     "status = ?, average_consultation_minutes = ? WHERE doctor_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doc.getDepartmentId());
            if (doc.getRoomId() != null && doc.getRoomId() > 0) {
                ps.setInt(2, doc.getRoomId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, doc.getSpecialization());
            ps.setString(4, doc.getStatus());
            ps.setInt(5, doc.getAverageConsultationMinutes());
            ps.setInt(6, doc.getDoctorId());
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }

    public boolean updateDoctorStatus(int doctorId, String status) throws SQLException {
        String sql = "UPDATE doctors SET status = ? WHERE doctor_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, doctorId);
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }
}
