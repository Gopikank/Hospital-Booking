package com.hospital.model;

import java.sql.Time;

public class DoctorSchedule {
    private int scheduleId;
    private int doctorId;
    private String dayOfWeek;
    private Time startTime;
    private Time endTime;
    private int maximumPatients;
    private String status; // ACTIVE, INACTIVE

    public DoctorSchedule() {}

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public String getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public Time getStartTime() { return startTime; }
    public void setStartTime(Time startTime) { this.startTime = startTime; }

    public Time getEndTime() { return endTime; }
    public void setEndTime(Time endTime) { this.endTime = endTime; }

    public int getMaximumPatients() { return maximumPatients; }
    public void setMaximumPatients(int maximumPatients) { this.maximumPatients = maximumPatients; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
