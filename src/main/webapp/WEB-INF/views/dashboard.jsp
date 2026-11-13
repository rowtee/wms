<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Workshop Management System - Dashboard</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Workshop Operations Center</h1>
            <p>Real-time vehicle check-in, repair lifecycle, technician tracking &amp; workshop telemetry.</p>
        </div>
        <div style="display: flex; gap: 0.5rem;">
            <a href="<%= request.getContextPath() %>/customers" class="btn btn-secondary">+ Register Customer</a>
            <a href="<%= request.getContextPath() %>/jobs" class="btn btn-primary">+ Open Job Order</a>
        </div>
    </div>

    <!-- Screen 1: KPI Metrics Grid -->
    <div class="kpi-grid">
        <div class="kpi-card">
            <span class="kpi-label">Active Job Orders</span>
            <span class="kpi-value">${kpi.activeJobs}</span>
            <span class="kpi-meta">Vehicles in shop: <strong>${kpi.vehiclesInShop}</strong></span>
        </div>
        <div class="kpi-card warning">
            <span class="kpi-label">Pending Invoices</span>
            <span class="kpi-value">${kpi.pendingInvoices}</span>
            <span class="kpi-meta">Awaiting customer payment</span>
        </div>
        <div class="kpi-card ${kpi.lowStockParts > 0 ? 'purple' : 'success'}">
            <span class="kpi-label">Low Stock Alerts</span>
            <span class="kpi-value">${kpi.lowStockParts}</span>
            <span class="kpi-meta">Parts below reorder threshold</span>
        </div>
        <div class="kpi-card success">
            <span class="kpi-label">Total Revenue</span>
            <span class="kpi-value">RM ${kpi.revenueToday}</span>
            <span class="kpi-meta">Completed jobs: <strong>${kpi.completedJobsThisMonth}</strong></span>
        </div>
    </div>

    <!-- Active Job Queue Table -->
    <div class="card">
        <div class="card-header">
            <h2>Current Workshop Job Queue</h2>
            <a href="<%= request.getContextPath() %>/jobs" class="btn btn-secondary btn-sm">View All Job Orders &rarr;</a>
        </div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Job #</th>
                        <th>Vehicle</th>
                        <th>Customer</th>
                        <th>Advisor / Mechanic</th>
                        <th>Complaint / Reported Issue</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="job" items="${recentJobs}">
                        <tr>
                            <td><strong>#${job.jobId}</strong></td>
                            <td>
                                <strong><code>${job.plateNo}</code></strong><br>
                                <span style="font-size:0.8rem; color:var(--text-secondary);">${job.vehicleInfo}</span>
                            </td>
                            <td>
                                ${job.customerName}<br>
                                <span style="font-size:0.8rem; color:var(--text-secondary);">${job.customerPhone}</span>
                            </td>
                            <td>
                                Adv: <strong>${job.advisorName}</strong><br>
                                <c:choose>
                                    <c:when test="${sessionScope.userRole == 'MANAGER' || sessionScope.userRole == 'ADMIN'}">
                                        <form action="<%= request.getContextPath() %>/jobs" method="POST" style="margin-top:0.35rem; display:flex; align-items:center; gap:0.25rem;">
                                            <input type="hidden" name="action" value="reassign">
                                            <input type="hidden" name="jobId" value="${job.jobId}">
                                            <input type="hidden" name="redirect" value="dashboard">
                                            <select name="mechanicId" class="form-control" style="font-size:0.8rem; padding:0.2rem 0.4rem; max-width:180px; background:white;" onchange="this.form.submit()" title="Reassign Task to Mechanic">
                                                <option value="" ${job.mechanicId == null ? 'selected' : ''}>-- Unassigned --</option>
                                                <c:forEach var="m" items="${mechanics}">
                                                    <option value="${m.staffId}" ${job.mechanicId == m.staffId ? 'selected' : ''}>👨‍🔧 ${m.fullName}</option>
                                                </c:forEach>
                                            </select>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        Tech: <em>${job.mechanicName != null ? job.mechanicName : 'Unassigned'}</em>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="max-width:280px;">${job.complaint}</td>
                            <td>
                                <span class="badge badge-${job.status.toLowerCase()}">${job.status}</span>
                            </td>
                            <td>
                                <a href="<%= request.getContextPath() %>/job-detail?id=${job.jobId}" class="btn btn-primary btn-sm">
                                    Open Sheet &rarr;
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty recentJobs}">
                        <tr>
                            <td colspan="7" style="text-align:center; padding:2rem; color:var(--text-secondary);">
                                No current jobs in the queue. Open a new job order to begin.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System (WMS) &bull; Jakarta EE &amp; JSP Architecture &bull; Local Server Active</p>
</footer>
</body>
</html>
