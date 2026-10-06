package com.hospital.model;

import java.sql.Date;

public class QueueSetting {
    private int settingId;
    private int doctorId;
    private Date queueDate;
    private int startingToken;
    private int maximumTokens;
    private int currentToken;
    private int averageConsultationMinutes;

    public QueueSetting() {}

    public int getSettingId() { return settingId; }
    public void setSettingId(int settingId) { this.settingId = settingId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public Date getQueueDate() { return queueDate; }
    public void setQueueDate(Date queueDate) { this.queueDate = queueDate; }

    public int getStartingToken() { return startingToken; }
    public void setStartingToken(int startingToken) { this.startingToken = startingToken; }

    public int getMaximumTokens() { return maximumTokens; }
    public void setMaximumTokens(int maximumTokens) { this.maximumTokens = maximumTokens; }

    public int getCurrentToken() { return currentToken; }
    public void setCurrentToken(int currentToken) { this.currentToken = currentToken; }

    public int getAverageConsultationMinutes() { return averageConsultationMinutes; }
    public void setAverageConsultationMinutes(int averageConsultationMinutes) { this.averageConsultationMinutes = averageConsultationMinutes; }
}
