<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Inventory &amp; Invoicing Hub - WMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Parts Inventory &amp; Invoicing Hub</h1>
            <p>Warehouse parts supply tracking, inventory reordering, customer billing, and payment settlements.</p>
        </div>
        <div style="display:flex; gap:0.5rem;">
            <button onclick="document.getElementById('restockModal').classList.add('open')" class="btn btn-secondary">
                🔄 Quick Restock
            </button>
            <button onclick="document.getElementById('newPartModal').classList.add('open')" class="btn btn-primary">
                + Add New Spare Part
            </button>
        </div>
    </div>

    <!-- Navigation Tabs -->
    <div class="tabs-nav">
        <a href="<%= request.getContextPath() %>/inventory-billing?tab=inventory" class="tab-btn ${activeTab == 'inventory' ? 'active' : ''}">
            📦 Spare Parts Inventory (${parts.size()})
        </a>
        <a href="<%= request.getContextPath() %>/inventory-billing?tab=billing" class="tab-btn ${activeTab == 'billing' ? 'active' : ''}">
            💳 Billing &amp; Invoices (${invoices.size()})
        </a>
    </div>

    <c:if test="${param.success != null}">
        <div class="card" style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:0.75rem 1rem; margin-bottom:1rem;">
            ✓ Operation performed successfully!
        </div>
    </c:if>

    <!-- Tab 1: Inventory Table -->
    <c:if test="${activeTab == 'inventory'}">
        <div class="card">
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Part Number</th>
                            <th>Part Description</th>
                            <th>Category</th>
                            <th>Stock Level</th>
                            <th>Reorder Level</th>
                            <th>Cost Price</th>
                            <th>Selling Price</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="p" items="${parts}">
                            <tr>
                                <td><code>${p.partNo}</code></td>
                                <td><strong>${p.name}</strong></td>
                                <td><span class="badge" style="background:#e2e8f0; color:#334155;">${p.category}</span></td>
                                <td>
                                    <strong>${p.stockQty}</strong> units
                                    <c:if test="${p.isLowStock()}">
                                        <span class="badge badge-low-stock" style="margin-left:0.5rem;">⚠️ Low Stock</span>
                                    </c:if>
                                </td>
                                <td>${p.reorderLevel} units</td>
                                <td>RM ${p.costPrice}</td>
                                <td><strong style="color:var(--primary);">RM ${p.unitPrice}</strong></td>
                                <td>
                                    <button class="btn btn-secondary btn-sm" onclick="openRestockFor('${p.partId}', '${p.name}')">
                                        + Restock
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>

    <!-- Tab 2: Invoices & Billing Table -->
    <c:if test="${activeTab == 'billing'}">
        <div class="card">
            <div class="table-responsive">
                <table>
                    <thead>
                        <tr>
                            <th>Invoice #</th>
                            <th>Job #</th>
                            <th>Invoice Date</th>
                            <th>Customer &amp; Vehicle</th>
                            <th>Subtotal</th>
                            <th>SST (6%)</th>
                            <th>Grand Total</th>
                            <th>Payment Status</th>
                            <th>Method</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inv" items="${invoices}">
                            <tr>
                                <td><strong style="font-size:1.05rem;">INV-${inv.invoiceId}</strong></td>
                                <td>
                                    <a href="<%= request.getContextPath() %>/job-detail?id=${inv.jobId}">
                                        Job #${inv.jobId}
                                    </a>
                                </td>
                                <td>${inv.invoiceDate}</td>
                                <td>
                                    <strong>${inv.customerName}</strong><br>
                                    <code>${inv.plateNo}</code>
                                </td>
                                <td>RM ${inv.subtotal}</td>
                                <td>RM ${inv.tax}</td>
                                <td><strong style="font-size:1.1rem; color:var(--primary);">RM ${inv.total}</strong></td>
                                <td>
                                    <span class="badge ${inv.paymentStatus == 'PAID' ? 'badge-paid' : 'badge-pending'}">
                                        ${inv.paymentStatus}
                                    </span>
                                </td>
                                <td>
                                    ${not empty inv.paymentMethod ? inv.paymentMethod : '<em style="color:#94a3b8;">Unpaid</em>'}
                                </td>
                                <td>
                                    <c:if test="${inv.paymentStatus != 'PAID'}">
                                        <button onclick="openPayModal('${inv.invoiceId}', '${inv.total}')" class="btn btn-success btn-sm">
                                            Collect Payment
                                        </button>
                                    </c:if>
                                    <c:if test="${inv.paymentStatus == 'PAID'}">
                                        <span style="color:#059669; font-weight:600; font-size:0.85rem;">✓ Settled</span>
                                    </c:if>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty invoices}">
                            <tr>
                                <td colspan="10" style="text-align:center; padding:2rem; color:var(--text-secondary);">
                                    No invoices issued yet.
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </div>
    </c:if>
</div>

<!-- Modal: Quick Restock -->
<div id="restockModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Restock Inventory Quantity</h3>
            <button class="modal-close" onclick="document.getElementById('restockModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/inventory-billing" method="POST">
            <input type="hidden" name="action" value="restock">
            
            <div class="form-group">
                <label>Select Part to Restock *</label>
                <select id="restockPartSelect" name="partId" class="form-control" required>
                    <c:forEach var="p" items="${parts}">
                        <option value="${p.partId}">${p.partNo} - ${p.name} (Current: ${p.stockQty})</option>
                    </c:forEach>
                </select>
            </div>

            <div class="form-group">
                <label>Additional Units to Add *</label>
                <input type="number" name="quantity" class="form-control" min="1" value="10" required>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('restockModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary">Add to Stock</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Create New Part -->
<div id="newPartModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Add New Spare Part / Material</h3>
            <button class="modal-close" onclick="document.getElementById('newPartModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/inventory-billing" method="POST">
            <input type="hidden" name="action" value="createPart">
            
            <div class="form-grid">
                <div class="form-group">
                    <label>Part Code / SKU *</label>
                    <input type="text" name="partNo" class="form-control" placeholder="e.g. BLT-FAN-09" required>
                </div>
                <div class="form-group">
                    <label>Part Name *</label>
                    <input type="text" name="name" class="form-control" placeholder="e.g. Serpentine Alternator Belt" required>
                </div>
                <div class="form-group">
                    <label>Category *</label>
                    <input type="text" name="category" class="form-control" placeholder="e.g. Belts, Cooling, Electrical" required>
                </div>
                <div class="form-group">
                    <label>Initial Stock Qty *</label>
                    <input type="number" name="stockQty" class="form-control" min="0" value="10" required>
                </div>
                <div class="form-group">
                    <label>Reorder Alert Threshold *</label>
                    <input type="number" name="reorderLevel" class="form-control" min="1" value="5" required>
                </div>
                <div class="form-group">
                    <label>Cost Price ($) *</label>
                    <input type="number" step="0.01" name="costPrice" class="form-control" placeholder="15.00" required>
                </div>
                <div class="form-group">
                    <label>Selling Price ($) *</label>
                    <input type="number" step="0.01" name="unitPrice" class="form-control" placeholder="29.99" required>
                </div>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('newPartModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Part</button>
            </div>
        </form>
    </div>
</div>

<!-- Modal: Pay Invoice -->
<div id="payModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Record Invoice Payment</h3>
            <button class="modal-close" onclick="document.getElementById('payModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/inventory-billing" method="POST">
            <input type="hidden" name="action" value="payInvoice">
            <input type="hidden" id="payInvoiceId" name="invoiceId" value="">
            
            <p style="margin-bottom:1rem; color:var(--text-secondary);">
                Amount to settle: <strong id="payAmountDisplay" style="color:var(--primary); font-size:1.1rem;"></strong>
            </p>

            <div class="form-group">
                <label>Payment Method *</label>
                <select name="paymentMethod" class="form-control" required>
                    <option value="CREDIT_CARD">Credit / Debit Card</option>
                    <option value="CASH">Cash</option>
                    <option value="BANK_TRANSFER">Bank Transfer / Wire</option>
                </select>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('payModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-success">Confirm Payment Received</button>
            </div>
        </form>
    </div>
</div>

<script>
function openRestockFor(partId, partName) {
    document.getElementById('restockPartSelect').value = partId;
    document.getElementById('restockModal').classList.add('open');
}

function openPayModal(invId, amount) {
    document.getElementById('payInvoiceId').value = invId;
    document.getElementById('payAmountDisplay').innerText = '$' + amount;
    document.getElementById('payModal').classList.add('open');
}
</script>

<footer class="footer">
    <p>Workshop Management System (WMS) &bull; Parts Inventory &amp; Payment Settlement</p>
</footer>
</body>
</html>
