package com.hospital.controller;

import com.hospital.dao.QueueDAO;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/doctor/mark-absent")
public class MarkAbsentServlet extends HttpServlet {

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
            boolean marked = queueDAO.markAbsent(queueId);
            if (marked) {
                JsonUtil.sendSuccess(resp, "Patient marked as absent.", queueDAO.getQueueItemById(queueId));
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Could not mark patient absent.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error marking absent: " + e.getMessage());
        }
    }
}
