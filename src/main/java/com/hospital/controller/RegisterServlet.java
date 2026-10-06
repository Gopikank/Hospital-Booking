package com.hospital.controller;

import com.hospital.dao.UserDAO;
import com.hospital.model.User;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/auth/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/auth/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String phone = req.getParameter("phone");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (fullName == null || fullName.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            phone == null || phone.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=missing_fields", "All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=password_mismatch", "Passwords do not match.");
            return;
        }

        if (password.length() < 6) {
            handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=short_password", "Password must be at least 6 characters.");
            return;
        }

        if (userDAO.emailExists(email.trim())) {
            handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=email_exists", "An account with this email already exists.");
            return;
        }

        try {
            int newUserId = userDAO.registerPatient(fullName.trim(), email.trim(), phone.trim(), password);
            if (newUserId > 0) {
                // Auto login after registration
                HttpSession session = req.getSession(true);
                session.setAttribute("userId", newUserId);
                session.setAttribute("role", "PATIENT");
                session.setAttribute("name", fullName.trim());
                session.setAttribute("email", email.trim().toLowerCase());

                if (isAjax) {
                    JsonUtil.sendSuccess(resp, "Registration successful!", req.getContextPath() + "/patient/dashboard.jsp");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/patient/dashboard.jsp?registered=true");
                }
            } else {
                handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=failed", "Registration failed. Please try again.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            handleError(resp, isAjax, req.getContextPath() + "/auth/register.jsp?error=server_error", "An error occurred during registration.");
        }
    }

    private void handleError(HttpServletResponse resp, boolean isAjax, String redirectUrl, String message) throws IOException {
        if (isAjax) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, message);
        } else {
            resp.sendRedirect(redirectUrl);
        }
    }
}
