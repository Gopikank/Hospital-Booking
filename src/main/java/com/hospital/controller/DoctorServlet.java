package com.hospital.controller;

import com.hospital.dao.DoctorDAO;
import com.hospital.dao.DoctorScheduleDAO;
import com.hospital.dao.UserDAO;
import com.hospital.model.Doctor;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = {"/admin/doctors-action", "/api/doctors"})
public class DoctorServlet extends HttpServlet {

    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String deptIdStr = req.getParameter("departmentId");
        if (deptIdStr != null && !deptIdStr.isEmpty()) {
            List<Doctor> docs = doctorDAO.getDoctorsByDepartment(Integer.parseInt(deptIdStr));
            JsonUtil.sendSuccess(resp, "Doctors by department", docs);
        } else {
            List<Doctor> docs = doctorDAO.getAllDoctors();
            JsonUtil.sendSuccess(resp, "All doctors", docs);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");

        try {
            if ("status".equalsIgnoreCase(action)) {
                int doctorId = Integer.parseInt(req.getParameter("doctorId"));
                String status = req.getParameter("status");
                doctorDAO.updateDoctorStatus(doctorId, status);
                JsonUtil.sendSuccess(resp, "Doctor status updated.");
            } else if ("update".equalsIgnoreCase(action)) {
                int doctorId = Integer.parseInt(req.getParameter("doctorId"));
                int departmentId = Integer.parseInt(req.getParameter("departmentId"));
                String roomIdStr = req.getParameter("roomId");
                Integer roomId = (roomIdStr != null && !roomIdStr.isEmpty()) ? Integer.parseInt(roomIdStr) : null;
                String specialization = req.getParameter("specialization");
                String status = req.getParameter("status");
                int avgMinutes = Integer.parseInt(req.getParameter("averageMinutes"));

                Doctor d = new Doctor();
                d.setDoctorId(doctorId);
                d.setDepartmentId(departmentId);
                d.setRoomId(roomId);
                d.setSpecialization(specialization);
                d.setStatus(status);
                d.setAverageConsultationMinutes(avgMinutes);

                doctorDAO.updateDoctor(d);
                resp.sendRedirect(req.getContextPath() + "/admin/doctors.jsp?success=updated");
            } else {
                // Add new doctor
                String name = req.getParameter("fullName");
                String email = req.getParameter("email");
                String phone = req.getParameter("phone");
                String password = req.getParameter("password");
                int departmentId = Integer.parseInt(req.getParameter("departmentId"));
                String roomIdStr = req.getParameter("roomId");
                Integer roomId = (roomIdStr != null && !roomIdStr.isEmpty()) ? Integer.parseInt(roomIdStr) : null;
                String specialization = req.getParameter("specialization");
                int avgMinutes = Integer.parseInt(req.getParameter("averageMinutes"));

                if (userDAO.emailExists(email)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/doctors.jsp?error=email_exists");
                    return;
                }

                int userId = userDAO.createDoctorUser(name, email, phone, password);
                Doctor doc = new Doctor();
                doc.setUserId(userId);
                doc.setDepartmentId(departmentId);
                doc.setRoomId(roomId);
                doc.setSpecialization(specialization);
                doc.setStatus("AVAILABLE");
                doc.setAverageConsultationMinutes(avgMinutes > 0 ? avgMinutes : 5);

                doctorDAO.addDoctor(doc);
                resp.sendRedirect(req.getContextPath() + "/admin/doctors.jsp?success=added");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/doctors.jsp?error=" + e.getMessage());
        }
    }
}
