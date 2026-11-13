package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Part implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long partId;
    private String partNo;
    private String name;
    private String category;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private Integer stockQty;
    private Integer reorderLevel;
    private LocalDateTime createdAt;

    public Part() {}

    public Part(Long partId, String partNo, String name, String category, BigDecimal unitPrice, BigDecimal costPrice, Integer stockQty, Integer reorderLevel, LocalDateTime createdAt) {
        this.partId = partId;
        this.partNo = partNo;
        this.name = name;
        this.category = category;
        this.unitPrice = unitPrice;
        this.costPrice = costPrice;
        this.stockQty = stockQty;
        this.reorderLevel = reorderLevel;
        this.createdAt = createdAt;
    }

    public boolean isLowStock() {
        return stockQty != null && reorderLevel != null && stockQty <= reorderLevel;
    }

    public Long getPartId() { return partId; }
    public void setPartId(Long partId) { this.partId = partId; }
    public String getPartNo() { return partNo; }
    public void setPartNo(String partNo) { this.partNo = partNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    public Integer getStockQty() { return stockQty; }
    public void setStockQty(Integer stockQty) { this.stockQty = stockQty; }
    public Integer getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(Integer reorderLevel) { this.reorderLevel = reorderLevel; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
