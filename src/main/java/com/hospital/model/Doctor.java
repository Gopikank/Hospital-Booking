package com.hospital.model;

import java.sql.Timestamp;

public class Doctor {
    private int doctorId;
    private int userId;
    private int departmentId;
    private Integer roomId;
    private String specialization;
    private String status; // AVAILABLE, BUSY, OFFLINE
    private int averageConsultationMinutes;
    private Timestamp createdAt;

    // Joined fields
    private String doctorName;
    private String email;
    private String phone;
    private String departmentName;
    private String roomNumber;

    public Doctor() {}

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAverageConsultationMinutes() { return averageConsultationMinutes; }
    public void setAverageConsultationMinutes(int averageConsultationMinutes) { this.averageConsultationMinutes = averageConsultationMinutes; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
}
