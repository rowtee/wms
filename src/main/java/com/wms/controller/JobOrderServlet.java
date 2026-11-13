package com.wms.controller;

import com.wms.dao.JobOrderDao;
import com.wms.dao.StaffDao;
import com.wms.dao.VehicleDao;
import com.wms.model.JobOrder;
import com.wms.model.Staff;
import com.wms.model.Vehicle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@WebServlet(name = "JobOrderServlet", urlPatterns = {"/jobs"})
public class JobOrderServlet extends HttpServlet {

    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final VehicleDao vehicleDao = new VehicleDao();
    private final StaffDao staffDao = new StaffDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String status = request.getParameter("status");
        List<JobOrder> jobs = jobOrderDao.findAll(status);
        List<Vehicle> vehicles = vehicleDao.findAll();
        List<Staff> advisors = staffDao.findByRole("ADVISOR");
        List<Staff> mechanics = staffDao.findByRole("MECHANIC");

        request.setAttribute("jobs", jobs);
        request.setAttribute("vehicles", vehicles);
        request.setAttribute("advisors", advisors);
        request.setAttribute("mechanics", mechanics);
        request.setAttribute("selectedStatus", status != null ? status : "ALL");
        request.setAttribute("activeMenu", "jobs");

        request.getRequestDispatcher("/WEB-INF/views/jobs.jsp").forward(request, response);
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
            String redirect = request.getParameter("redirect");
            if ("dashboard".equals(redirect)) {
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                String status = request.getParameter("status");
                response.sendRedirect(request.getContextPath() + "/jobs" + (status != null && !status.isEmpty() ? "?status=" + status : ""));
            }
            return;
        }

        Long vehicleId = Long.parseLong(request.getParameter("vehicleId"));
        Long advisorId = Long.parseLong(request.getParameter("advisorId"));
        String mechanicIdStr = request.getParameter("mechanicId");
        Long mechanicId = (mechanicIdStr != null && !mechanicIdStr.isEmpty()) ? Long.parseLong(mechanicIdStr) : null;
        int mileageIn = Integer.parseInt(request.getParameter("mileageIn"));
        String complaint = request.getParameter("complaint");
        String diagnosis = request.getParameter("diagnosis");

        JobOrder job = new JobOrder();
        job.setVehicleId(vehicleId);
        job.setAdvisorId(advisorId);
        job.setMechanicId(mechanicId);
        job.setMileageIn(mileageIn);
        job.setComplaint(complaint);
        job.setDiagnosis(diagnosis);
        job.setStatus("PENDING");
        job.setNextServiceDate(LocalDate.now().plusMonths(6));
        job.setNextServiceKm(mileageIn + 10000);

        Long createdId = jobOrderDao.create(job);

        if (createdId != null) {
            response.sendRedirect(request.getContextPath() + "/job-detail?id=" + createdId);
        } else {
            response.sendRedirect(request.getContextPath() + "/jobs?error=1");
        }
    }
}
