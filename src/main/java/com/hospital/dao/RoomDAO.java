package com.hospital.dao;

import com.hospital.model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.department_id, r.status, d.department_name " +
                     "FROM rooms r " +
                     "LEFT JOIN departments d ON r.department_id = d.department_id " +
                     "ORDER BY r.room_number";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Room r = new Room();
                r.setRoomId(rs.getInt("room_id"));
                r.setRoomNumber(rs.getString("room_number"));
                r.setDepartmentId((Integer) rs.getObject("department_id"));
                r.setStatus(rs.getString("status"));
                r.setDepartmentName(rs.getString("department_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.room_id, r.room_number, r.department_id, r.status, d.department_name " +
                     "FROM rooms r " +
                     "LEFT JOIN departments d ON r.department_id = d.department_id " +
                     "WHERE r.status = 'AVAILABLE' " +
                     "ORDER BY r.room_number";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Room r = new Room();
                r.setRoomId(rs.getInt("room_id"));
                r.setRoomNumber(rs.getString("room_number"));
                r.setDepartmentId((Integer) rs.getObject("department_id"));
                r.setStatus(rs.getString("status"));
                r.setDepartmentName(rs.getString("department_name"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public int addRoom(Room r) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, department_id, status) VALUES (?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, r.getRoomNumber());
            if (r.getDepartmentId() != null && r.getDepartmentId() > 0) {
                ps.setInt(2, r.getDepartmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, r.getStatus() != null ? r.getStatus() : "AVAILABLE");
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

    public boolean updateRoom(Room r) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, department_id = ?, status = ? WHERE room_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, r.getRoomNumber());
            if (r.getDepartmentId() != null && r.getDepartmentId() > 0) {
                ps.setInt(2, r.getDepartmentId());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, r.getStatus());
            ps.setInt(4, r.getRoomId());
            return ps.executeUpdate() > 0;
        } finally {
            DBConnection.close(ps, conn);
        }
    }
}
