package com.wms.controller;

import com.wms.dao.InvoiceDao;
import com.wms.dao.PartDao;
import com.wms.model.Invoice;
import com.wms.model.Part;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "InventoryBillingServlet", urlPatterns = {"/inventory-billing"})
public class InventoryBillingServlet extends HttpServlet {

    private final PartDao partDao = new PartDao();
    private final InvoiceDao invoiceDao = new InvoiceDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String tab = request.getParameter("tab");
        if (tab == null || tab.isEmpty()) {
            tab = "inventory";
        }

        List<Part> parts = partDao.findAll();
        List<Invoice> invoices = invoiceDao.findAll();

        request.setAttribute("parts", parts);
        request.setAttribute("invoices", invoices);
        request.setAttribute("activeTab", tab);
        request.setAttribute("activeMenu", "inventory-billing");

        request.getRequestDispatcher("/WEB-INF/views/inventory-billing.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if ("restock".equals(action)) {
            Long partId = Long.parseLong(request.getParameter("partId"));
            int qty = Integer.parseInt(request.getParameter("quantity"));
            partDao.updateStock(partId, qty);
            response.sendRedirect(request.getContextPath() + "/inventory-billing?tab=inventory&success=restocked");
            return;
        } else if ("createPart".equals(action)) {
            String partNo = request.getParameter("partNo");
            String name = request.getParameter("name");
            String category = request.getParameter("category");
            BigDecimal unitPrice = new BigDecimal(request.getParameter("unitPrice"));
            BigDecimal costPrice = new BigDecimal(request.getParameter("costPrice"));
            int stockQty = Integer.parseInt(request.getParameter("stockQty"));
            int reorderLevel = Integer.parseInt(request.getParameter("reorderLevel"));

            Part part = new Part(null, partNo, name, category, unitPrice, costPrice, stockQty, reorderLevel, null);
            partDao.create(part);
            response.sendRedirect(request.getContextPath() + "/inventory-billing?tab=inventory&success=partCreated");
            return;
        } else if ("payInvoice".equals(action)) {
            Long invoiceId = Long.parseLong(request.getParameter("invoiceId"));
            String paymentMethod = request.getParameter("paymentMethod");
            invoiceDao.markPaid(invoiceId, paymentMethod);
            response.sendRedirect(request.getContextPath() + "/inventory-billing?tab=billing&success=paid");
            return;
        }

        response.sendRedirect(request.getContextPath() + "/inventory-billing");
    }
}
