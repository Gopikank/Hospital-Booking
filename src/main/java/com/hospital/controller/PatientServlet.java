package com.hospital.controller;

import com.hospital.dao.AppointmentDAO;
import com.hospital.dao.UserDAO;
import com.hospital.model.Appointment;
import com.hospital.model.User;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/patients-action")
public class PatientServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String patientIdStr = req.getParameter("patientId");
        if (patientIdStr != null && !patientIdStr.isEmpty()) {
            int patientId = Integer.parseInt(patientIdStr);
            List<Appointment> history = appointmentDAO.getAppointmentsByPatient(patientId);
            JsonUtil.sendSuccess(resp, "Patient appointment history", history);
        } else {
            List<User> patients = userDAO.getAllPatients();
            JsonUtil.sendSuccess(resp, "All registered patients", patients);
        }
    }
}
