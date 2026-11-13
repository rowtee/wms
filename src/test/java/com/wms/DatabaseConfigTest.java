package com.wms;

import com.wms.config.DatabaseConfig;
import com.wms.dao.*;
import com.wms.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConfigTest {

    @BeforeAll
    static void setUp() {
        DatabaseConfig.initialize();
    }

    @AfterAll
    static void tearDown() {
        DatabaseConfig.shutdown();
    }

    @Test
    void testDatabaseConnection() throws SQLException {
        try (Connection connection = DatabaseConfig.getConnection()) {
            assertNotNull(connection, "Connection should not be null");
            assertFalse(connection.isClosed(), "Connection should be open");
        }
    }

    @Test
    void testCustomerAndVehicleSeeding() {
        CustomerDao customerDao = new CustomerDao();
        List<Customer> customers = customerDao.findAll();
        assertNotNull(customers);
        assertFalse(customers.isEmpty(), "Customers should be seeded");

        VehicleDao vehicleDao = new VehicleDao();
        List<Vehicle> vehicles = vehicleDao.findAll();
        assertNotNull(vehicles);
        assertFalse(vehicles.isEmpty(), "Vehicles should be seeded");
    }

    @Test
    void testJobOrdersAndKpis() {
        JobOrderDao jobOrderDao = new JobOrderDao();
        List<JobOrder> jobs = jobOrderDao.findAll(null);
        assertNotNull(jobs);
        assertFalse(jobs.isEmpty(), "Job orders should be seeded");

        DashboardKpi kpi = jobOrderDao.getDashboardKpis();
        assertNotNull(kpi);
        assertTrue(kpi.getActiveJobs() >= 1, "Should have active jobs");

        JobOrder first = jobs.get(0);
        assertTrue(jobOrderDao.updateMechanic(first.getJobId(), first.getMechanicId()), "updateMechanic should succeed");
    }

    @Test
    void testStaffAuthenticationAndRoles() {
        StaffDao staffDao = new StaffDao();
        List<Staff> staffList = staffDao.findAll();
        assertNotNull(staffList);
        assertFalse(staffList.isEmpty(), "Staff list should not be empty");
        
        Staff firstStaff = staffList.get(0);
        assertNotNull(firstStaff.getUsername());
        assertFalse(staffDao.authenticate(firstStaff.getUsername(), "wrong_password_test_fail").isPresent(), "Invalid password should fail");
    }

    @Test
    void testPlateSearchAndMechanicJobs() {
        VehicleDao vehicleDao = new VehicleDao();
        List<Vehicle> all = vehicleDao.findAll();
        System.out.println("Vehicles in DB: " + all.size());
        for (Vehicle v : all) {
            System.out.println("Found vehicle in DB: " + v.getPlateNo());
        }
        if (!all.isEmpty()) {
            String firstPlate = all.get(0).getPlateNo();
            assertTrue(vehicleDao.findByPlateNo(firstPlate).isPresent(), "Should find existing vehicle by plate");
        }
        assertFalse(vehicleDao.findByPlateNo("NON-EXISTENT").isPresent(), "Unknown plate should return empty");

        JobOrderDao jobOrderDao = new JobOrderDao();
        List<JobOrder> mechanicJobs = jobOrderDao.findByMechanicId(2L, null);
        assertNotNull(mechanicJobs);
        assertFalse(mechanicJobs.isEmpty(), "Mechanic 2 should have assigned jobs");
    }

    @Test
    void testMonthlyReportGeneration() {
        InvoiceDao invoiceDao = new InvoiceDao();
        MonthlyReport report = invoiceDao.getMonthlyReport(2026, 11);
        assertNotNull(report);
        assertNotNull(report.getTotalSales());
        assertNotNull(report.getMechanicPerformance());
    }

    @Test
    void testCustomerSearchByNameOrPhone() {
        CustomerDao customerDao = new CustomerDao();
        List<Customer> all = customerDao.findAll();
        assertFalse(all.isEmpty(), "Customers should exist");
        Customer first = all.get(0);

        List<Customer> byName = customerDao.searchByNameOrPhone(first.getFullName().substring(0, Math.min(3, first.getFullName().length())));
        assertFalse(byName.isEmpty(), "Should find customer by name substring");

        String phone = first.getPhone();
        String phoneSub = phone.length() >= 3 ? phone.substring(0, 3) : phone;
        List<Customer> byPhone = customerDao.searchByNameOrPhone(phoneSub);
        assertFalse(byPhone.isEmpty(), "Should find customer by phone substring");
    }

    @Test
    void testRoundRobinMechanicWithLeastCars() {
        StaffDao staffDao = new StaffDao();
        java.util.Optional<Staff> leastBusy = staffDao.findMechanicWithLeastJobs();
        assertTrue(leastBusy.isPresent(), "Should find least busy mechanic");
        assertEquals("MECHANIC", leastBusy.get().getRole());
    }

    @Test
    void testVinColumnDroppedFromVehiclesTable() throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getColumns(null, null, "vehicles", "vin")) {
                assertFalse(rs.next(), "Column 'vin' must not exist in vehicles table");
            }
        }
    }

    @Test
    void testMaximumSixTablesInDatabase() throws Exception {
        try (Connection conn = DatabaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<String> tables = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
            System.out.println("Current public tables in DB (" + tables.size() + "): " + tables);
            assertTrue(tables.size() <= 6, "Database must have at most 6 tables, found: " + tables.size() + " (" + tables + ")");
            assertEquals(6, tables.size(), "Database must have exactly 6 normalized tables");
            assertTrue(tables.contains("customers"), "Should contain customers table");
            assertTrue(tables.contains("vehicles"), "Should contain vehicles table");
            assertTrue(tables.contains("staff"), "Should contain staff table");
            assertTrue(tables.contains("catalog_items"), "Should contain catalog_items table");
            assertTrue(tables.contains("job_orders"), "Should contain job_orders table");
            assertTrue(tables.contains("job_items"), "Should contain job_items table");
        }
    }

    @Test
    void testConsolidatedCatalogAndJobItems() {
        ServiceTypeDao serviceTypeDao = new ServiceTypeDao();
        List<ServiceType> services = serviceTypeDao.findAll();
        assertNotNull(services);
        assertFalse(services.isEmpty(), "Unified catalog should return services");

        PartDao partDao = new PartDao();
        List<Part> parts = partDao.findAll();
        assertNotNull(parts);
        assertFalse(parts.isEmpty(), "Unified catalog should return parts");

        JobServiceDao jobServiceDao = new JobServiceDao();
        List<JobServiceItem> jobServices = jobServiceDao.findByJobId(1L);
        assertNotNull(jobServices);
        assertFalse(jobServices.isEmpty(), "Job items should return services for job 1");

        JobPartDao jobPartDao = new JobPartDao();
        List<JobPartItem> jobParts = jobPartDao.findByJobId(1L);
        assertNotNull(jobParts);
        assertFalse(jobParts.isEmpty(), "Job items should return parts for job 1");

        InvoiceDao invoiceDao = new InvoiceDao();
        List<Invoice> invoices = invoiceDao.findAll();
        assertNotNull(invoices);
        assertFalse(invoices.isEmpty(), "Invoices should be mapped from job_orders");
    }
}
