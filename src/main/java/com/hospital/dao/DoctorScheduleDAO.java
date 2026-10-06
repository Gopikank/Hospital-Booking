package com.hospital.dao;

import com.hospital.model.DoctorSchedule;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleDAO {

    public List<DoctorSchedule> getSchedulesByDoctor(int doctorId) {
        List<DoctorSchedule> list = new ArrayList<>();
        String sql = "SELECT schedule_id, doctor_id, day_of_week, start_time, end_time, maximum_patients, status " +
                     "FROM doctor_schedules WHERE doctor_id = ? AND status = 'ACTIVE'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            rs = ps.executeQuery();
            while (rs.next()) {
                DoctorSchedule s = new DoctorSchedule();
                s.setScheduleId(rs.getInt("schedule_id"));
                s.setDoctorId(rs.getInt("doctor_id"));
                s.setDayOfWeek(rs.getString("day_of_week"));
                s.setStartTime(rs.getTime("start_time"));
                s.setEndTime(rs.getTime("end_time"));
                s.setMaximumPatients(rs.getInt("maximum_patients"));
                s.setStatus(rs.getString("status"));
                list.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBConnection.close(rs, ps, conn);
        }
        return list;
    }

    public boolean isDoctorAvailableOnDay(int doctorId, String dayOfWeek) {
        String sql = "SELECT schedule_id FROM doctor_schedules WHERE doctor_id = ? AND day_of_week = ? AND status = 'ACTIVE'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBConnection.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, doctorId);
            ps.setString(2, dayOfWeek.toUpperCase());
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // Default fallback to available if schedule table is flexible
        } finally {
            DBConnection.close(rs, ps, conn);
        }
    }
}
