package com.wms.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Vehicle implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long vehicleId;
    private Long customerId;
    private String plateNo;
    private String make;
    private String model;
    private Integer year;
    private Integer currentMileage;
    private LocalDateTime createdAt;
    private String customerName;
    private String customerPhone;

    public Vehicle() {}

    public Vehicle(Long vehicleId, Long customerId, String plateNo, String make, String model, Integer year, Integer currentMileage, LocalDateTime createdAt) {
        this.vehicleId = vehicleId;
        this.customerId = customerId;
        this.plateNo = plateNo;
        this.make = make;
        this.model = model;
        this.year = year;
        this.currentMileage = currentMileage;
        this.createdAt = createdAt;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public Integer getCurrentMileage() { return currentMileage; }
    public void setCurrentMileage(Integer currentMileage) { this.currentMileage = currentMileage; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getDisplayTitle() {
        return plateNo + " - " + year + " " + make + " " + model;
    }
}
