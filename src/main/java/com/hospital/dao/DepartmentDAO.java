package com.hospital.dao;

import com.hospital.model.Department;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDAO {

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, department_name, description, status, created_at FROM departments ORDER BY department_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
                d.setStatus(rs.getString("status"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Department> getActiveDepartments() {
        List<Department> list = new ArrayList<>();
        String sql = "SELECT department_id, department_name, description, status, created_at FROM departments WHERE status = 'ACTIVE' ORDER BY department_name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
                d.setStatus(rs.getString("status"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public Department getDepartmentById(int id) {
        String sql = "SELECT department_id, department_name, description, status, created_at FROM departments WHERE department_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                Department d = new Department();
                d.setDepartmentId(rs.getInt("department_id"));
                d.setDepartmentName(rs.getString("department_name"));
                d.setDescription(rs.getString("description"));
                d.setStatus(rs.getString("status"));
                d.setCreatedAt(rs.getTimestamp("created_at"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return null;
    }

    public int addDepartment(Department d) throws SQLException {
        String sql = "INSERT INTO departments (department_name, description, status) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, d.getDepartmentName());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getStatus() != null ? d.getStatus() : "ACTIVE");
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

    public boolean updateDepartment(Department d) throws SQLException {
        String sql = "UPDATE departments SET department_name = ?, description = ?, status = ? WHERE department_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, d.getDepartmentName());
            ps.setString(2, d.getDescription());
            ps.setString(3, d.getStatus());
            ps.setInt(4, d.getDepartmentId());
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }

    public boolean toggleStatus(int id, String status) throws SQLException {
        String sql = "UPDATE departments SET status = ? WHERE department_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }
}
