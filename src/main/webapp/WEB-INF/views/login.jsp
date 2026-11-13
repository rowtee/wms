<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Workshop Management System - Login</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        .login-wrapper {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%);
            padding: 1.5rem;
        }
        .login-card {
            background: #ffffff;
            border-radius: 12px;
            max-width: 440px;
            width: 100%;
            padding: 2.5rem 2rem;
            box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.3), 0 10px 10px -5px rgba(0, 0, 0, 0.2);
        }
        .login-brand {
            display: flex;
            align-items: center;
            gap: 0.75rem;
            justify-content: center;
            margin-bottom: 1.5rem;
        }
        .login-brand h1 {
            font-size: 1.5rem;
            color: #0f172a;
        }
    </style>
</head>
<body style="background:none;">
<div class="login-wrapper">
    <div class="login-card">
        <div class="login-brand">
            <div class="brand-icon" style="width:42px; height:42px; font-size:1.4rem;">🔧</div>
            <div>
                <h1 style="line-height:1.2;">WorkshopMS</h1>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div style="background:#fef2f2; border:1px solid #fecaca; color:#991b1b; padding:0.75rem; border-radius:6px; font-size:0.875rem; margin-bottom:1rem;">
                ⚠️ ${error}
            </div>
        </c:if>

        <c:if test="${param.msg == 'logged_out'}">
            <div style="background:#ecfdf5; border:1px solid #a7f3d0; color:#065f46; padding:0.75rem; border-radius:6px; font-size:0.875rem; margin-bottom:1rem;">
                ✓ You have been logged out securely.
            </div>
        </c:if>

        <form action="<%= request.getContextPath() %>/login" method="POST">
            <div class="form-group">
                <label>Username / Staff ID</label>
                <input type="text" id="username" name="username" class="form-control" placeholder="Enter username..." required autofocus>
            </div>

            <div class="form-group">
                <label>Password</label>
                <input type="password" id="password" name="password" class="form-control" placeholder="••••••••" required>
            </div>

            <button type="submit" class="btn btn-primary" style="width:100%; justify-content:center; padding:0.75rem; margin-top:0.75rem;">
                Sign In &rarr;
            </button>
        </form>
    </div>
</div>
</body>
</html>
