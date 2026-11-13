<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Customer &amp; Vehicle Registry - WMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Customer &amp; Vehicle Registry</h1>
            <p>Master database of vehicle owners, client profiles, and registered automobiles.</p>
        </div>
        <div>
            <button onclick="document.getElementById('registerModal').classList.add('open')" class="btn btn-primary">
                + Add Customer &amp; Vehicle
            </button>
        </div>
    </div>

    <c:if test="${param.success != null}">
        <div class="card" style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:0.75rem 1rem; margin-bottom:1rem;">
            ✓ Customer and Vehicle recorded successfully in the database.
        </div>
    </c:if>

    <!-- Vehicles List -->
    <div class="card">
        <div class="card-header">
            <h2>Registered Vehicles</h2>
            <span class="badge" style="background:#e2e8f0; color:#334155;">${vehicles.size()} Vehicles Total</span>
        </div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>Plate No</th>
                        <th>Make &amp; Model</th>
                        <th>Year</th>
                        <th>Odometer (KM)</th>
                        <th>Customer / Owner</th>
                        <th>Contact Phone</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="v" items="${vehicles}">
                        <tr>
                            <td><strong><code>${v.plateNo}</code></strong></td>
                            <td>${v.make} ${v.model}</td>
                            <td>${v.year}</td>
                            <td>${v.currentMileage} km</td>
                            <td><strong>${v.customerName}</strong></td>
                            <td>${v.customerPhone}</td>
                            <td>
                                <a href="<%= request.getContextPath() %>/jobs?vehicleId=${v.vehicleId}" class="btn btn-secondary btn-sm">
                                    + Create Job
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty vehicles}">
                        <tr>
                            <td colspan="7" style="text-align:center; padding:2rem; color:var(--text-secondary);">
                                No vehicles registered yet.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>

    <!-- Customers Directory -->
    <div class="card">
        <div class="card-header">
            <h2>Client Profiles Directory</h2>
            <span class="badge" style="background:#e2e8f0; color:#334155;">${customers.size()} Clients Total</span>
        </div>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Full Name</th>
                        <th>Phone</th>
                        <th>Email</th>
                        <th>Billing / Home Address</th>
                        <th>Vehicles</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="c" items="${customers}">
                        <tr>
                            <td>#${c.customerId}</td>
                            <td><strong>${c.fullName}</strong></td>
                            <td>${c.phone}</td>
                            <td>${c.email}</td>
                            <td>${c.address}</td>
                            <td><span class="badge" style="background:#dbeafe; color:#1e40af;">${c.vehicleCount} Registered</span></td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>

<!-- Modal: Register Customer & Vehicle -->
<div id="registerModal" class="modal-overlay">
    <div class="modal-content">
        <div class="modal-header">
            <h3>Register New Customer &amp; Vehicle</h3>
            <button class="modal-close" onclick="document.getElementById('registerModal').classList.remove('open')">&times;</button>
        </div>
        <form action="<%= request.getContextPath() %>/customers" method="POST">
            <h4 style="margin-bottom:0.75rem; color:#1e293b; font-size:0.95rem;">1. Customer Details</h4>
            <div class="form-grid">
                <div class="form-group">
                    <label>Full Name *</label>
                    <input type="text" name="fullName" class="form-control" placeholder="e.g. David Beckham" required>
                </div>
                <div class="form-group">
                    <label>Phone Number *</label>
                    <input type="text" name="phone" class="form-control" placeholder="e.g. +1-555-9012" required>
                </div>
                <div class="form-group">
                    <label>Email Address</label>
                    <input type="email" name="email" class="form-control" placeholder="e.g. client@email.com">
                </div>
                <div class="form-group">
                    <label>Address</label>
                    <input type="text" name="address" class="form-control" placeholder="City, Street, Postal Code">
                </div>
            </div>

            <h4 style="margin:1rem 0 0.75rem; color:#1e293b; font-size:0.95rem; border-top:1px solid var(--border); padding-top:0.75rem;">
                2. Vehicle Details
            </h4>
            <div class="form-grid">
                <div class="form-group">
                    <label>License Plate No *</label>
                    <input type="text" name="plateNo" class="form-control" placeholder="e.g. WMS-9988" required>
                </div>
                <div class="form-group">
                    <label>Make (Brand) *</label>
                    <input type="text" name="make" class="form-control" placeholder="e.g. Toyota, BMW" required>
                </div>
                <div class="form-group">
                    <label>Model *</label>
                    <input type="text" name="model" class="form-control" placeholder="e.g. Corolla, 3 Series" required>
                </div>
                <div class="form-group">
                    <label>Manufacture Year *</label>
                    <input type="number" name="year" class="form-control" value="2022" required>
                </div>
                <div class="form-group">
                    <label>Current Mileage (KM)</label>
                    <input type="number" name="mileage" class="form-control" placeholder="e.g. 52000" value="0">
                </div>
            </div>

            <div style="display:flex; justify-content:flex-end; gap:0.5rem; margin-top:1.5rem;">
                <button type="button" class="btn btn-secondary" onclick="document.getElementById('registerModal').classList.remove('open')">Cancel</button>
                <button type="submit" class="btn btn-primary">Save Customer &amp; Vehicle</button>
            </div>
        </form>
    </div>
</div>

<footer class="footer">
    <p>Workshop Management System (WMS) &bull; Customers &amp; Fleet Management</p>
</footer>
</body>
</html>
