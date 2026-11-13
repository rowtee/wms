package com.wms.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class DashboardKpi implements Serializable {
    private static final long serialVersionUID = 1L;

    private int activeJobs;
    private int vehiclesInShop;
    private int pendingInvoices;
    private int lowStockParts;
    private BigDecimal revenueToday;
    private int completedJobsThisMonth;

    public DashboardKpi() {}

    public int getActiveJobs() { return activeJobs; }
    public void setActiveJobs(int activeJobs) { this.activeJobs = activeJobs; }
    public int getVehiclesInShop() { return vehiclesInShop; }
    public void setVehiclesInShop(int vehiclesInShop) { this.vehiclesInShop = vehiclesInShop; }
    public int getPendingInvoices() { return pendingInvoices; }
    public void setPendingInvoices(int pendingInvoices) { this.pendingInvoices = pendingInvoices; }
    public int getLowStockParts() { return lowStockParts; }
    public void setLowStockParts(int lowStockParts) { this.lowStockParts = lowStockParts; }
    public BigDecimal getRevenueToday() { return revenueToday; }
    public void setRevenueToday(BigDecimal revenueToday) { this.revenueToday = revenueToday; }
    public int getCompletedJobsThisMonth() { return completedJobsThisMonth; }
    public void setCompletedJobsThisMonth(int completedJobsThisMonth) { this.completedJobsThisMonth = completedJobsThisMonth; }
}
