
package com.pcare.model;

public class ActivityLog {

    private int logId;
    private int userId;
    private String actionType;
    private String description;
    private String timestamp;

    public ActivityLog(int logId, int userId,
                       String actionType, String description,
                       String timestamp) {
        this.logId = logId;
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
    }

    public ActivityLog(int userId, String actionType,
                       String description, String timestamp) {
        this.userId = userId;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Compatibility constructor for existing PCare code
    public ActivityLog(int logId, int userId,
                       String action, String dateTime) {
        this(logId, userId, action, "", dateTime);
    }

    public ActivityLog(int userId, String action, String dateTime) {
        this(userId, action, "", dateTime);
    }

    public int getLogId() {
        return logId;
    }

    public int getUserId() {
        return userId;
    }

    public String getActionType() {
        return actionType;
    }

    public String getDescription() {
        return description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Compatibility getters
    public String getAction() {
        return actionType;
    }

    public String getDateTime() {
        return timestamp;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Compatibility setters
    public void setAction(String action) {
        this.actionType = action;
    }

    public void setDateTime(String dateTime) {
        this.timestamp = dateTime;
    }
}
