<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Work Orders &amp; Jobs Queue - WMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Job Orders &amp; Work Queue</h1>
            <p>Manage workshop intake tickets, diagnose customer complaints, and assign certified technicians.</p>
        </div>
        <div>
            <button onclick="document.getElementById('newJobModal').classList.add('open')" class="btn btn-primary">
                + Create New Job Order
            </button>
        </div>
    </div>

    <!-- Status Filter Buttons -->
    <div class="tabs-nav">
        <a href="<%= request.getContextPath() %>/jobs?status=ALL" class="tab-btn ${selectedStatus == 'ALL' ? 'active' : ''}">All Orders</a>
        <a href="<%= request.getContextPath() %>/jobs?status=PENDING" class="tab-btn ${selectedStatus == 'PENDING' ? 'active' : ''}">Pending Intake</a>
        <a href="<%= request.getContextPath() %>/jobs?status=IN_PROGRESS" class="tab-btn ${selectedStatus == 'IN_PROGRESS' ? 'active' : ''}">In Progress</a>
        <a href="<%= request.getContextPath() %>/jobs?status=AWAITING_PARTS" class="tab-btn ${selectedStatus == 'AWAITING_PARTS' ? 'active' : ''}">Awaiting Parts</a>
        <a href="<%= request.getContextPath() %>/jobs?status=COMPLETED" class="tab-btn ${selectedStatus == 'COMPLETED' ? 'active' : ''}">Ready / Completed</a>
        <a href="<%= request.getContextPath() %>/jobs?status=INVOICED" class="tab-btn ${selectedStatus == 'INVOICED' ? 'active' : ''}">Invoiced</a>
    </div>

    <!-- Job Orders Table -->
    <div class="card">
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Job #</th>
                        <th>Vehicle &amp; Owner</th>
                        <th>Check-In &amp; Mileage</th>
                        <th>Advisor &amp; Mechanic</th>
                        <th>Reported Complaint</th>
                        <th>Status</th>
                        <th>Actions</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="j" items="${jobs}">
                        <tr>
                            <td><strong style="font-size:1.05rem;">#${j.jobId}</strong></td>
                            <td>
                                <strong><code>${j.plateNo}</code></strong><br>
                                <span style="font-size:0.85rem; color:#475569;">${j.vehicleInfo}</span><br>
                                <span style="font-size:0.8rem; color:var(--text-secondary);">Owner: ${j.customerName} (${j.customerPhone})</span>
                            </td>
                            <td>
                                ${j.dateIn}<br>
                                <span style="font-size:0.8rem; color:var(--text-secondary);">Odometer: <strong>${j.mileageIn} km</strong></span>
                            </td>
                            <td>
                                Adv: <strong>${j.advisorName}</strong><br>
                                <c:choose>
                                    <c:when test="${sessionScope.userRole == 'MANAGER' || sessionScope.userRole == 'ADMIN'}">
                                        <form action="<%= request.getContextPath() %>/jobs" method="POST" style="margin-top:0.35rem; display:flex; align-items:center; gap:0.25rem;">
                                            <input type="hidden" name="action" value="reassign">
                                            <input type="hidden" name="jobId" value="${j.jobId}">
                                            <input type="hidden" name="status" value="${selectedStatus}">
                                            <select name="mechanicId" class="form-control" style="font-size:0.8rem; padding:0.2rem 0.4rem; max-width:180px; background:white;" onchange="this.form.submit()" title="Reassign Task to Mechanic">
                                                <option value="" ${j.mechanicId == null ? 'selected' : ''}>-- Unassigned --</option>
                                                <c:forEach var="m" items="${mechanics}">
                                                    <option value="${m.staffId}" ${j.mechanicId == m.staffId ? 'selected' : ''}>👨‍🔧 ${m.fullName}</option>
                                                </c:forEach>
                                            </select>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        Tech: <em>${j.mechanicName != null ? j.mechanicName : 'Pending Assignment'}</em>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td style="max-width:260px;">
                                <div style="font-weight:500;">${j.complaint}</div>
                                <c:if test="${not empty j.diagnosis}">
                                    <div style="font-size:0.8rem; color:#0369a1; margin-top:0.25rem;">Diag: ${j.diagnosis}</div>
                                </c:if>
                            </td>
                            <td>
                                <span class="badge badge-${j.status.toLowerCase()}">${j.status}</span>
                            </td>
                            <td>
                                <a href="<%= request.getContextPath() %>/job-detail?id=${j.jobId}" class="btn btn-primary btn-sm">
                                    Manage Sheet &rarr;
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty jobs}">
                        <tr>
                            <td colspan="7" style="text-align:center; padding:2rem; color:var(--text-secondary);">
                                No job orders found matching criteria.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal: New Job Order -->
<div id="newJobModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Intake / Create New Job Order</h3>
            <button class="modal-close" onclick="document.getElementById('newJobModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/jobs" method="POST">
            <div class="form-group">
                <label>Select Registered Vehicle *</label>
                <select name="vehicleId" class="form-control" required>
                    <c:forEach var="v" items="${vehicles}">
                        <option value="${v.vehicleId}">${v.plateNo} - ${v.year} ${v.make} ${v.model} (Owner: ${v.customerName})</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-grid">
                <div class="form-group">
                    <label>Service Advisor *</label>
                    <select name="advisorId" class="form-control" required>
                        <c:forEach var="adv" items="${advisors}">
                            <option value="${adv.staffId}">${adv.fullName}</option>
                        </c:forEach>
                    </select>
                </div>
                <div class="form-group">
                    <label>Assigned Mechanic / Technician</label>
                    <select name="mechanicId" class="form-control">
                        <option value="">-- Assign Later --</option>
                        <c:forEach var="m" items="${mechanics}">
                            <option value="${m.staffId}">${m.fullName}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="form-group">
                <label>Check-in Odometer Mileage (KM) *</label>
                <input type="number" name="mileageIn" class="form-control" placeholder="e.g. 65000" required>
            </div>

            <div class="form-group">
                <label>Customer Complaint / Request *</label>
                <textarea name="complaint" class="form-control" rows="3" placeholder="Describe symptoms: sounds, vibrations, maintenance package..." required></textarea>
            </div>

            <div class="form-group">
                <label>Initial Inspection / Diagnosis Notes</label>
                <textarea name="diagnosis" class="form-control" rows="2" placeholder="Initial observations by service advisor..."></textarea>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('newJobModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary">Open Work Order</button>
            </div>
        </form>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System (WMS) &bull; Work Order Queue &amp; Tracking</p>
</footer>
</body>
</html>
