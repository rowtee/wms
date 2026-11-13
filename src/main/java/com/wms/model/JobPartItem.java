package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class JobPartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long jobPartId;
    private Long jobId;
    private Long partId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private String partNo;
    private String partName;

    public JobPartItem() {}

    public JobPartItem(Long jobPartId, Long jobId, Long partId, Integer quantity, BigDecimal unitPrice, String partNo, String partName) {
        this.jobPartId = jobPartId;
        this.jobId = jobId;
        this.partId = partId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.partNo = partNo;
        this.partName = partName;
    }

    public BigDecimal getSubtotal() {
        if (unitPrice == null || quantity == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getJobPartId() { return jobPartId; }
    public void setJobPartId(Long jobPartId) { this.jobPartId = jobPartId; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public String getPartNo() { return partNo; }
    public void setPartNo(String partNo) { this.partNo = partNo; }
    public String getPartName() { return partName; }
    public void setPartName(String partName) { this.partName = partName; }
}
