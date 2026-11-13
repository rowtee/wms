<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Mechanic Workbench - WorkshopMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Mechanic Workbench &amp; Assigned Vehicles</h1>
            <p>Welcome back, <strong>${sessionScope.userName}</strong>. Real-time view of vehicles assigned to your work bay.</p>
        </div>
        <div class="badge-bar">
            <span class="badge" style="background:#dbeafe; color:#1e40af; font-size:0.85rem;">
                🛠️ Assigned Work Orders: ${jobs.size()}
            </span>
        </div>
    </div>

    <c:if test="${param.updated != null}">
        <div class="card" style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:0.75rem 1rem; margin-bottom:1rem;">
            ✓ Work order status and technician diagnosis updated successfully.
        </div>
    </c:if>

    <!-- Status filter tabs -->
    <div class="tabs-nav">
        <a href="<%= request.getContextPath() %>/mechanic?status=ALL" class="tab-btn ${selectedStatus == 'ALL' ? 'active' : ''}">All My Jobs (${jobs.size()})</a>
        <a href="<%= request.getContextPath() %>/mechanic?status=PENDING" class="tab-btn ${selectedStatus == 'PENDING' ? 'active' : ''}">Pending Intake</a>
        <a href="<%= request.getContextPath() %>/mechanic?status=IN_PROGRESS" class="tab-btn ${selectedStatus == 'IN_PROGRESS' ? 'active' : ''}">In Progress</a>
        <a href="<%= request.getContextPath() %>/mechanic?status=AWAITING_PARTS" class="tab-btn ${selectedStatus == 'AWAITING_PARTS' ? 'active' : ''}">Awaiting Parts</a>
        <a href="<%= request.getContextPath() %>/mechanic?status=COMPLETED" class="tab-btn ${selectedStatus == 'COMPLETED' ? 'active' : ''}">Completed</a>
    </div>

    <!-- Assigned Cars Grid / Cards -->
    <div class="grid" style="grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); margin-bottom:2rem;">
        <c:forEach var="j" items="${jobs}">
            <div class="card" style="border-top: 4px solid ${j.status == 'COMPLETED' ? 'var(--success)' : (j.status == 'IN_PROGRESS' ? 'var(--primary)' : 'var(--warning)')};">
                <div class="card-header" style="margin-bottom:0.75rem; padding-bottom:0.5rem;">
                    <div>
                        <span style="font-size:0.8rem; font-weight:700; color:var(--text-secondary);">JOB #${j.jobId}</span>
                        <h3 style="font-size:1.25rem; font-family:monospace; color:#0f172a; margin-top:0.2rem;">${j.plateNo}</h3>
                    </div>
                    <span class="badge badge-${j.status.toLowerCase()}">${j.status}</span>
                </div>

                <!-- Car Details -->
                <div style="background:#f8fafc; border-radius:8px; padding:0.75rem; margin-bottom:1rem; border:1px solid var(--border);">
                    <div style="font-weight:600; font-size:0.95rem; color:#1e293b;">
                        🚗 ${j.vehicleInfo}
                    </div>
                    <div style="font-size:0.825rem; color:var(--text-secondary); margin-top:0.25rem;">
                        Odometer Check-in: <strong>${j.mileageIn} km</strong> &bull; Owner: ${j.customerName}
                    </div>
                </div>

                <!-- Description of what must be done -->
                <div style="margin-bottom:1rem;">
                    <span style="font-size:0.8rem; font-weight:700; text-transform:uppercase; color:#dc2626; letter-spacing:0.05em;">
                        📋 What Must Be Done (Customer Complaint):
                    </span>
                    <div style="background:#fff1f2; border-left:3px solid #f43f5e; padding:0.6rem 0.75rem; border-radius:0 6px 6px 0; font-size:0.875rem; color:#9f1239; margin-top:0.35rem;">
                        ${j.complaint}
                    </div>
                </div>

                <!-- Mechanic Status Updater Form -->
                <form action="<%= request.getContextPath() %>/mechanic" method="POST" style="border-top:1px solid var(--border); padding-top:0.75rem;">
                    <input type="hidden" name="action" value="updateStatus">
                    <input type="hidden" name="jobId" value="${j.jobId}">

                    <div class="form-group" style="margin-bottom:0.5rem;">
                        <label style="font-size:0.8rem;">Update Status:</label>
                        <select name="status" class="form-control" style="font-size:0.85rem; padding:0.4rem 0.6rem;">
                            <option value="PENDING" ${j.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                            <option value="IN_PROGRESS" ${j.status == 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                            <option value="AWAITING_PARTS" ${j.status == 'AWAITING_PARTS' ? 'selected' : ''}>AWAITING PARTS</option>
                            <option value="COMPLETED" ${j.status == 'COMPLETED' ? 'selected' : ''}>COMPLETED / READY</option>
                        </select>
                    </div>

                    <div class="form-group" style="margin-bottom:0.75rem;">
                        <label style="font-size:0.8rem;">Mechanic Inspection / Diagnosis Notes:</label>
                        <textarea name="diagnosis" class="form-control" rows="2" style="font-size:0.85rem;" placeholder="Note actions taken, test drive outcome...">${j.diagnosis}</textarea>
                    </div>

                    <div style="display:flex; justify-content:space-between; align-items:center;">
                        <a href="<%= request.getContextPath() %>/job-detail?id=${j.jobId}" class="btn btn-secondary btn-sm">
                            Open Full Sheet &rarr;
                        </a>
                        <button type="submit" class="btn btn-primary btn-sm">
                            Save Progress
                        </button>
                    </div>
                </form>
            </div>
        </c:forEach>

        <c:if test="${empty jobs}">
            <div class="card" style="grid-column: 1 / -1; text-align:center; padding:3rem; color:var(--text-secondary);">
                <h3>No work orders assigned to you right now.</h3>
                <p>New jobs created by the main counter will appear here once allocated to your technician account.</p>
            </div>
        </c:if>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System &bull; Mechanic Workbench Module</p>
</footer>
</body>
</html>
