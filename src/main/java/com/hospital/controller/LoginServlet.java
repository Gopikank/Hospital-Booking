package com.hospital.controller;

import com.hospital.dao.UserDAO;
import com.hospital.model.User;
import com.hospital.util.CookieUtil;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/auth/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/auth/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String remember = req.getParameter("remember");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            if (isAjax) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, "Email and password are required.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=missing_fields");
            }
            return;
        }

        User user = userDAO.authenticate(email.trim(), password);
        if (user != null) {
            // Invalidate any existing session and establish fresh session
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("role", user.getRole());
            session.setAttribute("name", user.getFullName());
            session.setAttribute("email", user.getEmail());

            if ("on".equalsIgnoreCase(remember) || "true".equalsIgnoreCase(remember)) {
                CookieUtil.setCookie(resp, "rememberEmail", user.getEmail());
            }

            String targetUrl = req.getContextPath();
            switch (user.getRole()) {
                case "PATIENT" -> targetUrl += "/patient/dashboard.jsp";
                case "DOCTOR" -> targetUrl += "/doctor/dashboard.jsp";
                case "ADMIN" -> targetUrl += "/admin/dashboard.jsp";
                default -> targetUrl += "/index.jsp";
            }

            if (isAjax) {
                Map<String, Object> data = new HashMap<>();
                data.put("role", user.getRole());
                data.put("name", user.getFullName());
                data.put("redirectUrl", targetUrl);
                JsonUtil.sendSuccess(resp, "Login successful.", data);
            } else {
                resp.sendRedirect(targetUrl);
            }
        } else {
            if (isAjax) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Invalid email or password.");
            } else {
                resp.sendRedirect(req.getContextPath() + "/auth/login.jsp?error=invalid_credentials");
            }
        }
    }
}
