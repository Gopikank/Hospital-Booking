package com.hospital.controller;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.model.Doctor;
import com.hospital.model.QueueItem;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@WebServlet("/doctor/api/queue")
public class DoctorQueueServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized. Please log in.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        Doctor doctor = doctorDAO.getDoctorByUserId(userId);
        if (doctor == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "Doctor profile not found.");
            return;
        }

        try {
            Date today = Date.valueOf(LocalDate.now());
            List<QueueItem> dailyQueue = queueDAO.getDailyQueueForDoctor(doctor.getDoctorId(), today);

            QueueItem currentServing = null;
            QueueItem nextPatient = null;
            List<QueueItem> waitingList = new ArrayList<>();
            int completedCount = 0;
            int absentCount = 0;
            int waitingCount = 0;

            for (QueueItem item : dailyQueue) {
                String st = item.getStatus();
                if ("CALLED".equalsIgnoreCase(st) || "IN_CONSULTATION".equalsIgnoreCase(st)) {
                    currentServing = item;
                } else if ("WAITING".equalsIgnoreCase(st) || "CHECKED_IN".equalsIgnoreCase(st) || "BOOKED".equalsIgnoreCase(st)) {
                    waitingList.add(item);
                    waitingCount++;
                    if (nextPatient == null) {
                        nextPatient = item;
                    }
                } else if ("COMPLETED".equalsIgnoreCase(st)) {
                    completedCount++;
                } else if ("ABSENT".equalsIgnoreCase(st)) {
                    absentCount++;
                }
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("doctor", doctor);
            responseData.put("currentServing", currentServing);
            responseData.put("nextPatient", nextPatient);
            responseData.put("waitingList", waitingList);
            responseData.put("waitingCount", waitingCount);
            responseData.put("completedCount", completedCount);
            responseData.put("absentCount", absentCount);
            responseData.put("totalPatients", dailyQueue.size());
            responseData.put("allQueueItems", dailyQueue);
            responseData.put("lastUpdated", LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a")));

            JsonUtil.sendSuccess(resp, "Doctor queue retrieved.", responseData);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving doctor queue: " + e.getMessage());
        }
    }
}
