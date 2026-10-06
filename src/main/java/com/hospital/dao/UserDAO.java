package com.hospital.dao;

import com.hospital.model.User;
import com.hospital.util.PasswordUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String email, String plainPassword) {
        String sql = "SELECT user_id, full_name, email, phone, password_hash, role, created_at, updated_at " +
                     "FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim());
            rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(plainPassword, storedHash)) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setFullName(rs.getString("full_name"));
                    user.setEmail(rs.getString("email"));
                    user.setPhone(rs.getString("phone"));
                    user.setPasswordHash(storedHash);
                    user.setRole(rs.getString("role"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    user.setUpdatedAt(rs.getTimestamp("updated_at"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return null;
    }

    public int registerPatient(String fullName, String email, String phone, String plainPassword) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, 'PATIENT')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone.trim());
            ps.setString(4, PasswordUtil.hashPassword(plainPassword));

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

    public int createDoctorUser(String fullName, String email, String phone, String plainPassword) throws SQLException {
        String sql = "INSERT INTO users (full_name, email, phone, password_hash, role) VALUES (?, ?, ?, ?, 'DOCTOR')";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, fullName.trim());
            ps.setString(2, email.trim().toLowerCase());
            ps.setString(3, phone.trim());
            ps.setString(4, PasswordUtil.hashPassword(plainPassword));

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

    public boolean emailExists(String email) {
        String sql = "SELECT user_id FROM users WHERE email = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, email.trim().toLowerCase());
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBConnection.close(rs, ps, conn);
        }
    }

    public User findById(int userId) {
        String sql = "SELECT user_id, full_name, email, phone, role, created_at FROM users WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));
                user.setCreatedAt(rs.getTimestamp("created_at"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return null;
    }

    public List<User> getAllPatients() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, phone, role, created_at FROM users WHERE role = 'PATIENT' ORDER BY created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setPhone(rs.getString("phone"));
                u.setRole(rs.getString("role"));
                u.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean updateProfile(int userId, String fullName, String phone) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, phone = ? WHERE user_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, fullName.trim());
            ps.setString(2, phone.trim());
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }
}
