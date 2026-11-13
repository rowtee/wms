package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class ServiceType implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long serviceTypeId;
    private String name;
    private BigDecimal labourCharge;
    private Integer intervalKm;
    private Integer intervalMonths;
    private String description;

    public ServiceType() {}

    public ServiceType(Long serviceTypeId, String name, BigDecimal labourCharge, Integer intervalKm, Integer intervalMonths, String description) {
        this.serviceTypeId = serviceTypeId;
        this.name = name;
        this.labourCharge = labourCharge;
        this.intervalKm = intervalKm;
        this.intervalMonths = intervalMonths;
        this.description = description;
    }

    public Long getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Long serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getLabourCharge() { return labourCharge; }
    public void setLabourCharge(BigDecimal labourCharge) { this.labourCharge = labourCharge; }
    public Integer getIntervalKm() { return intervalKm; }
    public void setIntervalKm(Integer intervalKm) { this.intervalKm = intervalKm; }
    public Integer getIntervalMonths() { return intervalMonths; }
    public void setIntervalMonths(Integer intervalMonths) { this.intervalMonths = intervalMonths; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
