# RideEase — Implementation Plan & Phase Roadmap

This document outlines the engineering implementation phases, milestones, technical deliverables, and verification procedures.

---

## 1. Phased Development Roadmap

```
Phase 1: Foundation & Infrastructure (Completed)
├── Spring Boot 3 Scaffolding & Dependencies (Security, Web, Mongo, WS)
├── MongoDB Atlas Connection & 2dsphere Geospatial Indexing
├── Cloudinary Java SDK 2.x & Dotenv Integration
└── React & Android Project Layouts

Phase 2: Authentication, Roles & Driver KYC
├── User Registration & Login (JWT Auth & Role Claims)
├── Driver Document & Vehicle Photo Uploads (Cloudinary CDN)
└── Admin Verification Approval / Rejection Workflow

Phase 3: Core Ride Booking & Dynamic Pricing
├── Address Geocoding & Route Calculation
├── Dynamic Fare Estimation Engine (Mini, Sedan, SUV)
└── Ride Booking Request & Lifecycle State Machine

Phase 4: Real-Time Matching & Live GPS Tracking
├── 2dsphere Geospatial Query for Nearby Available Drivers
├── Spring STOMP WebSocket Channel Setup (/ws-ride)
├── Ride Request Push, Driver Accept/Reject Logic
└── Driver Real-time GPS Streaming to Passenger Map

Phase 5: Trip Execution, Payments & Ratings
├── 4-digit Ride Start OTP Verification
├── Trip Completion, Distance/Time Recalculation
├── Payment Settlement (Cash/Online/Wallet status)
└── Review & Rating Aggregation System

Phase 6: Frontend Dashboards & Android Client
├── React Web Client: Passenger Booking, Driver App, Admin Panel
├── Android Java App: Driver Live Tracker & Passenger App
└── End-to-End Testing & Concurrency Validation
```

---

## 2. Detailed Technical Deliverables by Phase

### Phase 2: Auth & KYC Deliverables
- `AuthController`: `/api/auth/register`, `/api/auth/login`, `/api/auth/me`
- `JwtAuthenticationFilter`: Parses `Bearer <token>`, populates Spring `SecurityContextHolder`.
- `DriverController`: `/api/drivers/onboarding` handles `MultipartFile` uploads to Cloudinary folder `rideease/kyc`.
- `AdminController`: `/api/admin/drivers/{id}/verify` with status updates (`APPROVED`, `REJECTED`).

### Phase 3: Booking & Pricing Deliverables
- `FareCalculationService`:
  ```java
  double rawFare = (baseRate + (distanceKm * perKmRate) + (durationMinutes * perMinute)) * surge;
  ```
- `RideController`: `/api/rides/estimate` returning tiered options.
- `BookingRepository`: Persistence with `BookingStatus.REQUESTED`.

### Phase 4: Matching & WebSocket Deliverables
- `DriverMatchingService`:
  ```java
  List<Driver> drivers = driverRepository.findByVerificationStatusAndOnlineTrueAndAvailableTrueAndCurrentLocationNear(
      VerificationStatus.APPROVED, new Point(lon, lat), new Distance(8.0, Metrics.KILOMETERS)
  );
  ```
- `WebSocketController`:
  - Client sends to `/app/driver/location`
  - Server broadcasts to `/topic/ride/{bookingId}` and `/topic/nearby`

### Phase 5: Verification & Completion Deliverables
- Secure random 4-digit OTP generated via `SecureRandom`.
- Server-side validation: Driver cannot invoke `/api/rides/{id}/start` without matching OTP.
- `ReviewRepository` & average rating update on driver profile.

---

## 3. Environment Setup & Execution Instructions

### 3.1. Backend Execution
```bash
cd backend
# Verify .env credentials (MongoDB Atlas, Cloudinary)
mvn clean spring-boot:run
```
The REST API will boot on `http://localhost:8080`.

### 3.2. Frontend Web Execution
```bash
cd frontend
npm install
npm run dev
```
The React development server runs on `http://localhost:5173`.

### 3.3. Database Indexing
```bash
mongosh "$SPRING_DATA_MONGODB_URI" database/init-mongo.js
```

---

## 4. Verification & Testing Checklist

- [x] MongoDB Atlas connection verified via credentials in `.env` and `application.properties`.
- [x] Cloudinary SDK 2.x and `dotenv-java` verified via `CloudinaryTestRunner.java`.
- [ ] Authentication integration test: Register rider, driver, and admin accounts.
- [ ] Geospatial query test: Seed mock driver coordinates in Bangalore/Delhi and confirm `$near` returns results.
- [ ] WebSocket test: Verify STOMP handshake over `/ws-ride` and message receipt on `/topic/ride/{id}`.
- [ ] Concurrency test: Ensure two drivers accepting the same ride concurrently results in one winner and one `409 Conflict`.
