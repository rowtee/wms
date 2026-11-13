package com.wms.controller;

import com.wms.dao.*;
import com.wms.model.*;
import com.wms.service.PdfReportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "ManagerServlet", urlPatterns = {"/manager", "/manager/report"})
public class ManagerServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(ManagerServlet.class);

    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final InvoiceDao invoiceDao = new InvoiceDao();
    private final StaffDao staffDao = new StaffDao();
    private final PdfReportService pdfReportService = new PdfReportService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String statusFilter = request.getParameter("status");
        List<JobOrder> allJobs = jobOrderDao.findAll(statusFilter);
        DashboardKpi kpi = jobOrderDao.getDashboardKpis();

        // Monthly report parameters
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        String yearParam = request.getParameter("reportYear");
        String monthParam = request.getParameter("reportMonth");
        if (yearParam != null && !yearParam.isEmpty()) {
            try {
                currentYear = Integer.parseInt(yearParam);
            } catch (NumberFormatException ignored) {}
        }
        if (monthParam != null && !monthParam.isEmpty()) {
            try {
                currentMonth = Integer.parseInt(monthParam);
            } catch (NumberFormatException ignored) {}
        }

        MonthlyReport report = invoiceDao.getMonthlyReport(currentYear, currentMonth);

        // Check if PDF export is requested
        String exportParam = request.getParameter("export");
        String formatParam = request.getParameter("format");
        boolean isPdf = "pdf".equalsIgnoreCase(exportParam) || "pdf".equalsIgnoreCase(formatParam)
                || "/manager/report".equals(request.getServletPath());

        if (isPdf) {
            String filename = String.format("Workshop_Monthly_Report_%d_%02d.pdf", currentYear, currentMonth);
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            try {
                pdfReportService.generateMonthlyReportPdf(report, response.getOutputStream());
                response.getOutputStream().flush();
                return;
            } catch (Exception e) {
                logger.error("Failed to generate PDF report", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to render PDF report");
                return;
            }
        }

        request.setAttribute("allJobs", allJobs);
        request.setAttribute("mechanics", staffDao.findByRole("MECHANIC"));
        request.setAttribute("kpi", kpi);
        request.setAttribute("report", report);
        request.setAttribute("selectedYear", currentYear);
        request.setAttribute("selectedMonth", currentMonth);
        request.setAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");
        request.setAttribute("activeMenu", "manager");

        request.getRequestDispatcher("/WEB-INF/views/manager.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        if ("reassign".equals(action)) {
            String jobIdStr = request.getParameter("jobId");
            String mechanicIdStr = request.getParameter("mechanicId");
            if (jobIdStr != null && !jobIdStr.isEmpty()) {
                Long jobId = Long.parseLong(jobIdStr);
                Long mechanicId = (mechanicIdStr != null && !mechanicIdStr.isEmpty()) ? Long.parseLong(mechanicIdStr) : null;
                jobOrderDao.updateMechanic(jobId, mechanicId);
            }
        }
        String status = request.getParameter("status");
        String redirect = request.getContextPath() + "/manager" + (status != null && !status.isEmpty() ? "?status=" + status : "");
        response.sendRedirect(redirect);
    }
}
