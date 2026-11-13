package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class JobServiceItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long jobServiceId;
    private Long jobId;
    private Long serviceTypeId;
    private BigDecimal labourCharge;
    private String notes;
    private String serviceName;

    public JobServiceItem() {}

    public JobServiceItem(Long jobServiceId, Long jobId, Long serviceTypeId, BigDecimal labourCharge, String notes, String serviceName) {
        this.jobServiceId = jobServiceId;
        this.jobId = jobId;
        this.serviceTypeId = serviceTypeId;
        this.labourCharge = labourCharge;
        this.notes = notes;
        this.serviceName = serviceName;
    }

    public Long getJobServiceId() { return jobServiceId; }
    public void setJobServiceId(Long jobServiceId) { this.jobServiceId = jobServiceId; }
    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }
    public Long getServiceTypeId() { return serviceTypeId; }
    public void setServiceTypeId(Long serviceTypeId) { this.serviceTypeId = serviceTypeId; }
    public BigDecimal getLabourCharge() { return labourCharge; }
    public void setLabourCharge(BigDecimal labourCharge) { this.labourCharge = labourCharge; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
}
