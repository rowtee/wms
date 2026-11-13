<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manager Analytics &amp; Operations - WorkshopMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>General Manager Analytics &amp; Operations</h1>
            <p>High-level workshop governance: complete vehicle status monitoring, financial KPIs, and monthly executive reports.</p>
        </div>
        <div>
            <!-- Monthly Report Picker & PDF Generator -->
            <form action="<%= request.getContextPath() %>/manager" method="GET" style="display:flex; gap:0.5rem; align-items:center; flex-wrap:wrap;">
                <label style="font-size:0.85rem; font-weight:600; color:#475569;">Period:</label>
                <select name="reportMonth" class="form-control" style="width:auto;">
                    <c:forEach var="m" begin="1" end="12">
                        <option value="${m}" ${selectedMonth == m ? 'selected' : ''}>Month ${m}</option>
                    </c:forEach>
                </select>
                <select name="reportYear" class="form-control" style="width:auto;">
                    <option value="2026" ${selectedYear == 2026 ? 'selected' : ''}>2026</option>
                    <option value="2025" ${selectedYear == 2025 ? 'selected' : ''}>2025</option>
                </select>
                <button type="submit" name="export" value="pdf" class="btn btn-primary btn-sm">
                    📄 Generate PDF Report
                </button>
                <button type="submit" class="btn btn-secondary btn-sm">
                    Filter View
                </button>
            </form>
        </div>
    </div>

    <!-- Monthly Executive Sales & Metrics Report Card -->
    <div class="card" style="border: 2px solid var(--primary); background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);">
        <div class="card-header" style="border-bottom-color:#cbd5e1;">
            <div>
                <span class="badge" style="background:#1e3a8a; color:white;">Monthly Performance Report</span>
                <h2 style="color:#0f172a; margin-top:0.25rem;">Executive Summary: ${selectedYear} / Month ${selectedMonth}</h2>
            </div>
            <div style="text-align:right;">
                <div style="font-size:1.5rem; font-weight:800; color:var(--primary); margin-bottom:0.35rem;">
                    Sales: RM ${report.totalSales}
                </div>
                <a href="<%= request.getContextPath() %>/manager?reportYear=${selectedYear}&reportMonth=${selectedMonth}&export=pdf" class="btn btn-success btn-sm">
                    ⬇️ Download PDF Report
                </a>
            </div>
        </div>

        <div class="kpi-grid" style="margin-bottom:1rem;">
            <div class="kpi-card success">
                <span class="kpi-label">Total Sales</span>
                <span class="kpi-value" style="font-size:1.6rem; color:#047857;">RM ${report.totalSales}</span>
                <span class="kpi-meta">SST Collected (6%): <strong>RM ${report.totalTaxCollected}</strong></span>
            </div>
            <div class="kpi-card">
                <span class="kpi-label">Labor Revenue</span>
                <span class="kpi-value" style="font-size:1.6rem;">RM ${report.totalLaborRevenue}</span>
                <span class="kpi-meta">Mechanic labor charges</span>
            </div>
            <div class="kpi-card purple">
                <span class="kpi-label">Parts Revenue</span>
                <span class="kpi-value" style="font-size:1.6rem; color:#6d28d9;">RM ${report.totalPartsRevenue}</span>
                <span class="kpi-meta">Workshop parts &amp; inventory</span>
            </div>
            <div class="kpi-card warning">
                <span class="kpi-label">Vehicles Serviced</span>
                <span class="kpi-value" style="font-size:1.6rem; color:#b45309;">${report.totalVehiclesServiced}</span>
                <span class="kpi-meta">New customers: <strong>+${report.newCustomersRegistered}</strong></span>
            </div>
        </div>

        <!-- Breakdown: Mechanic Productivity & Top Performing Services -->
        <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); margin-bottom:0;">
            <!-- Mechanic Productivity -->
            <div>
                <h3 style="font-size:1rem; color:#1e293b; margin-bottom:0.75rem;">👨‍🔧 Mechanic Performance &amp; Productivity</h3>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Mechanic Name</th>
                                <th>Total Tasks</th>
                                <th>Completed</th>
                                <th>Completion Rate</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="m" items="${report.mechanicPerformance}">
                                <tr>
                                    <td><strong>${m.name}</strong></td>
                                    <td>${m.totalJobs} vehicles</td>
                                    <td><span class="badge badge-completed">${m.completedJobs} done</span></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${m.totalJobs > 0}">
                                                <fmt:formatNumber value="${(m.completedJobs / m.totalJobs) * 100}" maxFractionDigits="0"/>%
                                            </c:when>
                                            <c:otherwise>0%</c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>

            <!-- Top Services -->
            <div>
                <h3 style="font-size:1rem; color:#1e293b; margin-bottom:0.75rem;">🏆 Most Popular Service Packages</h3>
                <div class="table-responsive">
                    <table>
                        <thead>
                            <tr>
                                <th>Service Package</th>
                                <th>Frequency</th>
                                <th style="text-align:right;">Total Revenue</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${report.popularServices}">
                                <tr>
                                    <td><strong>${s.name}</strong></td>
                                    <td><span class="badge" style="background:#e0f2fe; color:#0369a1;">${s.usageCount} orders</span></td>
                                    <td style="text-align:right; font-weight:700;">RM ${s.totalCharge}</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- All Car Status Monitor (Full Fleet Visibility) -->
    <div class="card">
        <div class="card-header">
            <div>
                <h2>🚗 All Vehicles &amp; Repair Status Master Board</h2>
                <p style="font-size:0.85rem; color:var(--text-secondary);">Real-time tracking of every vehicle currently in the garage.</p>
            </div>
            <!-- Status Filter -->
            <div style="display:flex; gap:0.5rem;">
                <a href="<%= request.getContextPath() %>/manager?status=ALL" class="btn btn-secondary btn-sm ${selectedStatus == 'ALL' ? 'btn-primary' : ''}">All</a>
                <a href="<%= request.getContextPath() %>/manager?status=IN_PROGRESS" class="btn btn-secondary btn-sm ${selectedStatus == 'IN_PROGRESS' ? 'btn-primary' : ''}">In Progress</a>
                <a href="<%= request.getContextPath() %>/manager?status=AWAITING_PARTS" class="btn btn-secondary btn-sm ${selectedStatus == 'AWAITING_PARTS' ? 'btn-primary' : ''}">Awaiting Parts</a>
                <a href="<%= request.getContextPath() %>/manager?status=COMPLETED" class="btn btn-secondary btn-sm ${selectedStatus == 'COMPLETED' ? 'btn-primary' : ''}">Completed</a>
                <a href="<%= request.getContextPath() %>/manager?status=INVOICED" class="btn btn-secondary btn-sm ${selectedStatus == 'INVOICED' ? 'btn-primary' : ''}">Invoiced</a>
            </div>
        </div>

        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Job #</th>
                        <th>Plate No</th>
                        <th>Car Model &amp; Year</th>
                        <th>Customer Contact</th>
                        <th>Intake Odometer</th>
                        <th>Assigned Mechanic</th>
                        <th>Current Repair Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="j" items="${allJobs}">
                        <tr>
                            <td><strong>#${j.jobId}</strong></td>
                            <td><strong style="font-family:monospace; font-size:1.05rem;"><code>${j.plateNo}</code></strong></td>
                            <td>${j.vehicleInfo}</td>
                            <td>
                                ${j.customerName}<br>
                                <span style="font-size:0.8rem; color:var(--text-secondary);">${j.customerPhone}</span>
                            </td>
                            <td>${j.mileageIn} km</td>
                            <td>
                                <form action="<%= request.getContextPath() %>/manager" method="POST" style="margin:0; display:flex; align-items:center; gap:0.35rem;">
                                    <input type="hidden" name="action" value="reassign">
                                    <input type="hidden" name="jobId" value="${j.jobId}">
                                    <input type="hidden" name="status" value="${selectedStatus}">
                                    <select name="mechanicId" class="form-control" style="font-size:0.825rem; padding:0.25rem 0.5rem; min-width:145px; background:white;" onchange="this.form.submit()" title="Reassign Task to Mechanic">
                                        <option value="" ${j.mechanicId == null ? 'selected' : ''}>-- Unassigned --</option>
                                        <c:forEach var="m" items="${mechanics}">
                                            <option value="${m.staffId}" ${j.mechanicId == m.staffId ? 'selected' : ''}>👨‍🔧 ${m.fullName}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                            </td>
                            <td>
                                <span class="badge badge-${j.status.toLowerCase()}">${j.status}</span>
                            </td>
                            <td>
                                <a href="<%= request.getContextPath() %>/job-detail?id=${j.jobId}" class="btn btn-secondary btn-sm">
                                    Audit Sheet &rarr;
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty allJobs}">
                        <tr>
                            <td colspan="8" style="text-align:center; padding:2rem; color:var(--text-secondary);">
                                No vehicles found matching status filter.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System &bull; Manager Governance &amp; Analytics Module</p>
</footer>
</body>
</html>
