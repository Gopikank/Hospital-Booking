package com.hospital.model;

public class Room {
    private int roomId;
    private String roomNumber;
    private Integer departmentId;
    private String departmentName; // for join views
    private String status; // AVAILABLE, OCCUPIED, MAINTENANCE

    public Room() {}

    public Room(int roomId, String roomNumber, Integer departmentId, String status) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.departmentId = departmentId;
        this.status = status;
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public Integer getDepartmentId() { return departmentId; }
    public void setDepartmentId(Integer departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
