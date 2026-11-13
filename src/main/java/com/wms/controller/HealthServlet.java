package com.wms.controller;

import com.wms.config.DatabaseConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

/**
 * Health check endpoint providing application and database connectivity status.
 */
@WebServlet(name = "HealthServlet", urlPatterns = {"/api/health"})
public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        boolean dbOk = false;
        String dbError = null;

        try (Connection conn = DatabaseConfig.getConnection()) {
            dbOk = conn != null && !conn.isClosed();
        } catch (Exception e) {
            dbError = e.getMessage();
        }

        PrintWriter out = response.getWriter();
        out.print("{");
        out.print("\"status\":\"" + (dbOk ? "UP" : "DOWN") + "\",");
        out.print("\"application\":\"Warehouse Management System\",");
        out.print("\"javaVersion\":\"" + System.getProperty("java.version") + "\",");
        out.print("\"databaseConnected\":" + dbOk);
        if (dbError != null) {
            out.print(",\"error\":\"" + dbError.replace("\"", "\\\"") + "\"");
        }
        out.print("}");
        out.flush();
    }
}
