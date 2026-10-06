package com.hospital.controller;

import com.hospital.dao.QueueDAO;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/admin/dashboard-stats")
public class AdminDashboardServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Map<String, Object> stats = queueDAO.getAdminStatistics();
            JsonUtil.sendSuccess(resp, "Admin statistics retrieved.", stats);
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error retrieving admin statistics: " + e.getMessage());
        }
    }
}
