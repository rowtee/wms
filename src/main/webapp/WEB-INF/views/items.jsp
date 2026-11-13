<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Warehouse Inventory - WMS</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>
<div class="container">
    <header class="header">
        <h1>Inventory Items</h1>
        <p>Managed via Jakarta EE Controller (HomeServlet) &amp; ItemService</p>
        <div class="badge-bar">
            <span class="badge">Total Items: ${totalItems}</span>
            <span class="badge">MVC Pattern</span>
            <span class="badge">JSTL Enabled</span>
        </div>
    </header>

    <div class="card">
        <h2>Warehouse Inventory Records</h2>
        <div class="table-responsive">
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>SKU</th>
                        <th>Name</th>
                        <th>Category</th>
                        <th>Quantity</th>
                        <th>Unit Price</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="item" items="${items}">
                        <tr>
                            <td>${item.id}</td>
                            <td><code>${item.sku}</code></td>
                            <td><strong>${item.name}</strong></td>
                            <td><span class="badge" style="background:#e2e8f0; color:#334155;">${item.category}</span></td>
                            <td>${item.quantity}</td>
                            <td>RM ${item.unitPrice}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty items}">
                        <tr>
                            <td colspan="6" style="text-align: center; color: var(--text-secondary); padding: 2rem;">
                                No inventory items found.
                            </td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>

        <div class="actions">
            <a href="<%= request.getContextPath() %>/" class="btn btn-secondary">&larr; Back to Home</a>
        </div>
    </div>
</div>
</body>
</html>
