package com.wms.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class JobOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long jobId;
    private Long vehicleId;
    private Long advisorId;
    private Long mechanicId;
    private LocalDateTime dateIn;
    private LocalDateTime dateOut;
    private Integer mileageIn;
    private String complaint;
    private String diagnosis;
    private String status; // 'PENDING', 'IN_PROGRESS', 'AWAITING_PARTS', 'COMPLETED', 'INVOICED', 'CANCELLED'
    private LocalDate nextServiceDate;
    private Integer nextServiceKm;
    private LocalDateTime createdAt;

    // Joined Presentation Details
    private String plateNo;
    private String vehicleInfo;
    private String customerName;
    private String customerPhone;
    private String advisorName;
    private String mechanicName;

    public JobOrder() {}

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }
    public Long getAdvisorId() { return advisorId; }
    public void setAdvisorId(Long advisorId) { this.advisorId = advisorId; }
    public Long getMechanicId() { return mechanicId; }
    public void setMechanicId(Long mechanicId) { this.mechanicId = mechanicId; }
    public LocalDateTime getDateIn() { return dateIn; }
    public void setDateIn(LocalDateTime dateIn) { this.dateIn = dateIn; }
    public LocalDateTime getDateOut() { return dateOut; }
    public void setDateOut(LocalDateTime dateOut) { this.dateOut = dateOut; }
    public Integer getMileageIn() { return mileageIn; }
    public void setMileageIn(Integer mileageIn) { this.mileageIn = mileageIn; }
    public String getComplaint() { return complaint; }
    public void setComplaint(String complaint) { this.complaint = complaint; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getNextServiceDate() { return nextServiceDate; }
    public void setNextServiceDate(LocalDate nextServiceDate) { this.nextServiceDate = nextServiceDate; }
    public Integer getNextServiceKm() { return nextServiceKm; }
    public void setNextServiceKm(Integer nextServiceKm) { this.nextServiceKm = nextServiceKm; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPlateNo() { return plateNo; }
    public void setPlateNo(String plateNo) { this.plateNo = plateNo; }
    public String getVehicleInfo() { return vehicleInfo; }
    public void setVehicleInfo(String vehicleInfo) { this.vehicleInfo = vehicleInfo; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
    public String getMechanicName() { return mechanicName; }
    public void setMechanicName(String mechanicName) { this.mechanicName = mechanicName; }
}
