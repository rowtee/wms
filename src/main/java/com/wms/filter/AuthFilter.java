package com.wms.filter;

import com.wms.model.Staff;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Set;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"/*"})
public class AuthFilter implements Filter {

    // Public paths accessible without authentication
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/login",
            "/logout",
            "/css",
            "/api/health"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String reqPath = request.getRequestURI().substring(request.getContextPath().length());
        final String path = reqPath.isEmpty() ? "/" : reqPath;

        // Check if path is public
        boolean isPublic = "/".equals(path) || PUBLIC_PATHS.stream().anyMatch(path::startsWith);
        if (isPublic) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);
        Staff user = (session != null) ? (Staff) session.getAttribute("loggedUser") : null;

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=session_expired");
            return;
        }

        // Module-level RBAC authorization
        String role = user.getRole(); // 'COUNTER', 'MECHANIC', 'MANAGER', 'ADMIN'

        if (path.startsWith("/mechanic") && !"MECHANIC".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/counter?error=unauthorized");
            return;
        }

        if (path.startsWith("/counter") && !"COUNTER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role) && !"MANAGER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/mechanic?error=unauthorized");
            return;
        }

        if (path.startsWith("/manager") && !"MANAGER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            if ("MECHANIC".equalsIgnoreCase(role)) {
                response.sendRedirect(request.getContextPath() + "/mechanic?error=unauthorized");
            } else {
                response.sendRedirect(request.getContextPath() + "/counter?error=unauthorized");
            }
            return;
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {}
}
