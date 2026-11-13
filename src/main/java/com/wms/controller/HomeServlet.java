package com.wms.controller;

import com.wms.model.Item;
import com.wms.service.ItemService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for displaying warehouse items and home dashboard.
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/items"})
public class HomeServlet extends HttpServlet {

    private final ItemService itemService = new ItemService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<Item> items = itemService.getAllItems();
        long totalItems = itemService.getItemCount();

        request.setAttribute("items", items);
        request.setAttribute("totalItems", totalItems);

        request.getRequestDispatcher("/WEB-INF/views/items.jsp").forward(request, response);
    }
}
