# RideEase ("Toury" Edition) — Online Cab Booking Platform

A full-stack, enterprise-grade multi-portal online cab booking application built with **Java 17/21, Spring Boot 3.2.5, Thymeleaf, WebSocket STOMP, and MongoDB Atlas**.

---

## 🏛️ System Architecture

RideEase operates with a dedicated **3-port isolation architecture** hosted on an embedded Tomcat multi-connector server:

| Role / Portal | Port | Direct URL | Responsibilities |
|---|---|---|---|
| **Passenger / Rider** | **`8080`** | [http://localhost:8080/](http://localhost:8080/) | Fullscreen Leaflet map, draggable floating bottom sheet, OSRM road routing, instant GPS location accuracy, saved places (Home/Work/Recent), tiered vehicle selection (Mini, Sedan, SUV XL) with vector illustrations, live WebSocket tracking, 24/7 Safety Centre with emergency SOS, scheduled bookings, and downloadable tax receipts. |
| **Driver Partner** | **`8081`** | [http://localhost:8081/driver/login](http://localhost:8081/driver/login) | Map-first partner terminal, online/offline duty switch, real-time incoming request countdown card, atomic ride acceptance, start trip OTP validation, step-by-step road navigation, live GPS broadcast, and cash settlement confirmation. |
| **Fleet Administrator** | **`8082`** | [http://localhost:8082/admin/login](http://localhost:8082/admin/login) | Command center with live fleet telemetry map, Chart.js 7-day revenue & volume analytics, driver KYC & document verification table, and live searchable Trip Operations Ledger. |

### Subproject Structure:
- **`backend/`:** Complete Spring Boot 3.2.5 enterprise web application with server-rendered Thymeleaf dynamic templates, WebSocket STOMP telemetry, and MongoDB Atlas persistence.
- **`android/`:** Architecture specifications and build configuration for future native mobile client applications.
- **`database/init-mongo.js`:** MongoDB collection schemas, indexes, and geospatial `2dsphere` definitions.

---

## ✨ Features & Capabilities

### 1. 🗺️ Authoritative Road Routing & Live Telemetry
- **Zero Route Fabrication:** Uses genuine OSRM (`router.project-osrm.org`) driving polylines. Trips are cleanly rejected if no drivable road route exists — no synthetic straight-line lines or approximate multipliers.
- **Progressive Route Drawing:** Dynamic animated route stroke with dashed flow indicators.
- **Smooth Vehicle Marker Interpolation:** Leaflet markers glide smoothly between GPS telemetry ticks rather than jumping abruptly.

### 2. ⚡ Real-Time Event-Driven Dispatch (No Polling)
- **Eliminated Page Refresh Polling:** Replaced legacy `<meta http-equiv="refresh">` tags with event-driven WebSocket STOMP subscriptions.
- **Targeted Topic Channels:**
  - `/topic/ride/{id}/status`: Instant status transitions (`SEARCHING`, `ACCEPTED`, `ARRIVING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`).
  - `/topic/ride/{id}/telemetry`: Live driver coordinates with server-side validation, arrival proximity detection (< 75m), and reconnection snapshots (`/app/ride/{id}/snapshot`).
  - `/topic/drivers/requests`: Incoming ride broadcasts delivered instantly to driver terminals.
  - `/topic/admin/fleet/events` & `telemetry`: Real-time fleet KPI updates.

### 3. 🛡️ 24/7 Safety Centre & Security
- **One-Click SOS Emergency:** Instant panic trigger that broadcasts alerts with live coordinates to emergency contacts.
- **Trusted Emergency Contacts:** Add and manage emergency contacts (`Parent`, `Spouse`, `Sibling`, `Friend`) saved in MongoDB.
- **Driver Verification Shield:** Roster tracking of background checks, police clearance, vehicle registration, and driver license validity.
- **Trip OTP Verification:** 4-digit ride verification OTP required before the driver can start transit, limited to 5 failed attempts to prevent brute-forcing.

### 4. 💳 Multi-Channel Payments & Financial Correctness
- **Decoupled Lifecycle:** Trip completion (`IN_PROGRESS -> COMPLETED`) does not automatically mark a ride as `PAID`.
- **UPI, Wallet & Cash:**
  - **Instant UPI:** Integrated support for Google Pay, PhonePe, and Paytm UPI transfers.
  - **In-App Wallet:** Automated atomic balance verification and debiting (`WLT-...` transaction reference).
  - **Cash:** Trips remain `PENDING` until the driver explicitly confirms cash collection (`CSH-...` transaction reference).
- **Downloadable Tax Receipts (`/user/receipt/{bookingId}`):** Instant download of formatted tax invoices featuring itemized base fare, GST breakdown (SGST 9% + CGST 9%), and transaction metadata.

### 5. 🎯 Scheduled Rides & Personalization
- **Scheduled Rides:** Choose a future date and time for pickup directly during booking.
- **Saved Places:** Quick-select chips for Home, Work, and Recent destinations.
- **One-Tap Rebooking:** Quickly re-order frequent trips directly from the ride history ledger.
- **Ratings & Reviews:** 1–5 star ratings and feedback stored in MongoDB.

---

## ⚙️ Environment Configuration

Copy `backend/.env.example` to `backend/.env`:

```env
# Server Ports
PORT=8080
DRIVER_PORT=8081
ADMIN_PORT=8082

# MongoDB Connection String (Atlas or Local)
# Supports both SPRING_DATA_MONGODB_URI and MONGODB_URI
SPRING_DATA_MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.example.mongodb.net/rideease_db?retryWrites=true&w=majority&appName=Cluster0&tls=true

# JWT Secret (Minimum 32 characters)
JWT_SECRET=your_32_characters_secure_jwt_secret_key_here!
JWT_EXPIRATION_MS=86400000

# Cloudinary Storage Configuration (Optional for Driver KYC)
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>

# Initial Administrator Bootstrap (Optional)
ADMIN_INITIAL_EMAIL=admin@rideease.com
ADMIN_INITIAL_PASSWORD=ChooseASecureAdminPasswordHere!
```

---

## 🚀 How to Run

### Option 1: Run Locally with Maven
```powershell
cd backend
mvn clean spring-boot:run
```
Access the portals:
- **Passenger Portal:** http://localhost:8080/
- **Driver Portal:** http://localhost:8081/driver/login
- **Admin Portal:** http://localhost:8082/admin/login

### Option 2: Run with Docker Compose (Includes MongoDB 7.0 & Multi-Port Routing)
```powershell
docker-compose up -d --build
```
To view logs:
```powershell
docker-compose logs -f backend
```
To tear down:
```powershell
docker-compose down -v
```

---

## 🧪 Automated Testing

Execute the comprehensive automated test suite:
```powershell
cd backend
mvn test
```

### Verified Test Suite:
1. `BookingLifecycleTest` — Atomic state transitions and multi-driver concurrency prevention.
2. `FareCalculationServiceTest` — Distance and duration matrix calculations for Mini, Sedan, and SUV.
3. `JwtTokenProviderTest` — Token signing, parsing, and expiration validation.
4. `RideBookingValidationTest` — Jakarta Bean Validation on coordinates, addresses, and enums.
5. `RoadRoutingServiceTest` — OSRM API response decoding and road geometry validation.
6. `WalletPaymentSettlementTest` — Atomic wallet debits and transaction isolation.
