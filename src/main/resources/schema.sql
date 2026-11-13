-- ==============================================================================
-- Workshop Management System (WMS) - PostgreSQL 18 Schema (Max 6 Tables)
-- ==============================================================================

-- 1. Customers Table
CREATE TABLE IF NOT EXISTS customers (
    customer_id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Vehicles Table (No VIN Required for Fast Onboarding)
CREATE TABLE IF NOT EXISTS vehicles (
    vehicle_id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    plate_no VARCHAR(20) NOT NULL UNIQUE,
    make VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    manufacture_year INT NOT NULL,
    current_mileage INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicle_customer FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
);

-- 3. Staff Table (BCrypt Password Hashing)
CREATE TABLE IF NOT EXISTS staff (
    staff_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role VARCHAR(30) NOT NULL, -- 'COUNTER', 'MECHANIC', 'MANAGER', 'ADMIN'
    phone VARCHAR(30),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Unified Catalog Table (Combines Standard Service Packages & Spare Parts Inventory)
CREATE TABLE IF NOT EXISTS catalog_items (
    item_id BIGSERIAL PRIMARY KEY,
    item_type VARCHAR(20) NOT NULL, -- 'SERVICE' or 'PART'
    item_code VARCHAR(60) UNIQUE,   -- SKU / part number for parts, service code for services
    name VARCHAR(150) NOT NULL,
    category VARCHAR(60) NOT NULL,  -- 'Maintenance', 'Fluids', 'Brakes', 'Filters', etc.
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Selling price / labour charge in RM
    cost_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00, -- Cost in RM (for parts)
    stock_qty INT NOT NULL DEFAULT 0,                -- Stock quantity (for parts)
    reorder_level INT NOT NULL DEFAULT 0,            -- Low-stock threshold (for parts)
    interval_km INT DEFAULT 10000,                   -- Maintenance recommendation distance
    interval_months INT DEFAULT 6,                   -- Maintenance recommendation duration
    description VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. Job Orders Table (Includes Work Order Lifecycle & Invoicing / Billing)
CREATE TABLE IF NOT EXISTS job_orders (
    job_id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    advisor_id BIGINT NOT NULL,
    mechanic_id BIGINT,
    date_in TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    date_out TIMESTAMP,
    mileage_in INT NOT NULL,
    complaint VARCHAR(500) NOT NULL,
    diagnosis VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    next_service_date DATE,
    next_service_km INT,
    -- Merged Billing / Invoice Fields
    invoice_date DATE,
    subtotal DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    discount DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    total DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(30),
    paid_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_job_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(vehicle_id),
    CONSTRAINT fk_job_advisor FOREIGN KEY (advisor_id) REFERENCES staff(staff_id),
    CONSTRAINT fk_job_mechanic FOREIGN KEY (mechanic_id) REFERENCES staff(staff_id)
);

-- 6. Unified Job Items Table (Combines Services Performed & Spare Parts Consumed)
CREATE TABLE IF NOT EXISTS job_items (
    job_item_id BIGSERIAL PRIMARY KEY,
    job_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL, -- 'SERVICE' or 'PART'
    item_id BIGINT,                 -- References catalog_items(item_id)
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    notes VARCHAR(255),
    CONSTRAINT fk_job_item_job FOREIGN KEY (job_id) REFERENCES job_orders(job_id) ON DELETE CASCADE,
    CONSTRAINT fk_job_item_catalog FOREIGN KEY (item_id) REFERENCES catalog_items(item_id)
);

-- ==============================================================================
-- Initial Seed Data (Revamped 6-Table Architecture with Malaysian Context)
-- ==============================================================================

-- Staff Members (BCrypt Encrypted Passwords for: 123456)
INSERT INTO staff (staff_id, username, password_hash, full_name, role, phone, active) VALUES
(1, 'najwa', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Siti Najwa binti Ariffin (Front Desk)', 'COUNTER', '012-3456781', TRUE),
(2, 'adam', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Adam Shafiq bin Razak (Lead Tech)', 'MECHANIC', '012-3456782', TRUE),
(3, 'ajim', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Muhammad Hazim bin Zulkifli (Senior Tech)', 'MECHANIC', '012-3456783', TRUE),
(4, 'aina', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Nur Aina binti Kamaruddin (General Manager)', 'MANAGER', '012-3456780', TRUE),
(5, 'suresh', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Suresh a/l Mohan (Diagnostic Specialist)', 'MECHANIC', '016-8829103', TRUE),
(6, 'chong', '$2a$10$YJjwtjCCok0gXhTm6tlxneJSAmMGdDP0Apb5YogCebF2diqFprfNK', 'Chong Wei Loon (Suspension & Brakes)', 'MECHANIC', '017-4920194', TRUE)
ON CONFLICT (username) DO NOTHING;

SELECT setval('staff_staff_id_seq', (SELECT MAX(staff_id) FROM staff));

-- Customers (Authentic Malaysian Demographics & Addresses)
INSERT INTO customers (customer_id, full_name, phone, email, address) VALUES
(1, 'Ahmad Farhan bin Rosli', '012-3849102', 'farhan.rosli@gmail.com', 'No. 18, Jalan SS 2/64, 47300 Petaling Jaya, Selangor'),
(2, 'Tan Wei Loon', '016-4928173', 'tan.weiloon@yahoo.com', 'No. 52, Jalan Sutera 3, Taman Sentosa, 80150 Johor Bahru, Johor'),
(3, 'Priya a/p Subramaniam', '019-2819401', 'priya.subra@outlook.com', 'No. 7, Jalan USJ 11/3D, 47620 Subang Jaya, Selangor'),
(4, 'Muhammad Haziq bin Zulkifli', '013-8472910', 'haziq.zul@gmail.com', 'Lot 104, Kampung Melayu Majidee, 81100 Johor Bahru, Johor'),
(5, 'Siti Nur Aisyah binti Osman', '017-5930219', 'aisyah.osman@gmail.com', 'No. 23, Jalan Bukit Indah 5/2, 79100 Iskandar Puteri, Johor'),
(6, 'Dinesh a/l Mogan', '018-3920194', 'dinesh.mogan@hotmail.com', 'No. 88, Jalan Gasing, Seksyen 5, 46000 Petaling Jaya, Selangor'),
(7, 'Lee Jia Wei', '012-7719283', 'jiawei.lee@gmail.com', 'No. 12A, Jalan Song Thian Cheok, 93100 Kuching, Sarawak'),
(8, 'Nurul Izzah binti Mansor', '011-28491029', 'izzah.mansor@yahoo.com', 'No. 45, Lorong Serindit 4, Taman Eng Ann, 41150 Klang, Selangor'),
(9, 'Kavitha a/p Ramasamy', '016-8392011', 'kavitha.r@gmail.com', 'No. 6, Jalan Ipoh Jaya 2, 31350 Ipoh, Perak'),
(10, 'Chong Kok Seng', '017-3829104', 'kokseng.chong@gmail.com', 'No. 34, Jalan Melaka Raya 8, Taman Melaka Raya, 75000 Melaka'),
(11, 'Zainal Abidin bin Kassim', '019-3382910', 'zainal.kassim@gmail.com', 'No. 9, Jalan Setia Raja, Tabuan Jaya, 93350 Kuching, Sarawak'),
(12, 'Wong Siew Ling', '012-6649102', 'siewling.wong@outlook.com', 'No. 101, Jalan Jelutong, 11600 George Town, Pulau Pinang')
ON CONFLICT (customer_id) DO NOTHING;

SELECT setval('customers_customer_id_seq', (SELECT MAX(customer_id) FROM customers));

-- Vehicles (Popular Malaysian Models with Realistic Plate Numbers - No VIN)
INSERT INTO vehicles (vehicle_id, customer_id, plate_no, make, model, manufacture_year, current_mileage) VALUES
(1, 1, 'VCD 4821', 'Perodua', 'Myvi 1.5 AV', 2022, 38500),
(2, 2, 'JTC 5519', 'Proton', 'X50 1.5 TGDi Flagship', 2021, 52400),
(3, 3, 'BNL 7312', 'Honda', 'City 1.5V', 2020, 68200),
(4, 4, 'WVE 8834', 'Toyota', 'Vios 1.5G', 2019, 84100),
(5, 5, 'PKG 6109', 'Perodua', 'Bezza 1.3 X', 2023, 21300),
(6, 6, 'QAA 4902 B', 'Proton', 'Saga 1.3 Premium', 2022, 34800),
(7, 7, 'AKU 8291', 'Honda', 'Civic 1.5 RS Turbo', 2021, 49600),
(8, 8, 'WC 6128 K', 'Perodua', 'Ativa 1.0 Turbo AV', 2022, 39200),
(9, 9, 'VHQ 9012', 'Toyota', 'Corolla Altis 1.8G', 2020, 71500),
(10, 10, 'MBG 3319', 'Proton', 'Persona 1.6 Premium', 2018, 96400),
(11, 11, 'QS 7782 D', 'Toyota', 'Hilux 2.8 Rogue 4x4', 2021, 62300),
(12, 12, 'PNC 2291', 'Perodua', 'Alza 1.5 AV', 2023, 18900)
ON CONFLICT (vehicle_id) DO NOTHING;

SELECT setval('vehicles_vehicle_id_seq', (SELECT MAX(vehicle_id) FROM vehicles));

-- Catalog Items (Standard Maintenance Services 1-10, Genuine Parts & Consumables 11-23)
INSERT INTO catalog_items (item_id, item_type, item_code, name, category, unit_price, cost_price, stock_qty, reorder_level, interval_km, interval_months, description) VALUES
(1, 'SERVICE', 'SRV-OIL-01', 'Standard Oil & Filter Service', 'Maintenance', 45.00, 0.00, 0, 0, 10000, 6, 'Complete engine oil drain, filter replacement, fluid top-up and multi-point vehicle safety inspection.'),
(2, 'SERVICE', 'SRV-BRK-02', 'Brake Pad Replacement (Front/Rear)', 'Brakes', 85.00, 0.00, 0, 0, 30000, 18, 'Front or rear disc brake pad replacement, caliper pin lubrication, and rotor wear inspection.'),
(3, 'SERVICE', 'SRV-ALN-03', 'Wheel Alignment & Balancing', 'Suspension', 60.00, 0.00, 0, 0, 15000, 12, 'Computerized 4-wheel laser alignment, tire rotation, and dynamic wheel balancing.'),
(4, 'SERVICE', 'SRV-ENG-04', 'Major Engine Diagnostic & Tune-Up', 'Engine', 150.00, 0.00, 0, 0, 50000, 24, 'OBD-II scanner diagnostics, spark plug renewal, throttle body clean, and ECU sensor reset.'),
(5, 'SERVICE', 'SRV-AC-05', 'Air Conditioning Recharge & Flush', 'Climate', 75.00, 0.00, 0, 0, 20000, 12, 'R134a refrigerant recovery, vacuum leak test, compressor lubricant recharge, and cabin cooling test.'),
(6, 'SERVICE', 'SRV-CVT-06', 'CVT / Auto Transmission Fluid Service', 'Transmission', 80.00, 0.00, 0, 0, 40000, 24, 'Transmission fluid drain, pan clean, gasket inspection, and fluid replacement.'),
(7, 'SERVICE', 'SRV-CLT-07', 'Cooling System Flush & Radiator Service', 'Cooling', 55.00, 0.00, 0, 0, 40000, 24, 'Complete radiator flush, expansion tank cleaning, and long-life coolant replenishment.'),
(8, 'SERVICE', 'SRV-THB-08', 'Throttle Body & Fuel Injection Cleaning', 'Engine', 90.00, 0.00, 0, 0, 30000, 18, 'Decarbonize electronic throttle body, intake manifold clean, and idle air relearn calibration.'),
-- Spare Parts & Consumables
(11, 'PART', 'OIL-5W30-SYN', 'Fully Synthetic 5W-30 Motor Oil (4L)', 'Fluids', 145.00, 95.00, 48, 10, NULL, NULL, 'Premium grade 5W-30 API SP fully synthetic engine lubricant.'),
(12, 'PART', 'OIL-0W20-ECO', 'Fully Synthetic 0W-20 Eco Motor Oil (4L)', 'Fluids', 160.00, 105.00, 35, 10, NULL, NULL, 'Advanced fuel-saving 0W-20 synthetic oil for modern VVT/DVVT engines.'),
(13, 'PART', 'FLT-OIL-P2', 'Perodua Genuine OEM Oil Filter', 'Filters', 18.00, 9.50, 65, 15, NULL, NULL, 'OEM factory spin-on oil filter for Myvi, Bezza, Axia, Ativa, Alza.'),
(14, 'PART', 'FLT-OIL-PRO', 'Proton Genuine OEM Oil Filter', 'Filters', 20.00, 10.00, 52, 15, NULL, NULL, 'OEM factory oil filter for Saga, Persona, Iriz, X50, X70.'),
(15, 'PART', 'FLT-OIL-TY01', 'Toyota Genuine OEM Oil Filter', 'Filters', 28.00, 14.00, 42, 10, NULL, NULL, 'Genuine Toyota oil filter for Vios, Yaris, Altis, Camry.'),
(16, 'PART', 'BRK-PAD-MYVI', 'Bendix Ceramic Front Brake Pad Set (Perodua)', 'Brakes', 95.00, 55.00, 22, 6, NULL, NULL, 'High friction ceramic front brake pad set for Perodua vehicles.'),
(17, 'PART', 'BRK-PAD-X50', 'Brembo Front Brake Pad Set (Proton X50)', 'Brakes', 185.00, 115.00, 12, 5, NULL, NULL, 'High performance ceramic brake pads for Proton X50 / Geely Binyue.'),
(18, 'PART', 'BRK-PAD-HON', 'Akebono Front Brake Pad Set (City/Civic)', 'Brakes', 140.00, 80.00, 16, 5, NULL, NULL, 'OEM Japanese ceramic disc pads for Honda City and Civic.'),
(19, 'PART', 'SPK-PLG-IRID', 'NGK Laser Iridium Spark Plug (Set of 4)', 'Ignition', 160.00, 95.00, 18, 6, NULL, NULL, 'Ultra long-life laser welded iridium plugs for 100,000 km reliability.'),
(20, 'PART', 'FLT-AIR-CAB', 'Bosch Anti-Bacterial Cabin Filter', 'Filters', 45.00, 22.00, 28, 8, NULL, NULL, 'High efficiency PM2.5 activated carbon cabin filter.'),
(21, 'PART', 'FLD-CVT-HON', 'Honda Genuine HCF-2 CVT Fluid (3.5L)', 'Fluids', 155.00, 98.00, 15, 5, NULL, NULL, 'Factory specified second-generation continuous variable transmission fluid.'),
(22, 'PART', 'FLD-CVT-PRO', 'Proton Genuine CVTF-FE Transmission Fluid', 'Fluids', 140.00, 88.00, 3, 6, NULL, NULL, 'Genuine fuel economy transmission fluid for Punch/Punch Powertrain CVTs.'),
(23, 'PART', 'CLT-RAD-TY', 'Toyota Super Long Life Coolant 50/50 (4L)', 'Cooling', 65.00, 38.00, 4, 8, NULL, NULL, 'Factory premixed anti-rust and anti-freeze radiator coolant.')
ON CONFLICT (item_id) DO NOTHING;

SELECT setval('catalog_items_item_id_seq', (SELECT MAX(item_id) FROM catalog_items));

-- Job Orders (Realistic Malaysian Workshop Lifecycle: COMPLETED, IN_PROGRESS, AWAITING_PARTS, INVOICED, PENDING)
INSERT INTO job_orders (job_id, vehicle_id, advisor_id, mechanic_id, date_in, date_out, mileage_in, complaint, diagnosis, status, next_service_date, next_service_km, invoice_date, subtotal, tax, discount, total, payment_status, payment_method, paid_at) VALUES
-- Job 1: COMPLETED & PAID (Ahmad Farhan - Myvi)
(1, 1, 1, 2, CURRENT_TIMESTAMP - INTERVAL '3 day', CURRENT_TIMESTAMP - INTERVAL '2 day', 38500,
 'Routine 40,000 km periodic service and front brake pad inspection.',
 'Completed 40k service. Engine oil and OEM filter replaced. Front brake pads at 6mm, in safe condition.',
 'COMPLETED', CURRENT_DATE + INTERVAL '6 month', 48500,
 CURRENT_DATE - 2, 208.00, 12.48, 0.00, 220.48, 'PAID', 'DUITNOW_QR', CURRENT_TIMESTAMP - INTERVAL '2 day'),

-- Job 2: COMPLETED & PAID (Tan Wei Loon - Proton X50)
(2, 2, 1, 3, CURRENT_TIMESTAMP - INTERVAL '4 day', CURRENT_TIMESTAMP - INTERVAL '3 day', 52400,
 'High pitch squeaking noise when applying brakes at low speed.',
 'Front brake pads worn down to 1.5mm metal backing. Replaced with Brembo front ceramic pad set.',
 'COMPLETED', CURRENT_DATE + INTERVAL '6 month', 62400,
 CURRENT_DATE - 3, 270.00, 16.20, 10.00, 276.20, 'PAID', 'CREDIT_CARD', CURRENT_TIMESTAMP - INTERVAL '3 day'),

-- Job 3: COMPLETED & PAID (Priya - Honda City)
(3, 3, 1, 5, CURRENT_TIMESTAMP - INTERVAL '5 day', CURRENT_TIMESTAMP - INTERVAL '4 day', 68200,
 'Jerking sensation during acceleration from standstill; transmission warning lamp flashed once.',
 'CVT fluid degraded. Flushed and replaced with Honda HCF-2 fluid. Throttle body decarbonized.',
 'COMPLETED', CURRENT_DATE + INTERVAL '6 month', 78200,
 CURRENT_DATE - 4, 325.00, 19.50, 15.00, 329.50, 'PAID', 'ONLINE_TRANSFER', CURRENT_TIMESTAMP - INTERVAL '4 day'),

-- Job 4: INVOICED / AWAITING COUNTER PAYMENT (Muhammad Haziq - Toyota Vios)
(4, 4, 1, 6, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '2 hour', 84100,
 'Aircond blowing warm air when idling in traffic; steering wheel vibrating at 90 km/h.',
 'Low refrigerant recovered (210g remaining). Flushed, pressure tested OK and recharged. 4 wheels balanced and aligned.',
 'INVOICED', CURRENT_DATE + INTERVAL '6 month', 94100,
 CURRENT_DATE, 135.00, 8.10, 0.00, 143.10, 'PENDING', NULL, NULL),

-- Job 5: INVOICED / AWAITING COUNTER PAYMENT (Siti Nur Aisyah - Perodua Bezza)
(5, 5, 1, 2, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 hour', 21300,
 'Scheduled 20,000 km minor service with cabin odor treatment.',
 'Engine oil drained and filled with 0W-20 Eco. Perodua OEM filter and Bosch anti-bacterial cabin filter installed.',
 'INVOICED', CURRENT_DATE + INTERVAL '6 month', 31300,
 CURRENT_DATE, 223.00, 13.38, 5.00, 231.38, 'PENDING', NULL, NULL),

-- Job 6: IN_PROGRESS (Dinesh - Proton Saga)
(6, 6, 1, 3, CURRENT_TIMESTAMP - INTERVAL '6 hour', NULL, 34800,
 'Steering pulling sharply to the left on highway; routine oil service due.',
 'Inspecting steering tie rod ends and suspension bushings on lift 2.',
 'IN_PROGRESS', CURRENT_DATE + INTERVAL '6 month', 44800,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 7: IN_PROGRESS (Lee Jia Wei - Honda Civic RS Turbo)
(7, 7, 1, 5, CURRENT_TIMESTAMP - INTERVAL '4 hour', NULL, 49600,
 'Engine check light illuminated; rough cold start idling.',
 'OBD-II scanner error code P0301 (Cylinder 1 misfire). Removing ignition coils and checking spark plugs.',
 'IN_PROGRESS', CURRENT_DATE + INTERVAL '6 month', 59600,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 8: IN_PROGRESS (Nurul Izzah - Perodua Ativa)
(8, 8, 1, 6, CURRENT_TIMESTAMP - INTERVAL '3 hour', NULL, 39200,
 'Clunking noise from front right suspension when driving over speed bumps.',
 'Vehicle hoisted. Front suspension lower arm ball joint has slight play. Preparing replacement.',
 'IN_PROGRESS', CURRENT_DATE + INTERVAL '6 month', 49200,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 9: AWAITING_PARTS (Kavitha - Toyota Corolla Altis)
(9, 9, 1, 2, CURRENT_TIMESTAMP - INTERVAL '1 day', NULL, 71500,
 'Engine temperature gauge rising above middle line during traffic jams.',
 'Radiator upper hose has hairline crack and minor coolant seepage. Sourced replacement hose.',
 'AWAITING_PARTS', CURRENT_DATE + INTERVAL '6 month', 81500,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 10: AWAITING_PARTS (Chong Kok Seng - Proton Persona)
(10, 10, 1, 3, CURRENT_TIMESTAMP - INTERVAL '2 day', NULL, 96400,
 'CVT shuddering when shifting from Reverse to Drive; engine idling rough.',
 'Punch CVT clutch pack adaptation required after flushing CVTF fluid. Waiting for specialized fluid stock.',
 'AWAITING_PARTS', CURRENT_DATE + INTERVAL '6 month', 106400,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 11: PENDING (Zainal Abidin - Toyota Hilux Rogue)
(11, 11, 1, 5, CURRENT_TIMESTAMP - INTERVAL '1 hour', NULL, 62300,
 'Pre-roadtrip full 4x4 mechanical checkup, engine oil service, and brake fluid test.',
 NULL,
 'PENDING', CURRENT_DATE + INTERVAL '6 month', 72300,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL),

-- Job 12: PENDING (Wong Siew Ling - Perodua Alza)
(12, 12, 1, 6, CURRENT_TIMESTAMP - INTERVAL '30 minute', NULL, 18900,
 'Second scheduled service (20,000 km) and tire rotation.',
 NULL,
 'PENDING', CURRENT_DATE + INTERVAL '6 month', 28900,
 NULL, 0.00, 0.00, 0.00, 0.00, 'PENDING', NULL, NULL)
ON CONFLICT (job_id) DO NOTHING;

SELECT setval('job_orders_job_id_seq', (SELECT MAX(job_id) FROM job_orders));

-- Job Items (Line Items of Services Rendered and Parts Issued)
INSERT INTO job_items (job_id, item_type, item_id, quantity, unit_price, notes) VALUES
-- Job 1 Items (Ahmad Farhan - Myvi)
(1, 'SERVICE', 1, 1, 45.00, 'Routine 40,000 km service executed'),
(1, 'PART', 11, 1, 145.00, 'Installed Fully Synthetic 5W-30 Motor Oil (4L)'),
(1, 'PART', 13, 1, 18.00, 'Installed Perodua OEM Oil Filter'),

-- Job 2 Items (Tan Wei Loon - Proton X50)
(2, 'SERVICE', 2, 1, 85.00, 'Replaced front disc brake pads & lubricated caliper slides'),
(2, 'PART', 17, 1, 185.00, 'Installed Brembo Front Ceramic Brake Pad Set'),

-- Job 3 Items (Priya - Honda City)
(3, 'SERVICE', 6, 1, 80.00, 'Transmission drained and filled to specified level'),
(3, 'SERVICE', 8, 1, 90.00, 'Decarbonized electronic throttle body and calibrated idle speed'),
(3, 'PART', 21, 1, 155.00, 'Filled Honda Genuine HCF-2 CVT Fluid (3.5L)'),

-- Job 4 Items (Muhammad Haziq - Toyota Vios)
(4, 'SERVICE', 5, 1, 75.00, 'Refrigerant vacuum test and R134a recharge'),
(4, 'SERVICE', 3, 1, 60.00, 'Computerized 4-wheel laser alignment and dynamic balancing'),

-- Job 5 Items (Siti Nur Aisyah - Perodua Bezza)
(5, 'SERVICE', 1, 1, 45.00, '20,000 km periodic maintenance service'),
(5, 'PART', 12, 1, 160.00, 'Filled Fully Synthetic 0W-20 Eco Motor Oil (4L)'),
(5, 'PART', 13, 1, 18.00, 'Installed Perodua OEM Oil Filter'),

-- Job 6 Items (Dinesh - Proton Saga, In Progress)
(6, 'SERVICE', 1, 1, 45.00, 'Routine service underway'),
(6, 'SERVICE', 3, 1, 60.00, 'Wheel alignment queued'),

-- Job 7 Items (Lee Jia Wei - Honda Civic RS Turbo, In Progress)
(7, 'SERVICE', 4, 1, 150.00, 'Major OBD-II engine diagnosis and tune-up'),
(7, 'PART', 19, 1, 160.00, 'Reserved NGK Laser Iridium Spark Plug set')
ON CONFLICT DO NOTHING;
