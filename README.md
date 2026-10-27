# RideEase ("Toury" Edition) — Online Cab Booking Platform

A full-stack multi-portal online cab booking application built with **Java 17/21, Spring Boot 3.2.5, Thymeleaf, and MongoDB**.

---

## 🏛️ System Architecture

RideEase operates with a dedicated **3-port isolation architecture** hosted on an embedded Tomcat multi-connector server:

| Role / Portal | Port | Direct URL | Responsibilities |
|---|---|---|---|
| **Passenger / Rider** | **`8080`** | [http://localhost:8080/](http://localhost:8080/) | Interactive Leaflet map with OSRM road routing, autocomplete, tiered upfront pricing (Mini, Sedan, SUV), live ride tracking, OTP display, and wallet management. |
| **Driver Partner** | **`8081`** | [http://localhost:8081/driver/login](http://localhost:8081/driver/login) | Real-time trip broadcast feed, atomic acceptance, passenger pickup OTP verification, road navigation, trip completion, and cash collection confirmation. |
| **Fleet Administrator** | **`8082`** | [http://localhost:8082/admin/login](http://localhost:8082/admin/login) | Global dispatch ledger, trip lifecycle auditing, fleet KPIs, and driver KYC verification. |

### Subproject Structure:
- **`backend/` (Active & Production-Ready):** Pure Java Spring Boot 3.2.5 web application with server-rendered Thymeleaf dynamic templates, WebSocket STOMP telemetry, and MongoDB Atlas persistence.
- **`android/` (Planned Mobile Client):** Contains Gradle build scaffolding and architecture specifications for planned native Android apps.
- **`database/init-mongo.js`:** MongoDB collection schemas and geospatial `2dsphere` index definitions.

---

## 🔒 Security & Privacy Hardening

- **Zero Hardcoded Secrets:** All credentials, keys, and connection strings are read strictly from environment variables or a local `.env` file (which is git-ignored).
- **Fail-Fast Validation:** [`SecurityEnvironmentValidator.java`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/java/com/rideease/config/SecurityEnvironmentValidator.java) ensures required security properties are verified before boot.
- **Secure Admin Provisioning:** No default administrator passwords exist. Administrator bootstrapping only runs if `ADMIN_INITIAL_EMAIL` and `ADMIN_INITIAL_PASSWORD` are explicitly defined.
- **Jakarta Bean Validation:** [`BookRideRequest.java`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/java/com/rideease/dto/BookRideRequest.java) enforces coordinate range limits (`[-90, 90]`, `[-180, 180]`), address constraints, and validated vehicle/payment enums.
- **Structured Error Handling:** [`GlobalExceptionHandler.java`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/java/com/rideease/controller/GlobalExceptionHandler.java) returns sanitized error responses without leaking stack traces or internal exception details.

---

## 🗺️ Authoritative Road Routing & Verification

- **Zero Route Fabrication:** Powered by [`RoadRoutingService.java`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/java/com/rideease/service/RoadRoutingService.java), which calculates real driving polylines via OSRM (`router.project-osrm.org` / `routing.openstreetmap.de`).
- **No Straight Lines or Synthetic Multipliers:** Booking creation completely rejects trips where genuine road geometry cannot be found.
- **Real-Time Driver Telemetry:** Drivers broadcast GPS telemetry over `/app/driver/telemetry`, and passengers receive live car marker updates on Leaflet via `/topic/ride/{id}/telemetry`.

---

## 💳 Payment Correctness & Wallet Management

- **Decoupled Lifecycle:** Trip completion (`IN_PROGRESS -> COMPLETED`) does not automatically mark a ride as `PAID`.
- **In-App Wallet:** System checks passenger wallet balance. If balance is sufficient, it atomically debits the fare, logs a unique transaction ID (`WLT-...`), records `paidAt`, and marks the ride `PAID`. If balance is insufficient, status remains `PENDING`.
- **Cash Payments:** Cash trips complete with `paymentStatus = PENDING`. The driver partner must explicitly confirm physical cash collection via `/driver/payment/collect`.

---

## ⚙️ Environment Configuration

Copy `backend/.env.example` to `backend/.env`:

```env
# Server Ports
PORT=8080
DRIVER_PORT=8081
ADMIN_PORT=8082

# MongoDB Connection String (Atlas or Local)
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

### Option 2: Run with Docker Compose (Includes Local MongoDB 7)
```powershell
docker-compose up -d --build
```

---

## 🧪 Automated Testing

Execute the comprehensive automated test suite:
```powershell
cd backend
mvn test
```

### Test Coverage (13/13 Passing):
- `RideBookingValidationTest`: Validates latitude/longitude bounds and required address fields.
- `RoadRoutingServiceTest`: Verifies out-of-bounds rejection and road network snapping.
- `WalletPaymentSettlementTest`: Verifies atomic wallet balance debiting and insufficient balance handling.
- `FareCalculationServiceTest`: Verifies pricing for Mini, Sedan, SUV, and surge pricing.
- `JwtTokenProviderTest`: Verifies token signing, claim extraction, and expiration.
- `BookingLifecycleTest`: Verifies trip state machine and OTP attempt limits.
