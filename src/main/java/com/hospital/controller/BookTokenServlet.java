package com.hospital.controller;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.QueueDAO;
import com.hospital.model.Doctor;
import com.hospital.model.QueueItem;
import com.hospital.util.CookieUtil;
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

@WebServlet("/patient/book-token")
public class BookTokenServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect(req.getContextPath() + "/patient/book-token.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Please log in to book a token.");
            return;
        }

        int patientId = (Integer) session.getAttribute("userId");
        String doctorIdStr = req.getParameter("doctorId");
        String dateStr = req.getParameter("date");
        String tokenStr = req.getParameter("tokenNumber");
        String departmentName = req.getParameter("departmentName");
        String rememberPref = req.getParameter("rememberPreference");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(req.getHeader("X-Requested-With")) ||
                         (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"));

        if (doctorIdStr == null || doctorIdStr.isEmpty() ||
            dateStr == null || dateStr.isEmpty() ||
            tokenStr == null || tokenStr.isEmpty()) {
            sendResponse(resp, isAjax, false, "Please select doctor, appointment date, and token number.", null);
            return;
        }

        try {
            int doctorId = Integer.parseInt(doctorIdStr);
            int tokenNumber = Integer.parseInt(tokenStr);
            Date date = Date.valueOf(dateStr);

            if (date.toLocalDate().isBefore(LocalDate.now())) {
                sendResponse(resp, isAjax, false, "Booking for past dates is strictly prohibited.", null);
                return;
            }

            QueueItem bookedItem = queueDAO.bookToken(patientId, doctorId, date, tokenNumber, "ONLINE");

            // Save user preferences to cookies as required in Section 6
            if (departmentName != null && !departmentName.trim().isEmpty()) {
                CookieUtil.setCookie(resp, "preferredDepartment", departmentName.trim());
            }
            Doctor doc = doctorDAO.getDoctorById(doctorId);
            if (doc != null) {
                CookieUtil.setCookie(resp, "preferredDoctorId", String.valueOf(doctorId));
                CookieUtil.setCookie(resp, "preferredDoctorName", doc.getDoctorName());
            }

            if (isAjax) {
                JsonUtil.sendSuccess(resp, "Token booked successfully!", bookedItem);
            } else {
                resp.sendRedirect(req.getContextPath() + "/patient/dashboard.jsp?booked=true&token=" + tokenNumber);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            sendResponse(resp, isAjax, false, e.getMessage(), null);
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(resp, isAjax, false, "Failed to book token: " + e.getMessage(), null);
        }
    }

    private void sendResponse(HttpServletResponse resp, boolean isAjax, boolean success, String message, Object data) throws IOException {
        if (isAjax) {
            if (success) {
                JsonUtil.sendSuccess(resp, message, data);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST, message);
            }
        } else {
            resp.sendRedirect("book-token.jsp?error=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8));
        }
    }
}
