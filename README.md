# RideEase (Unified Edition) — Online Cab Booking Platform

A full-stack, enterprise-grade multi-portal online cab booking and dispatch application built with **Java 17/21, Spring Boot 3.2.5, Spring Data JPA / Hibernate, MySQL 8.0, JavaFX 21 Desktop Console, Cloudinary Media CDN, Thymeleaf, OpenStreetMap & OSRM Engine, and WebSocket STOMP**.

---

## 🏛️ System Architecture

RideEase operates with a dedicated **3-port isolation architecture** hosted on an embedded Tomcat multi-connector server, along with a standalone **JavaFX Desktop Administrative & Fleet Console** (`RideEaseFXApp` via `RideEaseFXLauncher`):

| Role / Portal | Port | Direct URL | Responsibilities |
|---|---|---|---|
| **Passenger / Rider** | **`8080`** | [http://localhost:8080/](http://localhost:8080/) | Fullscreen Leaflet map, draggable floating bottom sheet, OSRM road routing, device GPS location accuracy, nearby driver availability count & map pins, saved places, tiered vehicle selection (Mini, Sedan, SUV XL), live WebSocket tracking, 24/7 Safety Centre with emergency SOS, scheduled bookings, and downloadable tax receipts. |
| **Driver Partner** | **`8081`** | [http://localhost:8081/driver/login](http://localhost:8081/driver/login) | Map-first partner terminal, online/offline duty switch, real-time incoming request countdown card, atomic ride acceptance, start trip OTP validation, step-by-step road navigation, dual-leg route visualization (Leg 1: Captain to Pickup, Leg 2: Pickup to Destination), early completion reason validation modal, live GPS broadcast, and document verification upload portal. |
| **Fleet Administrator** | **`8082`** | [http://localhost:8082/admin/login](http://localhost:8082/admin/login) | Command center with live fleet telemetry map, Chart.js 7-day revenue & volume analytics, driver KYC & Cloudinary document verification panel with approve/reject workflow, and live searchable Trip Operations Ledger. |
| **JavaFX Desktop Console** | **Desktop Client** | Run `RideEaseFXApp` | Native desktop administrative & dispatch application featuring interactive OpenStreetMap slippy tile canvas with drag-to-pan & scroll-to-zoom, dynamic online Nominatim address search, one-click device geolocation, live STOMP subscriptions, real-time port health badges (8080, 8081, 8082), driver trip completion & settlement dialogs, database-bound Pie/Bar charts, and Cloudinary KYC inspection. |

### Subproject Structure:
- **`backend/`:** Complete Spring Boot 3.2.5 enterprise web application with server-rendered Thymeleaf dynamic templates, WebSocket STOMP telemetry, Spring Data JPA, and MySQL persistence.
  - **`backend/src/main/java/com/rideease/javafx/`:** Native JavaFX Desktop Console (`RideEaseFXApp.java`, `RideEaseFXLauncher.java`) with FXML views, models, controllers, and `OsmTileRenderer`.
- **`database/schema.sql`:** Complete MySQL 8.0 DDL relational schema for all tables (`users`, `drivers`, `vehicles`, `bookings`, `reviews`, `safety_contacts`).
- **`docs/`:** System design documents, architecture specifications, API flows, feature parity reports, and relational schemas.
- **`android/`:** Native Android client architecture documentation.

---

## ✨ Features & Capabilities

### 1. 🗄️ Relational Database (MySQL 8.0 & Spring Data JPA)
- **Enterprise Relational Schema:** Tables for `users`, `drivers`, `vehicles`, `bookings`, `reviews`, and `safety_contacts`.
- **Automated Schema Generation:** Powered by Hibernate DDL auto-updates (`spring.jpa.hibernate.ddl-auto=update`).
- **Spherical Haversine Distance Engine:** High-performance SQL queries for nearby driver discovery within custom kilometer radii.
- **Health Check Endpoint (`/api/health/db` & `/api/desktop/health`):** Live database connection and multi-port health diagnostics.

### 2. 📄 Cloudinary Vehicle & Driver Document Verification
- **Driver Profile & Vehicle KYC:** Dedicated portal (`/driver/verification`) where drivers upload personal photos, vehicle photos, Driving License, RC Book, Insurance Policy, and Commercial Permit.
- **Cloudinary Media CDN:** Uploads are stored securely in categorized Cloudinary folders with thumbnail transformation and document viewing links.
- **Admin Review Workflow:** Admins can preview documents from Cloudinary at `/admin/verifications` (and directly within the JavaFX Compliance Dossier) and approve or reject applications with custom feedback notes.

### 3. 🖥️ JavaFX Desktop Dispatch & Movable OpenStreetMap Console
- Native desktop interface for fleet operations with rich Java Event Handling:
  - **Movable & Zoomable OpenStreetMap Canvas (`OsmTileRenderer`):** Authentic OpenStreetMap slippy tiles rendered on JavaFX canvas with smooth mouse drag-to-pan and mouse wheel scroll-to-zoom.
  - **Real-Time Geocoding & City Search:** Type any city or landmark (e.g. *SRM AP*, *Mangalagiri AIIMS*, *Vijayawada*, *Hyderabad*, *Delhi*); automatically resolves coordinates and routes via OSRM.
  - **One-Click Current Location (`🎯 Use Current Location`):** Detects device location via network geolocation and calculates pickup routing immediately.
  - **Live Service Health Monitor:** Automatic 5-second background pinging of Port 8080, 8081, and 8082 endpoints with responsive visual status pills.
  - **Real Database KPI & Chart Integration:** Compliance distribution PieChart and Vehicle Tier BarChart directly backed by MySQL queries via `/api/desktop/kpis`.
  - **Dynamic Nearby Driver Radar:** Autonomous 4-second scanner querying `/api/drivers/nearby`.
  - **Conditional OTP Display & Real-time State Synchronization:** Security-hardened 4-digit OTP card displayed strictly after driver acceptance (`ACCEPTED` state).
  - **Driver Reached Destination & Fare Collection:** Dedicated `🏁 Complete Ride & Confirm Payment` button in Driver Terminal to settle fares directly to driver wallet and database.

### 4. 🗺️ Dual-Phase OSRM Route Navigation & Live Telemetry
- **Accurate Device Geolocation:** Both Rider and Driver interfaces request device GPS permissions with high accuracy (`enableHighAccuracy: true`).
- **Dual-Phase Navigation Flow:**
  - **Phase 1 (Navigation to Pickup):** Once a ride is accepted (`ACCEPTED`), the driver terminal map focuses on the navigation route to the passenger's pickup location.
  - **Arrival & OTP Verification (`ARRIVING`):** When within 75 meters of the pickup spot, the system notifies both parties and prompts for the 4-digit OTP.
  - **Phase 2 (Navigation to Destination):** Upon verifying the OTP (`IN_PROGRESS`), the route switches to passenger destination with remaining ETA.
- **Early Drop Modal Guard:** If a driver attempts to complete a trip before reaching the destination coordinate, an early completion modal requires providing a valid operational reason.
- **Automated Scheduled Ride Windowing:** Scheduled rides remain in `SCHEDULED` status until their scheduled dispatch window (within 2 minutes of pickup), preventing premature driver alerts.
- **Instant Cancellation Synchronization:** Cancellations triggered by rider or driver immediately broadcast across STOMP WebSockets (`/topic/drivers/requests` and `/topic/ride/{id}/status`), immediately dismissing cards across all active sessions.

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
Launch via Maven:
```powershell
cd backend
mvn javafx:run
```
*(Uses `com.rideease.javafx.RideEaseFXLauncher` with zero JVM bootlayer or classloader issues)*

---

## 🧪 Running Automated Tests
```powershell
cd backend
mvn test
```
All unit and integration tests execute against the test slice with zero external dependencies (13/13 passing).
