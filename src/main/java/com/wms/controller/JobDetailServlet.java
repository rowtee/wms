package com.wms.controller;

import com.wms.dao.*;
import com.wms.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "JobDetailServlet", urlPatterns = {"/job-detail"})
public class JobDetailServlet extends HttpServlet {

    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final JobServiceDao jobServiceDao = new JobServiceDao();
    private final JobPartDao jobPartDao = new JobPartDao();
    private final ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private final PartDao partDao = new PartDao();
    private final InvoiceDao invoiceDao = new InvoiceDao();
    private final StaffDao staffDao = new StaffDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/jobs");
            return;
        }

        Long jobId = Long.parseLong(idStr);
        Optional<JobOrder> jobOpt = jobOrderDao.findById(jobId);
        if (jobOpt.isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/jobs");
            return;
        }

        JobOrder job = jobOpt.get();
        List<JobServiceItem> services = jobServiceDao.findByJobId(jobId);
        List<JobPartItem> parts = jobPartDao.findByJobId(jobId);
        List<ServiceType> catalogServices = serviceTypeDao.findAll();
        List<Part> catalogParts = partDao.findAll();
        Optional<Invoice> existingInvoice = invoiceDao.findByJobId(jobId);

        BigDecimal laborTotal = services.stream()
                .map(JobServiceItem::getLabourCharge)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal partsTotal = parts.stream()
                .map(JobPartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal subtotal = laborTotal.add(partsTotal);
        BigDecimal taxRate = new BigDecimal("0.06"); // 6% service tax
        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        request.setAttribute("job", job);
        request.setAttribute("services", services);
        request.setAttribute("parts", parts);
        request.setAttribute("catalogServices", catalogServices);
        request.setAttribute("catalogParts", catalogParts);
        request.setAttribute("invoice", existingInvoice.orElse(null));
        request.setAttribute("laborTotal", laborTotal);
        request.setAttribute("partsTotal", partsTotal);
        request.setAttribute("subtotal", subtotal);
        request.setAttribute("tax", tax);
        request.setAttribute("total", total);
        request.setAttribute("mechanics", staffDao.findByRole("MECHANIC"));
        request.setAttribute("activeMenu", "jobs");

        request.getRequestDispatcher("/WEB-INF/views/job-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        Long jobId = Long.parseLong(request.getParameter("jobId"));

        if ("addService".equals(action)) {
            String[] serviceTypeIds = request.getParameterValues("serviceTypeIds");
            String singleServiceTypeId = request.getParameter("serviceTypeId");
            String notes = request.getParameter("notes");
            if (serviceTypeIds != null && serviceTypeIds.length > 0) {
                for (String sId : serviceTypeIds) {
                    if (sId != null && !sId.trim().isEmpty()) {
                        jobServiceDao.addServiceToJob(jobId, Long.parseLong(sId.trim()), notes);
                    }
                }
            } else if (singleServiceTypeId != null && !singleServiceTypeId.trim().isEmpty()) {
                jobServiceDao.addServiceToJob(jobId, Long.parseLong(singleServiceTypeId.trim()), notes);
            }
        } else if ("addPart".equals(action)) {
            Long partId = Long.parseLong(request.getParameter("partId"));
            int qty = Integer.parseInt(request.getParameter("quantity"));
            jobPartDao.addPartToJob(jobId, partId, qty);
        } else if ("updateStatus".equals(action)) {
            String status = request.getParameter("status");
            String diagnosis = request.getParameter("diagnosis");
            jobOrderDao.updateStatusAndDiagnosis(jobId, status, diagnosis);
        } else if ("reassignMechanic".equals(action)) {
            String mechanicIdStr = request.getParameter("mechanicId");
            Long mechanicId = (mechanicIdStr != null && !mechanicIdStr.isEmpty()) ? Long.parseLong(mechanicIdStr) : null;
            jobOrderDao.updateMechanic(jobId, mechanicId);
        } else if ("generateInvoice".equals(action)) {
            BigDecimal subtotal = new BigDecimal(request.getParameter("subtotal"));
            BigDecimal tax = new BigDecimal(request.getParameter("tax"));
            BigDecimal discount = new BigDecimal(request.getParameter("discount"));
            BigDecimal total = subtotal.add(tax).subtract(discount);
            invoiceDao.createInvoiceForJob(jobId, subtotal, tax, discount, total);
        }

        response.sendRedirect(request.getContextPath() + "/job-detail?id=" + jobId);
    }
}
