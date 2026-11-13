package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MonthlyReport implements Serializable {
    private static final long serialVersionUID = 1L;

    private int year;
    private int month;
    private int totalOrders;
    private int completedOrders;
    private int invoicedOrders;
    private BigDecimal totalSales;
    private BigDecimal totalLaborRevenue;
    private BigDecimal totalPartsRevenue;
    private BigDecimal totalTaxCollected;
    private int totalVehiclesServiced;
    private int newCustomersRegistered;
    
    // Performance breakdown
    private List<Map<String, Object>> mechanicPerformance;
    private List<Map<String, Object>> popularServices;

    public MonthlyReport() {}

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }
    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }
    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }
    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }
    public int getInvoicedOrders() { return invoicedOrders; }
    public void setInvoicedOrders(int invoicedOrders) { this.invoicedOrders = invoicedOrders; }
    public BigDecimal getTotalSales() { return totalSales; }
    public void setTotalSales(BigDecimal totalSales) { this.totalSales = totalSales; }
    public BigDecimal getTotalLaborRevenue() { return totalLaborRevenue; }
    public void setTotalLaborRevenue(BigDecimal totalLaborRevenue) { this.totalLaborRevenue = totalLaborRevenue; }
    public BigDecimal getTotalPartsRevenue() { return totalPartsRevenue; }
    public void setTotalPartsRevenue(BigDecimal totalPartsRevenue) { this.totalPartsRevenue = totalPartsRevenue; }
    public BigDecimal getTotalTaxCollected() { return totalTaxCollected; }
    public void setTotalTaxCollected(BigDecimal totalTaxCollected) { this.totalTaxCollected = totalTaxCollected; }
    public int getTotalVehiclesServiced() { return totalVehiclesServiced; }
    public void setTotalVehiclesServiced(int totalVehiclesServiced) { this.totalVehiclesServiced = totalVehiclesServiced; }
    public int getNewCustomersRegistered() { return newCustomersRegistered; }
    public void setNewCustomersRegistered(int newCustomersRegistered) { this.newCustomersRegistered = newCustomersRegistered; }

    public List<Map<String, Object>> getMechanicPerformance() { return mechanicPerformance; }
    public void setMechanicPerformance(List<Map<String, Object>> mechanicPerformance) { this.mechanicPerformance = mechanicPerformance; }
    public List<Map<String, Object>> getPopularServices() { return popularServices; }
    public void setPopularServices(List<Map<String, Object>> popularServices) { this.popularServices = popularServices; }
}
