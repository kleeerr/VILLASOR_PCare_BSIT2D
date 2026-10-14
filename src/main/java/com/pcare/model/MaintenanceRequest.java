package com.pcare.model;

public class MaintenanceRequest {

    private int requestId;
    private int computerId;
    private int reportedBy;
    private Integer assignedTo;
    private String problemDescription;
    private String dateReported;
    private String status;
    private String repairNotes;
    private String dateCompleted;

    public MaintenanceRequest(int requestId, int computerId,
                              int reportedBy, Integer assignedTo,
                              String problemDescription,
                              String dateReported, String status,
                              String repairNotes, String dateCompleted) {
        this.requestId = requestId;
        this.computerId = computerId;
        this.reportedBy = reportedBy;
        this.assignedTo = assignedTo;
        this.problemDescription = problemDescription;
        this.dateReported = dateReported;
        this.status = status;
        this.repairNotes = repairNotes;
        this.dateCompleted = dateCompleted;
    }

    public MaintenanceRequest(int computerId, int reportedBy,
                              String problemDescription,
                              String dateReported) {
        this.computerId = computerId;
        this.reportedBy = reportedBy;
        this.problemDescription = problemDescription;
        this.dateReported = dateReported;
        this.status = "Pending";
    }

    public int getRequestId() {
        return requestId;
    }

    public int getComputerId() {
        return computerId;
    }

    public int getReportedBy() {
        return reportedBy;
    }

    public Integer getAssignedTo() {
        return assignedTo;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public String getDateReported() {
        return dateReported;
    }

    public String getStatus() {
        return status;
    }

    public String getRepairNotes() {
        return repairNotes;
    }

    public String getDateCompleted() {
        return dateCompleted;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public void setReportedBy(int reportedBy) {
        this.reportedBy = reportedBy;
    }

    public void setAssignedTo(Integer assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public void setDateReported(String dateReported) {
        this.dateReported = dateReported;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRepairNotes(String repairNotes) {
        this.repairNotes = repairNotes;
    }

    public void setDateCompleted(String dateCompleted) {
        this.dateCompleted = dateCompleted;
    }
}