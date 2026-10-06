package com.hospital.model;

import java.sql.Date;
import java.sql.Timestamp;

public class QueueItem {
    private int queueId;
    private Integer appointmentId;
    private int patientId;
    private int doctorId;
    private int tokenNumber;
    private Date queueDate;
    private String status; // BOOKED, CHECKED_IN, WAITING, CALLED, IN_CONSULTATION, COMPLETED, CANCELLED, ABSENT
    private String bookingSource; // ONLINE, WALK_IN
    private Timestamp createdAt;
    private Timestamp checkedInAt;
    private Timestamp calledAt;
    private Timestamp consultationStartedAt;
    private Timestamp completedAt;

    // Additional display & computed fields
    private String patientName;
    private String patientPhone;
    private String doctorName;
    private String departmentName;
    private String roomNumber;
    private int currentToken;
    private int peopleAhead;
    private int estimatedWaitMinutes;
    private String notificationMessage;

    public QueueItem() {}

    public int getQueueId() { return queueId; }
    public void setQueueId(int queueId) { this.queueId = queueId; }

    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public int getTokenNumber() { return tokenNumber; }
    public void setTokenNumber(int tokenNumber) { this.tokenNumber = tokenNumber; }

    public Date getQueueDate() { return queueDate; }
    public void setQueueDate(Date queueDate) { this.queueDate = queueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBookingSource() { return bookingSource; }
    public void setBookingSource(String bookingSource) { this.bookingSource = bookingSource; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getCheckedInAt() { return checkedInAt; }
    public void setCheckedInAt(Timestamp checkedInAt) { this.checkedInAt = checkedInAt; }

    public Timestamp getCalledAt() { return calledAt; }
    public void setCalledAt(Timestamp calledAt) { this.calledAt = calledAt; }

    public Timestamp getConsultationStartedAt() { return consultationStartedAt; }
    public void setConsultationStartedAt(Timestamp consultationStartedAt) { this.consultationStartedAt = consultationStartedAt; }

    public Timestamp getCompletedAt() { return completedAt; }
    public void setCompletedAt(Timestamp completedAt) { this.completedAt = completedAt; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getCurrentToken() { return currentToken; }
    public void setCurrentToken(int currentToken) { this.currentToken = currentToken; }

    public int getPeopleAhead() { return peopleAhead; }
    public void setPeopleAhead(int peopleAhead) { this.peopleAhead = peopleAhead; }

    public int getEstimatedWaitMinutes() { return estimatedWaitMinutes; }
    public void setEstimatedWaitMinutes(int estimatedWaitMinutes) { this.estimatedWaitMinutes = estimatedWaitMinutes; }

    public String getNotificationMessage() { return notificationMessage; }
    public void setNotificationMessage(String notificationMessage) { this.notificationMessage = notificationMessage; }
}
