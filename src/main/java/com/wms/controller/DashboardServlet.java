package com.wms.controller;

import com.wms.dao.JobOrderDao;
import com.wms.dao.StaffDao;
import com.wms.model.DashboardKpi;
import com.wms.model.JobOrder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final StaffDao staffDao = new StaffDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        DashboardKpi kpi = jobOrderDao.getDashboardKpis();
        List<JobOrder> recentJobs = jobOrderDao.findAll(null);
        if (recentJobs.size() > 5) {
            recentJobs = recentJobs.subList(0, 5);
        }

        request.setAttribute("kpi", kpi);
        request.setAttribute("recentJobs", recentJobs);
        request.setAttribute("mechanics", staffDao.findByRole("MECHANIC"));
        request.setAttribute("activeMenu", "dashboard");

        request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
    }
}
