<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Work Order #${job.jobId} - Job Execution Sheet</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <div style="display:flex; align-items:center; gap:0.75rem;">
                <h1>Job Order #${job.jobId}</h1>
                <span class="badge badge-${job.status.toLowerCase()}">${job.status}</span>
            </div>
            <p>Vehicle: <strong>${job.plateNo}</strong> &bull; ${job.vehicleInfo} &bull; Owner: ${job.customerName}</p>
        </div>
        <div style="display:flex; gap:0.5rem;">
            <a href="<%= request.getContextPath() %>/jobs" class="btn btn-secondary">&larr; Back to Queue</a>
            <c:if test="${empty invoice}">
                <button onclick="document.getElementById('invoiceModal').classList.add('open')" class="btn btn-success">
                    🧾 Finalize &amp; Generate Invoice
                </button>
            </c:if>
            <c:if test="${not empty invoice}">
                <a href="<%= request.getContextPath() %>/inventory-billing?tab=billing" class="btn btn-secondary">
                    View Invoice #${invoice.invoiceId} (${invoice.paymentStatus}) &rarr;
                </a>
            </c:if>
        </div>
    </div>

    <!-- Vehicle & Customer Telemetry Summary -->
    <div class="grid" style="margin-bottom:1.5rem;">
        <div class="card" style="margin-bottom:0;">
            <div class="card-header">
                <h3>Customer &amp; Vehicle Info</h3>
            </div>
            <ul class="module-list">
                <li>
                    <span class="module-name">Customer</span>
                    <span class="module-desc"><strong>${job.customerName}</strong> (${job.customerPhone})</span>
                </li>
                <li>
                    <span class="module-name">Vehicle</span>
                    <span class="module-desc">${job.plateNo} &bull; ${job.vehicleInfo}</span>
                </li>
                <li>
                    <span class="module-name">Check-in Odometer</span>
                    <span class="module-desc"><strong>${job.mileageIn} km</strong></span>
                </li>
                <li>
                    <span class="module-name">Next Service Recommendation</span>
                    <span class="module-desc">${job.nextServiceKm} km (${job.nextServiceDate})</span>
                </li>
                <li>
                    <span class="module-name">Assigned Mechanic</span>
                    <span class="module-desc">
                        <c:choose>
                            <c:when test="${sessionScope.userRole == 'MANAGER' || sessionScope.userRole == 'ADMIN'}">
                                <form action="<%= request.getContextPath() %>/job-detail" method="POST" style="margin:0; display:inline-flex; align-items:center; gap:0.35rem;">
                                    <input type="hidden" name="action" value="reassignMechanic">
                                    <input type="hidden" name="jobId" value="${job.jobId}">
                                    <select name="mechanicId" class="form-control" style="font-size:0.8rem; padding:0.2rem 0.4rem; background:white;" onchange="this.form.submit()" title="Reassign Task to Mechanic">
                                        <option value="" ${job.mechanicId == null ? 'selected' : ''}>-- Unassigned --</option>
                                        <c:forEach var="m" items="${mechanics}">
                                            <option value="${m.staffId}" ${job.mechanicId == m.staffId ? 'selected' : ''}>👨‍🔧 ${m.fullName}</option>
                                        </c:forEach>
                                    </select>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <strong>${job.mechanicName != null ? job.mechanicName : 'Unassigned'}</strong>
                            </c:otherwise>
                        </c:choose>
                    </span>
                </li>
            </ul>
        </div>

        <div class="card" style="margin-bottom:0;">
            <div class="card-header">
                <h3>Diagnosis &amp; Status Controls</h3>
            </div>
            <form action="<%= request.getContextPath() %>/job-detail" method="POST">
                <input type="hidden" name="action" value="updateStatus">
                <input type="hidden" name="jobId" value="${job.jobId}">
                
                <div class="form-group">
                    <label>Reported Customer Complaint</label>
                    <div style="background:#f8fafc; padding:0.5rem 0.75rem; border-radius:6px; font-size:0.875rem; border:1px solid var(--border);">
                        ${job.complaint}
                    </div>
                </div>

                <div class="form-group">
                    <label>Technician Diagnosis &amp; Remedy</label>
                    <textarea name="diagnosis" class="form-control" rows="2">${job.diagnosis}</textarea>
                </div>

                <div style="display:flex; justify-content:space-between; align-items:center; gap:0.5rem;">
                    <select name="status" class="form-control" style="max-width:200px;">
                        <option value="PENDING" ${job.status == 'PENDING' ? 'selected' : ''}>PENDING</option>
                        <option value="IN_PROGRESS" ${job.status == 'IN_PROGRESS' ? 'selected' : ''}>IN PROGRESS</option>
                        <option value="AWAITING_PARTS" ${job.status == 'AWAITING_PARTS' ? 'selected' : ''}>AWAITING PARTS</option>
                        <option value="COMPLETED" ${job.status == 'COMPLETED' ? 'selected' : ''}>COMPLETED</option>
                    </select>
                    <button type="submit" class="btn btn-secondary btn-sm">Update Progress</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Labor Services Performed -->
    <div class="card">
        <div class="card-header">
            <div>
                <h2>1. Labor &amp; Workshop Services</h2>
                <span style="font-size:0.85rem; color:var(--text-secondary);">Tasks executed by certified technician</span>
            </div>
            <button onclick="document.getElementById('serviceModal').classList.add('open')" class="btn btn-secondary btn-sm">
                + Add Service Task
            </button>
        </div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Service / Labor Description</th>
                        <th>Technician Notes</th>
                        <th style="text-align:right;">Standard Charge</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="s" items="${services}">
                        <tr>
                            <td><strong>${s.serviceName}</strong></td>
                            <td>${not empty s.notes ? s.notes : '<em style="color:#94a3b8;">Standard procedure executed</em>'}</td>
                            <td style="text-align:right; font-weight:600;">RM ${s.labourCharge}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty services}">
                        <tr>
                            <td colspan="3" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">
                                No services assigned yet. Click "+ Add Service Task" to append labor.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
                <tfoot>
                    <tr style="background:#f8fafc; font-weight:700;">
                        <td colspan="2">Total Labor Subtotal</td>
                        <td style="text-align:right;">RM ${laborTotal}</td>
                    </tr>
                </tfoot>
            </table>
        </div>
    </div>

    <!-- Spare Parts Issued -->
    <div class="card">
        <div class="card-header">
            <div>
                <h2>2. Spare Parts &amp; Consumables Issued</h2>
                <span style="font-size:0.85rem; color:var(--text-secondary);">Components deducted from warehouse inventory</span>
            </div>
            <button onclick="document.getElementById('partModal').classList.add('open')" class="btn btn-secondary btn-sm">
                + Issue Part from Inventory
            </button>
        </div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Part #</th>
                        <th>Part Description</th>
                        <th>Unit Price</th>
                        <th>Qty</th>
                        <th style="text-align:right;">Subtotal</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="p" items="${parts}">
                        <tr>
                            <td><code>${p.partNo}</code></td>
                            <td><strong>${p.partName}</strong></td>
                            <td>RM ${p.unitPrice}</td>
                            <td><span class="badge" style="background:#f1f5f9; color:#1e293b;">x${p.quantity}</span></td>
                            <td style="text-align:right; font-weight:600;">RM ${p.subtotal}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty parts}">
                        <tr>
                            <td colspan="5" style="text-align:center; padding:1.5rem; color:var(--text-secondary);">
                                No spare parts billed to this order yet.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
                <tfoot>
                    <tr style="background:#f8fafc; font-weight:700;">
                        <td colspan="4">Total Parts Subtotal</td>
                        <td style="text-align:right;">RM ${partsTotal}</td>
                    </tr>
                </tfoot>
            </table>
        </div>
    </div>

    <!-- Financial Breakdown Panel -->
    <div class="card" style="background:#f8fafc; border:2px solid var(--border);">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
            <div>
                <h3 style="font-size:1.15rem; color:#1e293b;">Work Order Financial Summary (Malaysia Base)</h3>
                <span style="font-size:0.85rem; color:var(--text-secondary);">Labor charges + Material costs + Statutory SST (6%)</span>
            </div>
            <div style="text-align:right; min-width:240px;">
                <div style="display:flex; justify-content:space-between; margin-bottom:0.25rem;">
                    <span>Labor Subtotal:</span>
                    <strong>RM ${laborTotal}</strong>
                </div>
                <div style="display:flex; justify-content:space-between; margin-bottom:0.25rem;">
                    <span>Parts Subtotal:</span>
                    <strong>RM ${partsTotal}</strong>
                </div>
                <div style="display:flex; justify-content:space-between; margin-bottom:0.25rem;">
                    <span>SST (6%):</span>
                    <strong>RM ${tax}</strong>
                </div>
                <div style="display:flex; justify-content:space-between; font-size:1.3rem; font-weight:700; color:var(--primary); border-top:1px solid #cbd5e1; padding-top:0.4rem; margin-top:0.4rem;">
                    <span>Grand Total:</span>
                    <span>RM ${total}</span>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal: Add Service -->
<div id="serviceModal" class="modal-overlay">
    <div class="modal-content" style="max-width: 680px;">
        <div class="modal-header">
            <div>
                <h3 style="margin:0;">Add Services to Job Sheet</h3>
                <p style="margin:0.25rem 0 0 0; font-size:0.85rem; color:#64748b;">Select one or multiple standard service packages to attach to this work order.</p>
            </div>
            <button class="modal-close" onclick="document.getElementById('serviceModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/job-detail" method="POST" id="addServiceForm" onsubmit="return validateServiceModal()">
            <input type="hidden" name="action" value="addService">
            <input type="hidden" name="jobId" value="${job.jobId}">
            
            <div class="form-group">
                <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:0.5rem;">
                    <label style="font-weight:600; margin:0;">Select Standard Services (Multi-Select) *</label>
                    <div style="font-size:0.8rem; display:flex; gap:0.5rem;">
                        <button type="button" class="btn btn-secondary" style="padding:0.2rem 0.6rem; font-size:0.75rem;" onclick="selectAllServices(true)">Select All</button>
                        <button type="button" class="btn btn-secondary" style="padding:0.2rem 0.6rem; font-size:0.75rem;" onclick="selectAllServices(false)">Clear All</button>
                    </div>
                </div>

                <div style="display:flex; flex-direction:column; gap:0.5rem; max-height:280px; overflow-y:auto; padding:0.5rem; border:1px solid var(--border); border-radius:6px; background:#f8fafc;">
                    <c:forEach var="st" items="${catalogServices}">
                        <label class="modal-service-item" style="display:flex; justify-content:space-between; align-items:center; padding:0.65rem 0.85rem; background:white; border:1px solid #e2e8f0; border-radius:6px; cursor:pointer; transition:all 0.15s;">
                            <div style="display:flex; align-items:center; gap:0.65rem;">
                                <input type="checkbox" name="serviceTypeIds" value="${st.serviceTypeId}" data-price="${st.labourCharge}" onchange="updateModalServiceSummary()" style="width:17px; height:17px; cursor:pointer;">
                                <div>
                                    <strong style="color:#0f172a; font-size:0.95rem;">${st.name}</strong>
                                    <div style="font-size:0.78rem; color:#64748b;">
                                        Recommended: ${st.intervalKm} km &bull; ${st.intervalMonths} months interval
                                    </div>
                                </div>
                            </div>
                            <span style="font-weight:700; color:var(--primary); font-size:0.95rem; white-space:nowrap;">RM ${st.labourCharge}</span>
                        </label>
                    </c:forEach>
                </div>

                <!-- Live Selection Summary -->
                <div id="serviceModalSummary" style="margin-top:0.6rem; padding:0.6rem 0.85rem; background:#e0f2fe; border:1px solid #bae6fd; border-radius:6px; display:flex; justify-content:space-between; align-items:center; font-size:0.85rem; color:#0369a1;">
                    <span>Selected: <strong id="modalSelectedCount">0</strong> service(s)</span>
                    <span>Total Labor Charge: <strong id="modalTotalLabor" style="font-size:1rem; color:#0284c7;">RM 0.00</strong></span>
                </div>
            </div>

            <div class="form-group" style="margin-top:1rem;">
                <label>Technician Specific Notes (Applied to selected services)</label>
                <input type="text" name="notes" class="form-control" placeholder="e.g. Performed 30-point safety check, tested rotor runout">
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('serviceModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary" id="addServiceSubmitBtn">Add Selected Services</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Issue Part -->
<div id="partModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Issue Spare Part from Inventory</h3>
            <button class="modal-close" onclick="document.getElementById('partModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/job-detail" method="POST">
            <input type="hidden" name="action" value="addPart">
            <input type="hidden" name="jobId" value="${job.jobId}">
            
            <div class="form-group">
                <label>Select Spare Part *</label>
                <select name="partId" class="form-control" required>
                    <c:forEach var="p" items="${catalogParts}">
                        <option value="${p.partId}">${p.partNo} - ${p.name} (RM ${p.unitPrice}) [In Stock: ${p.stockQty}]</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label>Quantity *</label>
                <input type="number" name="quantity" class="form-control" min="1" value="1" required>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('partModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary">Issue to Job &amp; Deduct Stock</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Finalize Invoice -->
<div id="invoiceModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Generate &amp; Issue Official Invoice</h3>
            <button class="modal-close" onclick="document.getElementById('invoiceModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/job-detail" method="POST">
            <input type="hidden" name="action" value="generateInvoice">
            <input type="hidden" name="jobId" value="${job.jobId}">
            <input type="hidden" name="subtotal" value="${subtotal}">
            <input type="hidden" name="tax" value="${tax}">
            
            <p style="margin-bottom:1rem; color:var(--text-secondary); font-size:0.9rem;">
                Finalize this work order, compute statutory SST (6%), and produce an official billing invoice for <strong>${job.customerName}</strong>.
            </p>

            <div class="form-grid">
                <div class="form-group">
                    <label>Subtotal (Labor + Spare Parts)</label>
                    <input type="text" class="form-control" value="RM ${subtotal}" disabled>
                </div>
                <div class="form-group">
                    <label>Service Tax SST (6%)</label>
                    <input type="text" class="form-control" value="RM ${tax}" disabled>
                </div>
            </div>

            <div class="form-group">
                <label>Promotional / Loyalty Discount (RM)</label>
                <input type="number" name="discount" class="form-control" step="0.01" value="0.00" required>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('invoiceModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-success">Confirm &amp; Issue Invoice</button>
            </div>
        </form>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System (WMS) &bull; Job Execution &amp; Line Items</p>
</footer>

<script>
function selectAllServices(select) {
    var cbs = document.querySelectorAll('#addServiceForm input[name="serviceTypeIds"]');
    cbs.forEach(function(cb) {
        cb.checked = select;
    });
    updateModalServiceSummary();
}

function updateModalServiceSummary() {
    var cbs = document.querySelectorAll('#addServiceForm input[name="serviceTypeIds"]:checked');
    var count = cbs.length;
    var total = 0.0;
    cbs.forEach(function(cb) {
        total += parseFloat(cb.getAttribute('data-price') || 0);
    });

    var countEl = document.getElementById('modalSelectedCount');
    var totalEl = document.getElementById('modalTotalLabor');
    if (countEl) countEl.innerText = count;
    if (totalEl) totalEl.innerText = 'RM ' + total.toFixed(2);
}

function validateServiceModal() {
    var checked = document.querySelectorAll('#addServiceForm input[name="serviceTypeIds"]:checked');
    if (checked.length === 0) {
        alert('Please select at least one standard service package to add to this job sheet.');
        return false;
    }
    return true;
}
</script>
</body>
</html>
