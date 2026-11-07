# RideEase — System Architecture (MySQL, Cloudinary & JavaFX)

## 1. High-Level Multi-Portal Architecture Diagram

```
                                ┌────────────────────────────────┐
                                │     Cloudinary Media CDN       │
                                │  (Driver KYC & Vehicle Photos) │
                                └───────────────▲────────────────┘
                                                │
         ┌──────────────────────────────────────┼──────────────────────────────────────┐
         │                                      │                                      │
 ┌───────┴──────────────────────┐ ┌─────────────┴────────────────┐ ┌───────────────────┴───────┐
 │     Passenger Portal         │ │     Driver Partner Hub       │ │    Fleet Admin Console      │
 │      Port: 8080              │ │      Port: 8081              │ │      Port: 8082             │
 │ • Rider Signup & Signin      │ │ • Driver Login & Signup      │ │ • Restricted Admin Login    │
 │ • Device GPS Geolocation     │ │ • High-Accuracy Geolocation  │ │ • Live Fleet Telemetry Map  │
 │ • Nearby Driver Radar        │ │ • Incoming Request Radar     │ │ • KYC Document Approval Pane│
 │ • Dual-Leg Trip Trajectory   │ │ • Dual-Leg Navigation View   │ │ • Global Bookings Monitor   │
 │ • Real-Time WebSocket Telemetry│ • 2s Cadence (driver@gmail)  │ │ • Registered Users Ledger   │
 └───────────────┬──────────────┘ └─────────────┬────────────────┘ └───────────────────┬─────────┘
                 │                              │                                      │
                 └──────────────────────────────┼──────────────────────────────────────┘
                                                │ HTTP 1.1 / Session / STOMP
                                                ▼
                      ┌───────────────────────────────────────────────────┐
                      │       Spring Boot 3.x Multi-Connector Engine      │
                      ├───────────────────────────────────────────────────┤
                      │  • MultiPortTomcatConfig (Ports: 8080, 8081, 8082)│
                      │  • PortAccessFilter (Role boundary enforcement)   │
                      │  • Spring Data JPA & Hibernate Relational ORM     │
                      │  • Dynamic Fare Engine (Mini, Sedan, SUV)         │
                      │  • Cloudinary Document & Image Ingestion Service  │
                      │  • DatabaseHealthCheckController (/api/health/db) │
                      │  • JavaFX Application Bridge (RideEaseFXApp)      │
                      └─────────────────────────┬─────────────────────────┘
                                                │ JDBC / JPA
                                                ▼
                      ┌───────────────────────────────────────────────────┐
                      │              MySQL 8.0 Relational DB              │
                      ├───────────────────────────────────────────────────┤
                      │  • Database: rideease_db                          │
                      │  • Tables: users, drivers, vehicles, bookings,    │
                      │    reviews, safety_contacts, connectivity_test    │
                      │  • Spatial indexing & Haversine distance queries  │
                      │  • InnoDB ACID transactional integrity            │
                      └───────────────────────────────────────────────────┘
```

---

## 2. Multi-Port Configuration & Isolation Strategy

1. **Port 8080 (Primary Server Port):**
   - Serves the **Passenger Web Portal**.
   - Handles rider authentication, cab estimation, high-accuracy device GPS tracking, nearby driver discovery, and live ride telemetry.
2. **Port 8081 (Driver Partner Port):**
   - Serves the **Driver Partner Hub**.
   - Handles driver authentication, duty toggle (Online/Offline), incoming ride countdown, document verification upload to Cloudinary, dual-leg navigation, and real-time telemetry streaming (including the 2-second trajectory updates for `driver@gmail.com`).
3. **Port 8082 (Fleet Admin Port):**
   - Serves the **Fleet Operations Command**.
   - Provides live fleet KPIs, vehicle and driver compliance approvals, and operations tracking.
4. **Desktop JavaFX Console:**
   - Standalone administrative interface featuring rich Java Event Handling (`RideEaseFXApp.java`).
