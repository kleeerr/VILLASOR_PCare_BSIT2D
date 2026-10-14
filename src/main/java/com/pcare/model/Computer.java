package com.pcare.model;

public class Computer {

    private int computerId;
    private String computerName;
    private String location;
    private String specifications;
    private String condition;
    private String dateRegistered;

    public Computer(int computerId, String computerName,
                    String location, String specifications,
                    String condition, String dateRegistered) {
        this.computerId = computerId;
        this.computerName = computerName;
        this.location = location;
        this.specifications = specifications;
        this.condition = condition;
        this.dateRegistered = dateRegistered;
    }

    public Computer(String computerName, String location,
                    String specifications, String condition,
                    String dateRegistered) {
        this.computerName = computerName;
        this.location = location;
        this.specifications = specifications;
        this.condition = condition;
        this.dateRegistered = dateRegistered;
    }

    public int getComputerId() {
        return computerId;
    }

    public String getComputerName() {
        return computerName;
    }

    public String getLocation() {
        return location;
    }

    public String getSpecifications() {
        return specifications;
    }

    public String getCondition() {
        return condition;
    }

    public String getDateRegistered() {
        return dateRegistered;
    }

    public void setComputerId(int computerId) {
        this.computerId = computerId;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSpecifications(String specifications) {
        this.specifications = specifications;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDateRegistered(String dateRegistered) {
        this.dateRegistered = dateRegistered;
    }
}