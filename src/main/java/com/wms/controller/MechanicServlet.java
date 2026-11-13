package com.wms.controller;

import com.wms.dao.*;
import com.wms.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MechanicServlet", urlPatterns = {"/mechanic", "/mechanic/update"})
public class MechanicServlet extends HttpServlet {

    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final JobServiceDao jobServiceDao = new JobServiceDao();
    private final JobPartDao jobPartDao = new JobPartDao();
    private final ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private final PartDao partDao = new PartDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Staff loggedUser = (session != null) ? (Staff) session.getAttribute("loggedUser") : null;
        if (loggedUser != null && "MANAGER".equalsIgnoreCase(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/manager");
            return;
        }

        Long mechanicId = loggedUser != null ? loggedUser.getStaffId() : 2L;
        String status = request.getParameter("status");

        List<JobOrder> myJobs = jobOrderDao.findByMechanicId(mechanicId, status);
        request.setAttribute("jobs", myJobs);
        request.setAttribute("selectedStatus", status != null ? status : "ALL");
        request.setAttribute("activeMenu", "mechanic");

        // If a specific job is selected to view details
        String jobIdStr = request.getParameter("jobId");
        if (jobIdStr != null && !jobIdStr.isEmpty()) {
            Long jobId = Long.parseLong(jobIdStr);
            Optional<JobOrder> jobOpt = jobOrderDao.findById(jobId);
            if (jobOpt.isPresent()) {
                request.setAttribute("selectedJob", jobOpt.get());
                request.setAttribute("jobServices", jobServiceDao.findByJobId(jobId));
                request.setAttribute("jobParts", jobPartDao.findByJobId(jobId));
                request.setAttribute("catalogServices", serviceTypeDao.findAll());
                request.setAttribute("catalogParts", partDao.findAll());
            }
        }

        request.getRequestDispatcher("/WEB-INF/views/mechanic.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        Long jobId = Long.parseLong(request.getParameter("jobId"));

        if ("updateStatus".equals(action)) {
            String status = request.getParameter("status");
            String diagnosis = request.getParameter("diagnosis");
            jobOrderDao.updateStatusAndDiagnosis(jobId, status, diagnosis);
        } else if ("addService".equals(action)) {
            Long serviceTypeId = Long.parseLong(request.getParameter("serviceTypeId"));
            String notes = request.getParameter("notes");
            jobServiceDao.addServiceToJob(jobId, serviceTypeId, notes);
        } else if ("addPart".equals(action)) {
            Long partId = Long.parseLong(request.getParameter("partId"));
            int qty = Integer.parseInt(request.getParameter("quantity"));
            jobPartDao.addPartToJob(jobId, partId, qty);
        }

        response.sendRedirect(request.getContextPath() + "/mechanic?jobId=" + jobId + "&updated=1");
    }
}
