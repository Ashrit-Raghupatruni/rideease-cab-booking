# RideEase ("Toury" Edition) — Online Cab Booking Platform

A full-stack, enterprise-grade multi-portal online cab booking application built with **Java 17/21, Spring Boot 3.2.5, Spring Data JPA / Hibernate, MySQL 8.0, JavaFX Desktop Console, Cloudinary Media CDN, Thymeleaf, and WebSocket STOMP**.

---

## 🏛️ System Architecture

RideEase operates with a dedicated **3-port isolation architecture** hosted on an embedded Tomcat multi-connector server, along with a standalone **JavaFX Desktop Administrative & Fleet Console**:

| Role / Portal | Port | Direct URL | Responsibilities |
|---|---|---|---|
| **Passenger / Rider** | **`8080`** | [http://localhost:8080/](http://localhost:8080/) | Fullscreen Leaflet map, draggable floating bottom sheet, OSRM road routing, device GPS location accuracy, nearby driver availability count & map pins, saved places, tiered vehicle selection (Mini, Sedan, SUV XL), live WebSocket tracking, 24/7 Safety Centre with emergency SOS, scheduled bookings, and downloadable tax receipts. |
| **Driver Partner** | **`8081`** | [http://localhost:8081/driver/login](http://localhost:8081/driver/login) | Map-first partner terminal, online/offline duty switch, real-time incoming request countdown card, atomic ride acceptance, start trip OTP validation, step-by-step road navigation, dual-leg route visualization (Leg 1: Captain to Pickup, Leg 2: Pickup to Destination), live GPS broadcast, and document verification upload portal. |
| **Fleet Administrator** | **`8082`** | [http://localhost:8082/admin/login](http://localhost:8082/admin/login) | Command center with live fleet telemetry map, Chart.js 7-day revenue & volume analytics, driver KYC & Cloudinary document verification panel with approve/reject workflow, and live searchable Trip Operations Ledger. |
| **JavaFX Desktop Console** | **Desktop Client** | Run `RideEaseFXApp` | Native desktop administrative & dispatch application featuring rich Java Event Handling (ActionEvents, ChangeListeners, MouseEvents, TableView row selection with Cloudinary photo previews, and interactive charts). |

### Subproject Structure:
- **`backend/`:** Complete Spring Boot 3.2.5 enterprise web application with server-rendered Thymeleaf dynamic templates, WebSocket STOMP telemetry, Spring Data JPA, and MySQL persistence.
  - **`backend/src/main/java/com/rideease/javafx/`:** Native JavaFX Desktop Console (`RideEaseFXApp.java`) with FXML views and controllers.
- **`database/schema.sql`:** Complete MySQL 8.0 DDL relational schema for all tables (`users`, `drivers`, `vehicles`, `bookings`, `reviews`, `safety_contacts`).
- **`docs/`:** System design documents, architecture specifications, API flows, and relational schemas.
- **`android/`:** Native Android client architecture documentation.

---

## ✨ Features & Capabilities

### 1. 🗄️ Relational Database (MySQL 8.0 & Spring Data JPA)
- **Enterprise Relational Schema:** Tables for `users`, `drivers`, `vehicles`, `bookings`, `reviews`, and `safety_contacts`.
- **Automated Schema Generation:** Powered by Hibernate DDL auto-updates (`spring.jpa.hibernate.ddl-auto=update`).
- **Spherical Haversine Distance Engine:** High-performance SQL queries for nearby driver discovery within custom kilometer radii.
- **Health Check Endpoint (`/api/health/db`):** Live database connection and latency diagnostics.

### 2. 📄 Cloudinary Vehicle & Driver Document Verification
- **Driver Profile & Vehicle KYC:** Dedicated portal (`/driver/verification`) where drivers upload personal photos, vehicle photos, Driving License, RC Book, Insurance Policy, and Commercial Permit.
- **Cloudinary Media CDN:** Uploads are stored securely in categorized Cloudinary folders with thumbnail transformation and document viewing links.
- **Admin Review Workflow:** Admins can preview documents from Cloudinary at `/admin/verifications` and approve or reject applications with custom feedback notes.

### 3. 🖥️ JavaFX Desktop Dispatch & Fleet Management Console
- Native desktop interface for fleet operations with rich Java Event Handling:
  - **ActionEvents:** One-click approval, rejection, and database sync handlers.
  - **MouseEvents:** Table row clicks instantly load driver documents and profile pictures from Cloudinary into a preview inspector.
  - **Property Bindings & ChangeListeners:** Real-time reactive filtering across names, license plates, and vehicle categories.
  - **Concurrency Tasks:** Background asynchronous worker threads (`javafx.concurrent.Task`) with `setOnSucceeded` and `setOnFailed` event listeners.

### 4. 🗺️ Dual-Leg OSRM Route Visualization & Telemetry
- **Accurate Device Geolocation:** Both Rider and Driver interfaces request device GPS permissions with high accuracy (`enableHighAccuracy: true`).
- **Nearby Driver Discovery:** Displays the count and live map markers of available captains nearby.
- **Dual-Leg Trajectory:**
  - **Leg 1 (Dashed Amber Line):** Driver location to User Pickup point.
  - **Leg 2 (Solid Indigo Line):** User Pickup point to Destination Dropoff.
- **Targeted Driver Telemetry Update:**
  - For `driver@gmail.com`: Position automatically advances towards the passenger every **2 seconds** for live demonstration.
  - For all other drivers: Real-time device GPS is streamed continuously via `watchPosition`.

---

## ⚙️ Environment Configuration

Configure `backend/.env` (or set environment variables):

```env
# Server Ports
PORT=8080
PORT_DRIVER=8081
PORT_ADMIN=8082

# MySQL Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/rideease_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=YourMySQLPassword
DB_USERNAME=root
DB_PASSWORD=YourMySQLPassword

# Cloudinary Storage Configuration
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>
CLOUDINARY_CLOUD_NAME=<cloud_name>
CLOUDINARY_API_KEY=<api_key>
CLOUDINARY_API_SECRET=<api_secret>

# JWT Authentication
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

# Administrator Bootstrap
ADMIN_INITIAL_EMAIL=admin@rideease.com
ADMIN_INITIAL_PASSWORD=admin123
```

---

## 🚀 How to Run

### 1. Start MySQL Service
Ensure your local MySQL service (e.g., `MYSQL80` on port 3306) is running.

### 2. Run the Spring Boot Web Application
```powershell
cd backend
mvn clean spring-boot:run
```

Access the web portals:
- **Passenger Portal:** [http://localhost:8080/](http://localhost:8080/)
- **Driver Partner Hub:** [http://localhost:8081/driver/login](http://localhost:8081/driver/login)
- **Admin Fleet Console:** [http://localhost:8082/admin/login](http://localhost:8082/admin/login)
- **Database Health Check:** [http://localhost:8080/api/health/db](http://localhost:8080/api/health/db)

### 3. Run the JavaFX Desktop Command Center
Launch `RideEaseFXApp` directly from your IDE or execute:
```powershell
cd backend
mvn compile exec:java -Dexec.mainClass="com.rideease.javafx.RideEaseFXApp"
```

---

## 🧪 Running Automated Tests
```powershell
cd backend
mvn test
```
All unit and integration tests execute against the test slice with zero external dependencies.
