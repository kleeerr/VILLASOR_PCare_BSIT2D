package com.pcare.model;

public class ActivityLog {

    private int logId;
    private int userId;
    private String action;
    private String dateTime;

    public ActivityLog(int logId, int userId,
                       String action, String dateTime) {
        this.logId = logId;
        this.userId = userId;
        this.action = action;
        this.dateTime = dateTime;
    }

    public ActivityLog(int userId, String action, String dateTime) {
        this.userId = userId;
        this.action = action;
        this.dateTime = dateTime;
    }

    public int getLogId() {
        return logId;
    }

    public int getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }
}