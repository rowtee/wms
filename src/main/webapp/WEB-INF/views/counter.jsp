<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Main Reception &amp; Counter - WorkshopMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        .plate-search-input {
            font-size: 1rem;
            text-transform: uppercase;
            font-weight: 600;
            letter-spacing: 0.05em;
        }
        .plate-search-input::placeholder {
            font-size: 0.875rem;
            font-weight: 400;
            text-transform: none;
            letter-spacing: normal;
            font-family: inherit;
            color: #94a3b8;
        }
        .service-option-label {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 0.65rem 0.85rem;
            border: 1px solid var(--border);
            border-radius: 6px;
            cursor: pointer;
            transition: all 0.2s;
            margin-bottom: 0.5rem;
            background: white;
        }
        .service-option-label:hover {
            border-color: var(--primary);
            background: #f0f7ff;
        }
        .mechanic-workload-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
            padding: 0.25rem 0.6rem;
            border-radius: 9999px;
            font-size: 0.78rem;
            font-weight: 600;
            background: #e2e8f0;
            color: #334155;
        }
        .search-results-box {
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: white;
            border: 1px solid var(--border);
            border-radius: 8px;
            box-shadow: var(--shadow-lg);
            max-height: 220px;
            overflow-y: auto;
            z-index: 1000;
            display: none;
        }
        .search-result-item {
            padding: 0.6rem 0.85rem;
            border-bottom: 1px solid #f1f5f9;
            cursor: pointer;
            transition: background 0.15s;
        }
        .search-result-item:hover {
            background: #eff6ff;
        }
    </style>
</head>
<body>
<jsp:include page="common/navbar.jsp" />

<div class="container">
    <div class="page-header">
        <div class="page-title">
            <h1>Front Desk &amp; Main Reception Counter</h1>
            <p>Vehicle check-in, license plate search, ticket creation, and automatic round-robin assignment based on lowest mechanic workload (Malaysia Base: RM &amp; 6% SST).</p>
        </div>
    </div>

    <c:if test="${param.createdJobId != null}">
        <div class="card" style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:1rem; margin-bottom:1.5rem; display:flex; justify-content:space-between; align-items:center;">
            <div>
                <strong>✓ Work Order #${param.createdJobId} opened successfully!</strong> Vehicle dispatched to workshop mechanic queue.
            </div>
            <a href="<%= request.getContextPath() %>/job-detail?id=${param.createdJobId}" class="btn btn-success btn-sm">
                View Job Sheet &rarr;
            </a>
        </div>
    </c:if>

    <!-- Round-Robin Mechanic Status Banner -->
    <div class="card" style="background:#f8fafc; border:1px solid var(--border); padding:1rem 1.25rem; margin-bottom:1.5rem;">
        <div style="display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:0.75rem;">
            <div>
                <span style="font-size:0.8rem; font-weight:700; color:#475569; text-transform:uppercase; letter-spacing:0.05em;">
                    🔄 Automatic Round-Robin Workload Dispatch
                </span>
                <div style="font-size:0.9rem; color:#1e293b; margin-top:0.2rem;">
                    Next mechanic with least active cars: 
                    <strong style="color:var(--primary); font-size:1rem;">
                        ${suggestedMechanic != null ? suggestedMechanic.fullName : 'All Mechanics Ready'}
                    </strong>
                </div>
            </div>
            <div style="display:flex; gap:0.5rem; flex-wrap:wrap;">
                <c:forEach var="w" items="${mechanicsWorkload}">
                    <span class="mechanic-workload-badge">
                        👨‍🔧 ${w.fullName}: <strong>${w.activeCars} active cars</strong>
                    </span>
                </c:forEach>
            </div>
        </div>
    </div>

    <!-- Step 1: Search by License Plate -->
    <div class="card" style="box-shadow:var(--shadow-md);">
        <div class="card-header">
            <h2>🔍 1. Search Vehicle by Number Plate</h2>
        </div>
        <form action="<%= request.getContextPath() %>/counter" method="GET" style="display:flex; gap:0.75rem; max-width:620px;">
            <input type="text" name="plateNo" class="form-control plate-search-input" placeholder="Enter Plate No (e.g. WMS 7821)" value="${searchedPlate}" required autofocus>
            <button type="submit" class="btn btn-primary" style="padding:0 1.75rem; font-weight:600;">
                Search Plate
            </button>
        </form>
    </div>

    <!-- Step 2A: Car Exists in Database -->
    <c:if test="${not empty foundVehicle}">
        <div class="card" style="border: 2px solid var(--success); background:#f0fdf4;">
            <div class="card-header" style="border-bottom-color:#bbf7d0;">
                <div>
                    <span class="badge" style="background:#15803d; color:white; margin-bottom:0.25rem;">✓ Registered Vehicle Found</span>
                    <h2 style="color:#14532d; font-family:monospace; font-size:1.6rem;">${foundVehicle.plateNo}</h2>
                </div>
                <div style="text-align:right;">
                    <strong>Owner: ${foundVehicle.customerName}</strong><br>
                    <span style="font-size:0.85rem; color:#166534;">Phone: ${foundVehicle.customerPhone}</span>
                </div>
            </div>

            <!-- Intake Ticket Form for Existing Car -->
            <form action="<%= request.getContextPath() %>/counter" method="POST" id="existingCarForm">
                <input type="hidden" name="isNewVehicle" value="false">
                <input type="hidden" name="vehicleId" value="${foundVehicle.vehicleId}">

                <div class="form-grid" style="margin-bottom:1rem;">
                    <div class="form-group">
                        <label>Vehicle Make &amp; Model</label>
                        <input type="text" class="form-control" value="${foundVehicle.year} ${foundVehicle.make} ${foundVehicle.model}" disabled>
                    </div>
                    <div class="form-group">
                        <label>Last Recorded Odometer</label>
                        <input type="text" class="form-control" value="${foundVehicle.currentMileage} km" disabled>
                    </div>
                    <div class="form-group">
                        <label>Current Check-In Odometer (KM) *</label>
                        <input type="number" name="mileageIn" class="form-control" value="${foundVehicle.currentMileage + 100}" required>
                    </div>
                    <div class="form-group">
                        <label>Mechanic Assignment (Round-Robin) *</label>
                        <select name="mechanicId" class="form-control" required>
                            <option value="auto" selected>🔄 Auto: Round-Robin (Least Active Cars${suggestedMechanic != null ? ' - ' += suggestedMechanic.fullName : ''})</option>
                            <c:forEach var="m" items="${mechanics}">
                                <option value="${m.staffId}">${m.fullName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <div class="form-group">
                    <label>Customer Complaint / Description of Required Jobs *</label>
                    <textarea name="complaint" class="form-control" rows="3" placeholder="Describe symptoms, noise, warning lights, or requested periodic maintenance..." required></textarea>
                </div>

                <!-- Select Services / Packages with Live Price Calc (RM & SST) -->
                <div class="form-group">
                    <label style="font-weight:700; margin-bottom:0.5rem;">Select Required Jobs &amp; Standard Services:</label>
                    <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap:0.5rem;">
                        <label class="service-option-label" id="noServiceCard_existing" style="border:1px dashed #94a3b8; background:#f8fafc;">
                            <div>
                                <input type="checkbox" id="noService_existing" checked onchange="toggleNoService('existing')">
                                <strong style="margin-left:0.4rem; color:#475569;">No Standard Service (Diagnostics / Custom Work Only)</strong>
                            </div>
                            <span style="font-weight:700; color:#64748b;">RM 0.00</span>
                        </label>
                        <c:forEach var="st" items="${serviceTypes}">
                            <label class="service-option-label">
                                <div>
                                    <input type="checkbox" name="selectedServices" value="${st.serviceTypeId}" data-price="${st.labourCharge}" onchange="calculateTotal('existing')">
                                    <strong style="margin-left:0.4rem;">${st.name}</strong>
                                </div>
                                <span style="font-weight:700; color:var(--primary);">RM ${st.labourCharge}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <!-- Total Price Display (RM & SST) -->
                <div style="background:#e0f2fe; border:1px solid #bae6fd; border-radius:8px; padding:1.2rem; margin-top:1rem; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
                    <div>
                        <strong style="color:#0369a1; font-size:1.15rem;">Estimated Total Price:</strong>
                        <div style="font-size:0.85rem; color:#0c4a6e;">
                            Labor Subtotal: <strong id="existingSubtotal">RM 0.00</strong> &bull; SST (6%): <strong id="existingSst">RM 0.00</strong>
                        </div>
                    </div>
                    <div style="text-align:right;">
                        <span style="font-size:0.8rem; text-transform:uppercase; color:#0369a1; font-weight:700;">Total Including SST</span>
                        <div style="font-size:2rem; font-weight:800; color:#0284c7;" id="existingTotalPrice">RM 0.00</div>
                    </div>
                </div>

                <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1.5rem;">
                    <button type="submit" class="btn btn-primary" style="padding:0.75rem 1.75rem; font-size:1rem;">
                        Open Work Ticket &amp; Dispatch to Mechanic &rarr;
                    </button>
                </div>
            </form>
        </div>
    </c:if>

    <!-- Step 2B: Car NOT Found - Prompt to Register Car & Open Ticket -->
    <c:if test="${notFound}">
        <div class="card" style="border: 2px dashed #f59e0b; background:#fffbeb;">
            <div class="card-header" style="border-bottom-color:#fde68a;">
                <div>
                    <span class="badge badge-pending" style="margin-bottom:0.25rem;">New Vehicle</span>
                    <h2 style="color:#92400e;">Plate No: <code>${searchedPlate}</code> is not yet registered.</h2>
                    <p style="font-size:0.9rem; color:#b45309;">Register vehicle and client details without requiring a VIN number (omitted for fast customer onboarding).</p>
                </div>
            </div>

            <form action="<%= request.getContextPath() %>/counter" method="POST" id="newCarForm">
                <input type="hidden" name="isNewVehicle" value="true">
                <input type="hidden" name="plateNo" value="${searchedPlate}">

                <!-- Customer selection with Search Field for existing customers -->
                <h3 style="font-size:1.05rem; color:#1e293b; margin-bottom:0.75rem;">1. Customer / Owner Profile</h3>
                <div style="margin-bottom:1rem; display:flex; gap:1.5rem;">
                    <label style="cursor:pointer; font-weight:600;">
                        <input type="radio" name="customerOption" value="new" checked onclick="toggleCustomerMode('new')"> 
                        Register New Customer
                    </label>
                    <label style="cursor:pointer; font-weight:600;">
                        <input type="radio" name="customerOption" value="existing" onclick="toggleCustomerMode('existing')"> 
                        Link to Existing Customer (Search by Name or Phone)
                    </label>
                </div>

                <!-- Mode A: New Customer Form (No VIN) -->
                <div id="newCustomerFields" class="form-grid">
                    <div class="form-group">
                        <label>Customer Full Name *</label>
                        <input type="text" name="newCustomerName" class="form-control" placeholder="e.g. Alex Tan, David Wong">
                    </div>
                    <div class="form-group">
                        <label>Phone Number *</label>
                        <input type="text" name="newCustomerPhone" class="form-control" placeholder="e.g. 012-3456789">
                    </div>
                    <div class="form-group">
                        <label>Email Address</label>
                        <input type="email" name="newCustomerEmail" class="form-control" placeholder="client@email.com">
                    </div>
                    <div class="form-group">
                        <label>Address / City</label>
                        <input type="text" name="newCustomerAddress" class="form-control" placeholder="Kuala Lumpur, Petaling Jaya...">
                    </div>
                </div>

                <!-- Mode B: Search field of Name or Phone Number for Existing Customer -->
                <div id="existingCustomerField" style="display:none; background:#ffffff; border:1px solid #fed7aa; padding:1.25rem; border-radius:8px; margin-bottom:1rem;">
                    <input type="hidden" id="selectedCustomerId" name="customerId" value="">
                    
                    <div class="form-group" style="position:relative;">
                        <label><strong>Search Existing Customer (by Name or Phone Number):</strong></label>
                        <input type="text" id="custSearchInput" class="form-control" placeholder="Type customer name (e.g. Alice) or phone (e.g. 012 / 555)..." oninput="searchCustomers(this.value)" autocomplete="off">
                        
                        <div id="searchResultsBox" class="search-results-box"></div>
                    </div>

                    <div id="selectedCustAlert" style="display:none; background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:0.6rem 0.85rem; border-radius:6px; font-size:0.9rem;">
                        Linked to: <strong id="selectedCustName"></strong> (<span id="selectedCustPhone"></span>)
                    </div>
                </div>

                <!-- Vehicle Details (VIN Omitted) -->
                <h3 style="font-size:1.05rem; color:#1e293b; margin:1.25rem 0 0.75rem; border-top:1px solid #fed7aa; padding-top:0.75rem;">
                    2. Vehicle Specifications (No VIN Required)
                </h3>
                <div class="form-grid">
                    <div class="form-group">
                        <label>Make (Brand) *</label>
                        <input type="text" name="make" class="form-control" placeholder="e.g. Perodua, Proton, Toyota, Honda" required>
                    </div>
                    <div class="form-group">
                        <label>Model *</label>
                        <input type="text" name="model" class="form-control" placeholder="e.g. Myvi, Saga, City, Vios, X50" required>
                    </div>
                    <div class="form-group">
                        <label>Manufacture Year *</label>
                        <input type="number" name="manufactureYear" class="form-control" value="2022" required>
                    </div>
                    <div class="form-group">
                        <label>Check-in Odometer (KM) *</label>
                        <input type="number" name="mileageIn" class="form-control" placeholder="e.g. 45000" required>
                    </div>
                    <div class="form-group">
                        <label>Mechanic Assignment (Round-Robin Least Cars) *</label>
                        <select name="mechanicId" class="form-control" required>
                            <option value="auto" selected>🔄 Auto: Round-Robin (Least Active Cars${suggestedMechanic != null ? ' - ' += suggestedMechanic.fullName : ''})</option>
                            <c:forEach var="m" items="${mechanics}">
                                <option value="${m.staffId}">${m.fullName}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <!-- Work Description & Services -->
                <h3 style="font-size:1.05rem; color:#1e293b; margin:1.25rem 0 0.75rem; border-top:1px solid #fed7aa; padding-top:0.75rem;">
                    3. Description of Required Work &amp; Pricing (RM &amp; 6% SST)
                </h3>
                <div class="form-group">
                    <label>Customer Complaint / Required Jobs Description *</label>
                    <textarea name="complaint" class="form-control" rows="3" placeholder="Describe symptoms or maintenance requirements specified by customer..." required></textarea>
                </div>

                <div class="form-group">
                    <label style="font-weight:700; margin-bottom:0.5rem;">Select Required Jobs &amp; Standard Services:</label>
                    <div style="display:grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap:0.5rem;">
                        <label class="service-option-label" id="noServiceCard_new" style="border:1px dashed #94a3b8; background:#f8fafc;">
                            <div>
                                <input type="checkbox" id="noService_new" checked onchange="toggleNoService('new')">
                                <strong style="margin-left:0.4rem; color:#475569;">No Standard Service (Diagnostics / Custom Work Only)</strong>
                            </div>
                            <span style="font-weight:700; color:#64748b;">RM 0.00</span>
                        </label>
                        <c:forEach var="st" items="${serviceTypes}">
                            <label class="service-option-label">
                                <div>
                                    <input type="checkbox" name="selectedServices" value="${st.serviceTypeId}" data-price="${st.labourCharge}" onchange="calculateTotal('new')">
                                    <strong style="margin-left:0.4rem;">${st.name}</strong>
                                </div>
                                <span style="font-weight:700; color:var(--primary);">RM ${st.labourCharge}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <!-- Total Price Display (RM & SST) -->
                <div style="background:#e0f2fe; border:1px solid #bae6fd; border-radius:8px; padding:1.2rem; margin-top:1rem; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:1rem;">
                    <div>
                        <strong style="color:#0369a1; font-size:1.15rem;">Estimated Total Price:</strong>
                        <div style="font-size:0.85rem; color:#0c4a6e;">
                            Labor Subtotal: <strong id="newSubtotal">RM 0.00</strong> &bull; SST (6%): <strong id="newSst">RM 0.00</strong>
                        </div>
                    </div>
                    <div style="text-align:right;">
                        <span style="font-size:0.8rem; text-transform:uppercase; color:#0369a1; font-weight:700;">Total Including SST</span>
                        <div style="font-size:2rem; font-weight:800; color:#0284c7;" id="newTotalPrice">RM 0.00</div>
                    </div>
                </div>

                <div style="display:flex; justify-content:flex-end; gap:0.75rem; margin-top:1.5rem;">
                    <button type="submit" class="btn btn-primary" style="padding:0.75rem 1.75rem; font-size:1rem;">
                        Register Vehicle, Open Ticket &amp; Dispatch to Mechanic &rarr;
                    </button>
                </div>
            </form>
        </div>
    </c:if>
</div>

<script>
function toggleNoService(mode) {
    var form = (mode === 'existing') ? document.getElementById('existingCarForm') : document.getElementById('newCarForm');
    var noServiceCb = document.getElementById(mode === 'existing' ? 'noService_existing' : 'noService_new');
    var checkboxes = form.querySelectorAll('input[name="selectedServices"]');
    
    if (noServiceCb && noServiceCb.checked) {
        checkboxes.forEach(function(cb) {
            cb.checked = false;
        });
    }
    calculateTotal(mode);
}

function calculateTotal(mode) {
    var form = (mode === 'existing') ? document.getElementById('existingCarForm') : document.getElementById('newCarForm');
    var checkboxes = form.querySelectorAll('input[name="selectedServices"]:checked');
    var noServiceCb = document.getElementById(mode === 'existing' ? 'noService_existing' : 'noService_new');
    
    if (checkboxes.length > 0 && noServiceCb) {
        noServiceCb.checked = false;
    } else if (checkboxes.length === 0 && noServiceCb) {
        noServiceCb.checked = true;
    }

    var subtotal = 0.0;
    checkboxes.forEach(function(cb) {
        subtotal += parseFloat(cb.getAttribute('data-price') || 0);
    });
    var sst = subtotal * 0.06;
    var grandTotal = subtotal + sst;

    if (mode === 'existing') {
        document.getElementById('existingSubtotal').innerText = 'RM ' + subtotal.toFixed(2);
        document.getElementById('existingSst').innerText = 'RM ' + sst.toFixed(2);
        document.getElementById('existingTotalPrice').innerText = 'RM ' + grandTotal.toFixed(2);
    } else {
        document.getElementById('newSubtotal').innerText = 'RM ' + subtotal.toFixed(2);
        document.getElementById('newSst').innerText = 'RM ' + sst.toFixed(2);
        document.getElementById('newTotalPrice').innerText = 'RM ' + grandTotal.toFixed(2);
    }
}

function toggleCustomerMode(mode) {
    if (mode === 'existing') {
        document.getElementById('newCustomerFields').style.display = 'none';
        document.getElementById('existingCustomerField').style.display = 'block';
    } else {
        document.getElementById('newCustomerFields').style.display = 'grid';
        document.getElementById('existingCustomerField').style.display = 'none';
    }
}

var searchTimeout = null;
function searchCustomers(query) {
    clearTimeout(searchTimeout);
    var box = document.getElementById('searchResultsBox');
    if (!query || query.trim().length < 2) {
        box.style.display = 'none';
        return;
    }

    searchTimeout = setTimeout(function() {
        fetch('<%= request.getContextPath() %>/counter/search-customer?q=' + encodeURIComponent(query))
            .then(function(res) { return res.json(); })
            .then(function(data) {
                if (data.length === 0) {
                    box.innerHTML = '<div style="padding:0.75rem; color:#64748b; font-size:0.875rem;">No customer found.</div>';
                    box.style.display = 'block';
                    return;
                }
                var html = '';
                data.forEach(function(c) {
                    html += '<div class="search-result-item" onclick="selectCustomer(' + c.customerId + ', \'' + escapeJs(c.fullName) + '\', \'' + escapeJs(c.phone) + '\')">' +
                            '<strong>' + c.fullName + '</strong> (' + c.phone + ')<br>' +
                            '<span style="font-size:0.75rem; color:#64748b;">' + (c.address || c.email || 'No address specified') + '</span>' +
                            '</div>';
                });
                box.innerHTML = html;
                box.style.display = 'block';
            });
    }, 250);
}

function selectCustomer(id, name, phone) {
    document.getElementById('selectedCustomerId').value = id;
    document.getElementById('custSearchInput').value = name + ' (' + phone + ')';
    document.getElementById('selectedCustName').innerText = name;
    document.getElementById('selectedCustPhone').innerText = phone;
    document.getElementById('selectedCustAlert').style.display = 'block';
    document.getElementById('searchResultsBox').style.display = 'none';
}

function escapeJs(str) {
    return (str || '').replace(/'/g, "\\'");
}

document.addEventListener('click', function(e) {
    if (!e.target.closest('#existingCustomerField')) {
        document.getElementById('searchResultsBox').style.display = 'none';
    }
});
</script>

<footer class="footer">
    <p>Workshop Management System &bull; Main Counter Reception Module (Malaysia Base: RM &amp; 6% SST)</p>
</footer>
</body>
</html>
