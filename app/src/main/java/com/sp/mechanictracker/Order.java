package com.sp.mechanictracker;

import java.util.List;

public class Order {
    public String getBicycleDetails() {
        return BicycleDetails;
    }

    public void setBicycleDetails(String bicycleDetails) {
        BicycleDetails = bicycleDetails;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDeliveryMethod() {
        return DeliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        DeliveryMethod = deliveryMethod;
    }

    public List<String> getImages() {
        return Images;
    }

    public void setImages(List<String> images) {
        Images = images;
    }

    public String getMechanic() {
        return Mechanic;
    }

    public void setMechanic(String mechanic) {
        Mechanic = mechanic;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getPID() {
        return PID;
    }

    public void setPID(String PID) {
        this.PID = PID;
    }

    public String getPackage() {
        return Package;
    }

    public void setPackage(String aPackage) {
        Package = aPackage;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    private String BicycleDetails;
    private String Date;
    private String DeliveryMethod;
    private List<String> Images;
    private String Mechanic;
    private String Notes;

    public String getOrderID() {
        return OrderID;
    }

    public void setOrderID(String orderID) {
        OrderID = orderID;
    }

    private String OrderID;
    private String PID;
    private String Package;
    private String Phone;
    private String Status;
    private String Time;


    @Override
    public String toString() {
        return "Order{" +
                "bicycleDetails='" + BicycleDetails + '\'' +
                ", date='" + Date + '\'' +
                ", deliveryMethod='" + DeliveryMethod + '\'' +
                ", images=" + Images +
                ", mechanic='" + Mechanic + '\'' +
                ", notes='" + Notes + '\'' +
                ", orderID='" + OrderID + '\'' +
                ", pid='" + PID + '\'' +
                ", package='" + Package + '\'' +
                ", phone='" + Phone + '\'' +
                ", status='" + Status + '\'' +
                ", time='" + Time + '\'' +
                '}';
    }

    // Constructors, getters, setters, and toString() method

    // You can generate getters and setters automatically in your IDE for brevity.
}
