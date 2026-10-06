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

@WebServlet("/doctor/call-next")
public class CallNextPatientServlet extends HttpServlet {

    private final QueueDAO queueDAO = new QueueDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized. Please log in as doctor.");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        Doctor doctor = doctorDAO.getDoctorByUserId(userId);
        if (doctor == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN, "User is not mapped to an active doctor profile.");
            return;
        }

        try {
            Date today = Date.valueOf(LocalDate.now());
            QueueItem calledPatient = queueDAO.callNextPatient(doctor.getDoctorId(), today);

            if (calledPatient != null) {
                JsonUtil.sendSuccess(resp, "Token " + calledPatient.getTokenNumber() + " called successfully!", calledPatient);
            } else {
                JsonUtil.sendError(resp, HttpServletResponse.SC_OK, "No waiting patients in the queue for today.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error calling next patient: " + e.getMessage());
        }
    }
}
