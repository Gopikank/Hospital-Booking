package com.hospital.controller;

import com.hospital.dao.QueueDAO;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/complete-consultation")
public class CompleteConsultationServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String queueIdStr = req.getParameter("queueId");
        if (queueIdStr == null || queueIdStr.isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Queue ID is required.");
            return;
        }

        try {
            int queueId = Integer.parseInt(queueIdStr);
            boolean completed = queueDAO.completeConsultation(queueId);
            if (completed) {
                JsonUtil.sendSuccess(resp, "Consultation completed successfully.", queueDAO.getQueueItemById(queueId));
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not complete consultation. Invalid patient status.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error completing consultation: " + e.getMessage());
        }
    }
}
