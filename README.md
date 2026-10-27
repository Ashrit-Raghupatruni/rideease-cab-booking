# RideEase ("Toury" Edition) — Online Cab Booking Platform

A full-stack multi-portal online cab booking application built with **Java 17/21, Spring Boot 3.2.5, Thymeleaf, and MongoDB Atlas**.

---

## 🏛️ System Architecture & Subprojects

The repository contains three project subdirectories reflecting the planned and implemented layers:

### 1. `backend/` (Fully Implemented & Active)
- **Framework:** Spring Boot 3.2.5 (Pure Java)
- **Frontend Architecture:** Java-rendered dynamic server-side views using **Thymeleaf, Bootstrap 5.3, Bootstrap Icons, and Leaflet.js**.
- **Port Isolation:**
  - **Port `8080` (Passenger Portal):** Interactive map with OSRM road routing, location autocomplete, upfront fare tiers, trip tracking, payment history, and profile settings.
  - **Port `8081` (Driver Partner Portal):** Real-time ride requests dispatch, OTP pickup verification, route navigation, and trip completion ledger.
  - **Port `8082` (Fleet Admin Portal):** Fleet metrics, global booking ledger, and driver KYC approvals.
- **Data Persistence:** MongoDB Atlas with Geospatial `2dsphere` indexes.
- **Media Storage:** Cloudinary Java SDK 2.x for driver verification and vehicle document management.
- **Authoritative Routing:** Integrated OSRM road-routing engine (`RoadRoutingService.java`) with zero coordinate synthesis or straight-line fallbacks.

### 2. `frontend/` (Scaffold / Planned Future Web Client)
- Contains `package.json` specifications for an optional standalone React/Vite web application.
- *Status:* Unbuilt scaffold; the active production application runs directly on the Spring Boot + Thymeleaf server (`backend/`).

### 3. `android/` (Planned Mobile Client Skeleton)
- Contains Gradle Android project scaffolding for native Android driver and rider apps.

---

## 🔒 Security & Environment Setup

All sensitive secrets and environment configuration must be provided via environment variables. **No hardcoded credentials exist in source code.**

### 1. Configure Environment Variables
Copy `backend/.env.example` to `backend/.env` (or set system environment variables):

```env
# Server Ports
PORT=8080
DRIVER_PORT=8081
ADMIN_PORT=8082

# MongoDB Atlas URI (Mandatory - Application will fail to start if missing)
SPRING_DATA_MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.example.mongodb.net/rideease_db?retryWrites=true&w=majority&appName=Cluster0&tls=true

# JWT Secret (Mandatory - Minimum 32 characters)
JWT_SECRET=your_minimum_32_characters_secure_jwt_secret_here

# Cloudinary Storage Configuration (Optional for KYC uploads)
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>

# Initial Administrator Bootstrap (Optional)
ADMIN_INITIAL_EMAIL=admin@rideease.com
ADMIN_INITIAL_PASSWORD=ChooseASecureAdminPasswordHere
```

---

## 🚦 How to Build, Test, and Run

### Run Automated Tests
```powershell
cd backend
mvn test
```

### Start the Application
```powershell
cd backend
mvn spring-boot:run
```

Once started:
1. **Passenger Portal:** [http://localhost:8080/](http://localhost:8080/)
2. **Driver Portal:** [http://localhost:8081/driver/login](http://localhost:8081/driver/login)
3. **Admin Portal:** [http://localhost:8082/admin/login](http://localhost:8082/admin/login)
4. **Database Health Diagnostic:** [http://localhost:8080/api/health/db](http://localhost:8080/api/health/db)
