package com.example.modulethreeassignmentkalebward;

public class DataItem {
    private long id = -1; // Database record ID
    private String name;
    private String quantity;
    private String date;

    public DataItem(String name, String quantity, String date) {
        this.name = name;
        this.quantity = quantity;
        this.date = date;
    }

    // Getters and setters

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}