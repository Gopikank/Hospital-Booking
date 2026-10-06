package com.hospital.controller;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.model.Doctor;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/available-tokens")
public class AvailableTokensServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String doctorIdStr = req.getParameter("doctorId");
        String dateStr = req.getParameter("date");

        if (doctorIdStr == null || doctorIdStr.isEmpty() || dateStr == null || dateStr.isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Doctor ID and date parameters are required.");
            return;
        }

        try {
            int doctorId = Integer.parseInt(doctorIdStr);
            Date date = Date.valueOf(dateStr);

            if (date.toLocalDate().isBefore(LocalDate.now())) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Booking is not allowed for past dates.");
                return;
            }

            Doctor doctor = doctorDAO.getDoctorById(doctorId);
            if (doctor == null) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_NOT_FOUND, "Doctor not found.");
                return;
            }

            List<Integer> availableTokens = queueDAO.getAvailableTokens(doctorId, date);

            Map<String, Object> result = new HashMap<>();
            result.put("doctorId", doctor.getDoctorId());
            result.put("doctorName", doctor.getDoctorName());
            result.put("departmentName", doctor.getDepartmentName());
            result.put("roomNumber", doctor.getRoomNumber());
            result.put("doctorStatus", doctor.getStatus());
            result.put("averageMinutes", doctor.getAverageConsultationMinutes());
            result.put("date", dateStr);
            result.put("availableTokens", availableTokens);
            result.put("totalAvailable", availableTokens.size());

            JsonUtil.sendSuccess(resp, "Available tokens fetched successfully.", result);
        } catch (IllegalArgumentException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Invalid date format. Use YYYY-MM-DD.");
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error fetching tokens: " + e.getMessage());
        }
    }
}
