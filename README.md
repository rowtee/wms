# Workshop Management System (WMS)

A robust Jakarta EE 10 / JSP web application for managing automotive workshop workflows, including customer registrations, vehicle profiles, technician assignments, service catalog billing, and end-to-end job order tracking.

---

## 🚀 Quick Start with Docker (Recommended)

Anyone can run this project locally without installing Java, Maven, or PostgreSQL manually. All dependencies and database tables with sample data are automatically provisioned.

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running (Windows / macOS / Linux).

### 1. Clone the Repository
```bash
git clone https://github.com/rowtee/wms.git
cd wms
```

### 2. Start the Application
Run the following command in the project root:
```bash
docker compose up --build
```

Docker will:
1. Build the application WAR file using Maven 3.9 and Eclipse Temurin JDK 21 in a clean build container.
2. Spin up a PostgreSQL 17 container and automatically run [`schema.sql`](file:///C:/Users/User/IdeaProjects/WMS/src/main/resources/schema.sql) to initialize all 6 tables and Malaysian sample data.
3. Deploy the application into Apache Tomcat 10.1 (Jakarta EE 10).
4. Expose the web app on port **8080**.

### 3. Access the Application
Open your web browser and navigate to:
- **Root URL**: [http://localhost:8080/](http://localhost:8080/)
- **Context URL**: [http://localhost:8080/WMS_war/](http://localhost:8080/WMS_war/)

---

## 🔑 Demo Login Credentials

The database is pre-seeded with staff accounts across different workshop roles:

| Username | Password | Full Name | Role | Department / Specialization |
| :--- | :--- | :--- | :--- | :--- |
| `najwa` | `123456` | Siti Najwa binti Razak | Service Advisor | Customer Service & Front Counter |
| `aina` | `123456` | Nur Aina binti Ismail | Workshop Manager | Operations & Scheduling |
| `adam` | `123456` | Muhammad Adam bin Yusof | Senior Mechanic | Diagnostics & Engine Overhaul |
| `ajim` | `123456` | Hazim bin Hamdan | Mechanic | Periodic Maintenance & Brakes |
| `suresh` | `123456` | Suresh a/l Ramasamy | Senior Mechanic | Electrical, Hybrid & Air-Cond |
| `chong` | `123456` | Chong Wei Lun | Junior Mechanic | Suspension, Tires & Alignment |

---

## 🛠️ Stopping and Cleaning Up

To stop the containers:
```bash
docker compose down
```

To stop and remove persistent database volumes (fresh reset):
```bash
docker compose down -v
```

---

## 💻 Running Locally Without Docker (Manual Development)

### Requirements
- **JDK 21** (Eclipse Temurin or OpenJDK)
- **Apache Maven 3.9+** (or use the included `./mvnw` / `mvnw.cmd` wrapper)
- **PostgreSQL 15+** running on `localhost:5432` with a database named `wms_db` (username `postgres`, password `postgres`)
- **Apache Tomcat 10.1+** (Jakarta EE 10 compatible)

### Steps
1. Create database and load schema:
   ```bash
   psql -U postgres -c "CREATE DATABASE wms_db;"
   psql -U postgres -d wms_db -f src/main/resources/schema.sql
   ```
2. Build the WAR package:
   ```bash
   ./mvnw clean package
   ```
3. Deploy `target/wms-app.war` into Tomcat's `webapps/` folder as `ROOT.war` (or `WMS_war.war`).
4. Start Tomcat and visit `http://localhost:8080/`.

---

## 🗄️ Database Architecture (6 Normalized Tables)

The database strictly adheres to the 6-table workshop domain model:
1. **`customers`**: Customer contact records and Malaysian addresses.
2. **`vehicles`**: Customer vehicles (plates, models, VIN, engine numbers, mileage).
3. **`staff`**: Staff profiles, roles (Service Advisor, Mechanic, Manager), and auth credentials.
4. **`catalog_items`**: Standard repair services, inspections, and consumable replacement parts.
5. **`job_orders`**: Work orders linking customer, vehicle, assigned technician, status, and costs.
6. **`job_items`**: Line items detailing services and parts rendered per job order with unit prices.

---

## 📦 Tech Stack

- **Platform**: Jakarta EE 10 (Servlet 6.0, JSP 3.1, JSTL 3.0)
- **Java**: JDK 21 (LTS)
- **Database**: PostgreSQL 17 / 18
- **Connection Pool**: HikariCP 5.1.0
- **Application Server**: Apache Tomcat 10.1
- **Styling**: Bootstrap 5 + Vanilla CSS
- **Orchestration**: Docker & Docker Compose
