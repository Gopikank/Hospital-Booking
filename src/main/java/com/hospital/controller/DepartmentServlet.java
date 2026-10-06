package com.hospital.controller;

import com.hospital.dao.DepartmentDAO;
import com.hospital.model.Department;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/departments-action")
public class DepartmentServlet extends HttpServlet {

    private final DepartmentDAO departmentDAO = new DepartmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr != null) {
            Department d = departmentDAO.getDepartmentById(Integer.parseInt(idStr));
            JsonUtil.sendSuccess(resp, "Department fetched", d);
        } else {
            List<Department> list = departmentDAO.getAllDepartments();
            JsonUtil.sendSuccess(resp, "All departments", list);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String name = req.getParameter("departmentName");
        String description = req.getParameter("description");
        String status = req.getParameter("status");
        String idStr = req.getParameter("departmentId");

        try {
            if ("toggle".equalsIgnoreCase(action)) {
                int id = Integer.parseInt(idStr);
                departmentDAO.toggleStatus(id, status);
                JsonUtil.sendSuccess(resp, "Status updated.");
            } else if ("update".equalsIgnoreCase(action)) {
                int id = Integer.parseInt(idStr);
                Department d = new Department(id, name, description, status);
                departmentDAO.updateDepartment(d);
                resp.sendRedirect(req.getContextPath() + "/admin/departments.jsp?success=updated");
            } else {
                // Add new department
                Department d = new Department(0, name, description, status != null ? status : "ACTIVE");
                departmentDAO.addDepartment(d);
                resp.sendRedirect(req.getContextPath() + "/admin/departments.jsp?success=added");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/departments.jsp?error=" + e.getMessage());
        }
    }
}
