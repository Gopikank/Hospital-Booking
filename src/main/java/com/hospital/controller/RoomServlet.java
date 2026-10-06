package com.hospital.controller;

import com.hospital.dao.RoomDAO;
import com.hospital.model.Room;
import com.hospital.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/admin/rooms-action")
public class RoomServlet extends HttpServlet {

    private final RoomDAO roomDAO = new RoomDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Room> rooms = roomDAO.getAllRooms();
        JsonUtil.sendSuccess(resp, "Rooms list", rooms);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        String roomNumber = req.getParameter("roomNumber");
        String departmentIdStr = req.getParameter("departmentId");
        String status = req.getParameter("status");
        String roomIdStr = req.getParameter("roomId");

        try {
            Integer deptId = (departmentIdStr != null && !departmentIdStr.isEmpty()) ? Integer.parseInt(departmentIdStr) : null;
            if ("update".equalsIgnoreCase(action)) {
                int id = Integer.parseInt(roomIdStr);
                Room r = new Room(id, roomNumber, deptId, status);
                roomDAO.updateRoom(r);
                resp.sendRedirect(req.getContextPath() + "/admin/rooms.jsp?success=updated");
            } else {
                // Add room
                Room r = new Room(0, roomNumber, deptId, status != null ? status : "AVAILABLE");
                roomDAO.addRoom(r);
                resp.sendRedirect(req.getContextPath() + "/admin/rooms.jsp?success=added");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendRedirect(req.getContextPath() + "/admin/rooms.jsp?error=" + e.getMessage());
        }
    }
}
