package com.wms.controller;

import com.wms.dao.StaffDao;
import com.wms.model.Staff;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Optional;

@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout"})
public class AuthServlet extends HttpServlet {

    private final StaffDao staffDao = new StaffDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            response.sendRedirect(request.getContextPath() + "/login?msg=logged_out");
            return;
        }

        // Check if already logged in
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("loggedUser") != null) {
            Staff user = (Staff) session.getAttribute("loggedUser");
            redirectByRole(user, request, response);
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null) {
            request.setAttribute("error", "Username and password are required.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
            return;
        }

        Optional<Staff> staffOpt = staffDao.authenticate(username, password);
        if (staffOpt.isPresent()) {
            Staff user = staffOpt.get();
            HttpSession session = request.getSession(true);
            session.setAttribute("loggedUser", user);
            session.setAttribute("userRole", user.getRole());
            session.setAttribute("userName", user.getFullName());

            redirectByRole(user, request, response);
        } else {
            request.setAttribute("error", "Invalid credentials. Please verify your username and password.");
            request.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(request, response);
        }
    }

    private void redirectByRole(Staff user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String role = user.getRole();
        if ("MECHANIC".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/mechanic");
        } else if ("COUNTER".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/counter");
        } else if ("MANAGER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect(request.getContextPath() + "/manager");
        } else {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}
