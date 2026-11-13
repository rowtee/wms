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
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@WebServlet(name = "MainCounterServlet", urlPatterns = {"/counter", "/counter/search-customer"})
public class MainCounterServlet extends HttpServlet {

    private final VehicleDao vehicleDao = new VehicleDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final StaffDao staffDao = new StaffDao();
    private final ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
    private final JobOrderDao jobOrderDao = new JobOrderDao();
    private final JobServiceDao jobServiceDao = new JobServiceDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Staff loggedUser = (session != null) ? (Staff) session.getAttribute("loggedUser") : null;
        if (loggedUser != null && "MANAGER".equalsIgnoreCase(loggedUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/manager");
            return;
        }

        String path = request.getServletPath();

        // AJAX search for existing customers by name or phone
        if ("/counter/search-customer".equals(path)) {
            String keyword = request.getParameter("q");
            List<Customer> matches = customerDao.searchByNameOrPhone(keyword);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            PrintWriter out = response.getWriter();
            out.print("[");
            for (int i = 0; i < matches.size(); i++) {
                Customer c = matches.get(i);
                out.print("{");
                out.print("\"customerId\":" + c.getCustomerId() + ",");
                out.print("\"fullName\":\"" + escapeJson(c.getFullName()) + "\",");
                out.print("\"phone\":\"" + escapeJson(c.getPhone()) + "\",");
                out.print("\"email\":\"" + escapeJson(c.getEmail() != null ? c.getEmail() : "") + "\",");
                out.print("\"address\":\"" + escapeJson(c.getAddress() != null ? c.getAddress() : "") + "\"");
                out.print("}");
                if (i < matches.size() - 1) out.print(",");
            }
            out.print("]");
            out.flush();
            return;
        }

        // Vehicle Plate Search
        String queryPlate = request.getParameter("plateNo");
        if (queryPlate != null && !queryPlate.trim().isEmpty()) {
            Optional<Vehicle> vOpt = vehicleDao.findByPlateNo(queryPlate);
            request.setAttribute("searchedPlate", queryPlate.trim().toUpperCase());
            if (vOpt.isPresent()) {
                request.setAttribute("foundVehicle", vOpt.get());
            } else {
                request.setAttribute("notFound", true);
            }
        }

        // Round robin recommendation: find mechanic with least active cars
        Optional<Staff> suggestedMechanic = staffDao.findMechanicWithLeastJobs();
        suggestedMechanic.ifPresent(staff -> request.setAttribute("suggestedMechanic", staff));

        request.setAttribute("mechanicsWorkload", staffDao.getMechanicsWorkload());
        request.setAttribute("mechanics", staffDao.findByRole("MECHANIC"));
        request.setAttribute("serviceTypes", serviceTypeDao.findAll());
        request.setAttribute("customers", customerDao.findAll());
        request.setAttribute("activeMenu", "counter");

        request.getRequestDispatcher("/WEB-INF/views/counter.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession(false);
        Staff loggedUser = (session != null) ? (Staff) session.getAttribute("loggedUser") : null;
        Long advisorId = loggedUser != null ? loggedUser.getStaffId() : 1L;

        String isNewVehicle = request.getParameter("isNewVehicle");
        Long vehicleId;

        if ("true".equalsIgnoreCase(isNewVehicle)) {
            // Register Customer if needed
            Long customerId;
            String customerOption = request.getParameter("customerOption"); // 'existing' or 'new'
            if ("existing".equals(customerOption)) {
                customerId = Long.parseLong(request.getParameter("customerId"));
            } else {
                String fullName = request.getParameter("newCustomerName");
                String phone = request.getParameter("newCustomerPhone");
                String email = request.getParameter("newCustomerEmail");
                String address = request.getParameter("newCustomerAddress");
                Customer c = new Customer(null, fullName, phone, email, address, null);
                customerId = customerDao.create(c);
            }

            // Create Vehicle without VIN
            String plateNo = request.getParameter("plateNo").trim().toUpperCase();
            String make = request.getParameter("make");
            String model = request.getParameter("model");
            int year = Integer.parseInt(request.getParameter("manufactureYear"));
            int mileage = Integer.parseInt(request.getParameter("mileageIn"));

            // No VIN required in customer onboarding
            Vehicle v = new Vehicle(null, customerId, plateNo, make, model, year, mileage, null);
            vehicleId = vehicleDao.create(v);
        } else {
            vehicleId = Long.parseLong(request.getParameter("vehicleId"));
            int mileageIn = Integer.parseInt(request.getParameter("mileageIn"));
            vehicleDao.updateMileage(vehicleId, mileageIn);
        }

        // Mechanic Assignment: Use specified or auto round-robin with least cars
        String mechanicIdStr = request.getParameter("mechanicId");
        Long mechanicId = null;
        if (mechanicIdStr != null && !mechanicIdStr.trim().isEmpty() && !mechanicIdStr.equals("auto")) {
            mechanicId = Long.parseLong(mechanicIdStr);
        } else {
            Optional<Staff> leastBusy = staffDao.findMechanicWithLeastJobs();
            if (leastBusy.isPresent()) {
                mechanicId = leastBusy.get().getStaffId();
            }
        }

        int mileageIn = Integer.parseInt(request.getParameter("mileageIn"));
        String complaint = request.getParameter("complaint");

        JobOrder job = new JobOrder();
        job.setVehicleId(vehicleId);
        job.setAdvisorId(advisorId);
        job.setMechanicId(mechanicId);
        job.setMileageIn(mileageIn);
        job.setComplaint(complaint);
        job.setStatus("PENDING");
        job.setNextServiceDate(LocalDate.now().plusMonths(6));
        job.setNextServiceKm(mileageIn + 10000);

        Long createdJobId = jobOrderDao.create(job);

        // Add selected services
        String[] selectedServiceIds = request.getParameterValues("selectedServices");
        if (selectedServiceIds != null && createdJobId != null) {
            for (String sId : selectedServiceIds) {
                Long serviceTypeId = Long.parseLong(sId);
                jobServiceDao.addServiceToJob(createdJobId, serviceTypeId, "Intake counter request");
            }
        }

        response.sendRedirect(request.getContextPath() + "/counter?createdJobId=" + createdJobId);
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}
