package com.hospital.controller;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.model.Doctor;
import com.hospital.model.QueueItem;
import com.hospital.model.QueueSetting;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/queue-status")
public class QueueStatusServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queueIdStr = req.getParameter("queueId");
        String doctorIdStr = req.getParameter("doctorId");

        HttpSession session = req.getSession(false);
        Integer sessionUserId = (session != null) ? (Integer) session.getAttribute("userId") : null;
        String sessionRole = (session != null) ? (String) session.getAttribute("role") : null;

        Map<String, Object> data = new HashMap<>();
        String timeStamp = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
        data.put("lastUpdated", timeStamp);

        try {
            // Case 1: Specific queue item requested or patient logged in
            QueueItem patientQueue = null;
            if (queueIdStr != null && !queueIdStr.isEmpty()) {
                patientQueue = queueDAO.getQueueItemById(Integer.parseInt(queueIdStr));
            } else if ("PATIENT".equals(sessionRole) && sessionUserId != null) {
                patientQueue = queueDAO.getTodayPatientQueue(sessionUserId);
            }

            if (patientQueue != null) {
                data.put("hasActiveToken", true);
                data.put("queueId", patientQueue.getQueueId());
                data.put("tokenNumber", patientQueue.getTokenNumber());
                data.put("currentToken", patientQueue.getCurrentToken());
                data.put("peopleAhead", patientQueue.getPeopleAhead());
                data.put("estimatedWaitMinutes", patientQueue.getEstimatedWaitMinutes());
                data.put("status", patientQueue.getStatus());
                data.put("doctorName", patientQueue.getDoctorName());
                data.put("departmentName", patientQueue.getDepartmentName());
                data.put("roomNumber", patientQueue.getRoomNumber());
                data.put("bookingSource", patientQueue.getBookingSource());
                data.put("notificationMessage", patientQueue.getNotificationMessage());
                data.put("isCalled", "CALLED".equalsIgnoreCase(patientQueue.getStatus()));
                data.put("isNext", patientQueue.getPeopleAhead() <= 1 && "WAITING".equalsIgnoreCase(patientQueue.getStatus()));
            } else {
                data.put("hasActiveToken", false);
            }

            // Case 2: General doctor live queue requested (e.g. For dedicated live queue screen)
            if (doctorIdStr != null && !doctorIdStr.isEmpty()) {
                int doctorId = Integer.parseInt(doctorIdStr);
                Doctor doc = doctorDAO.getDoctorById(doctorId);
                if (doc != null) {
                    data.put("doctorId", doc.getDoctorId());
                    data.put("doctorName", doc.getDoctorName());
                    data.put("departmentName", doc.getDepartmentName());
                    data.put("roomNumber", doc.getRoomNumber());

                    Date today = Date.valueOf(LocalDate.now());
                    try (Connection conn = com.hospital.dao.DBConnection.getConnection()) {
                        QueueSetting qs = queueDAO.getOrCreateQueueSetting(conn, doctorId, today);
                        data.put("currentToken", qs.getCurrentToken());
                    }
                    data.put("dailyQueue", queueDAO.getDailyQueueForDoctor(doctorId, today));
                }
            }

            JsonUtil.sendSuccess(resp, "Queue status retrieved.", data);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving queue status: " + e.getMessage());
        }
    }
}
