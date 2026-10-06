package com.hospital.controller;

import com.hospital.dao.QueueDAO;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/patient/cancel-token")
public class CancelTokenServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized. Please log in.");
            return;
        }

        int patientId = (Integer) session.getAttribute("userId");
        String queueIdStr = req.getParameter("queueId");

        if (queueIdStr == null || queueIdStr.isEmpty()) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Queue ID is required.");
            return;
        }

        try {
            int queueId = Integer.parseInt(queueIdStr);
            boolean cancelled = queueDAO.cancelToken(queueId, patientId);

            if (cancelled) {
                JsonUtil.sendSuccess(resp, "Token cancelled successfully.");
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Cannot cancel token. Tokens that have already been called or completed cannot be cancelled.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error cancelling token: " + e.getMessage());
        }
    }
}
