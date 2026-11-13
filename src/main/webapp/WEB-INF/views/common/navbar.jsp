<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<nav class="navbar">
    <div class="nav-container">
        <a href="<%= request.getContextPath() %>/dashboard" class="brand">
            <div class="brand-icon">🔧</div>
            <span>WorkshopMS</span>
        </a>
        <ul class="nav-links">
            <c:if test="${sessionScope.userRole == 'MECHANIC' || sessionScope.userRole == 'ADMIN'}">
                <li>
                    <a href="<%= request.getContextPath() %>/mechanic" class="${activeMenu == 'mechanic' ? 'active' : ''}">
                        👨‍🔧 Mechanic Module
                    </a>
                </li>
            </c:if>

            <c:if test="${sessionScope.userRole == 'COUNTER' || sessionScope.userRole == 'ADMIN'}">
                <li>
                    <a href="<%= request.getContextPath() %>/counter" class="${activeMenu == 'counter' ? 'active' : ''}">
                        🛎️ Main Counter
                    </a>
                </li>
            </c:if>

            <c:if test="${sessionScope.userRole == 'MANAGER' || sessionScope.userRole == 'ADMIN'}">
                <li>
                    <a href="<%= request.getContextPath() %>/manager" class="${activeMenu == 'manager' ? 'active' : ''}">
                        📊 Manager Analytics
                    </a>
                </li>
            </c:if>

            <li>
                <a href="<%= request.getContextPath() %>/dashboard" class="${activeMenu == 'dashboard' ? 'active' : ''}">
                    📋 All Jobs
                </a>
            </li>

            <li>
                <a href="<%= request.getContextPath() %>/inventory-billing" class="${activeMenu == 'inventory-billing' ? 'active' : ''}">
                    📦 Parts &amp; Invoices
                </a>
            </li>
        </ul>

        <div style="display:flex; align-items:center; gap:0.75rem;">
            <c:if test="${not empty sessionScope.loggedUser}">
                <span style="font-size:0.85rem; color:#cbd5e1;">
                    👤 <strong>${sessionScope.userName}</strong>
                    <span class="badge" style="background:#334155; color:#93c5fd; margin-left:0.25rem;">${sessionScope.userRole}</span>
                </span>
                <a href="<%= request.getContextPath() %>/logout" class="btn btn-secondary btn-sm" style="background:#334155; color:white; border:none;">
                    Sign Out
                </a>
            </c:if>
            <c:if test="${empty sessionScope.loggedUser}">
                <a href="<%= request.getContextPath() %>/login" class="btn btn-primary btn-sm">Sign In</a>
            </c:if>
        </div>
    </div>
</nav>
