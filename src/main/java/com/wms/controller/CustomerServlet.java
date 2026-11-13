package com.wms.controller;

import com.wms.dao.CustomerDao;
import com.wms.dao.VehicleDao;
import com.wms.model.Customer;
import com.wms.model.Vehicle;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/customers"})
public class CustomerServlet extends HttpServlet {

    private final CustomerDao customerDao = new CustomerDao();
    private final VehicleDao vehicleDao = new VehicleDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Customer> customers = customerDao.findAll();
        List<Vehicle> vehicles = vehicleDao.findAll();

        request.setAttribute("customers", customers);
        request.setAttribute("vehicles", vehicles);
        request.setAttribute("activeMenu", "customers");

        request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String fullName = request.getParameter("fullName");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");

        Customer customer = new Customer(null, fullName, phone, email, address, null);
        Long customerId = customerDao.create(customer);

        if (customerId != null) {
            String plateNo = request.getParameter("plateNo");
            String make = request.getParameter("make");
            String model = request.getParameter("model");
            String yearStr = request.getParameter("year");
            String mileageStr = request.getParameter("mileage");

            if (plateNo != null && !plateNo.trim().isEmpty()) {
                int year = 2022;
                int mileage = 0;
                try { if (yearStr != null) year = Integer.parseInt(yearStr.trim()); } catch (NumberFormatException ignored) {}
                try { if (mileageStr != null) mileage = Integer.parseInt(mileageStr.trim()); } catch (NumberFormatException ignored) {}

                Vehicle v = new Vehicle(null, customerId, plateNo, make, model, year, mileage, null);
                vehicleDao.create(v);
            }
        }

        response.sendRedirect(request.getContextPath() + "/customers?success=1");
    }
}
