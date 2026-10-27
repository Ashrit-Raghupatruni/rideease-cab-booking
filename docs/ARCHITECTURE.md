# RideEase — System Architecture (Multi-Port Isolation Model)

## 1. High-Level Multi-Port Architecture Diagram

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
│ • Pickup / Dropoff Selection │ │ • Live Request Auto-Radar    │ │ • Fleet Telemetry Counters  │
│ • Waiting for Captain Screen │ │ • Accept / Decline Actions   │ │ • Global Bookings Monitor   │
│ • Animated Highway Car       │ │ • OTP Ride Start Validator   │ │ • Registered Users Ledger   │
│ • My Rides & Payment History │ │ • Complete Trip Settlement   │ │ • Dynamic Fare Matrix Tuning│
│ • Account Settings & Delete  │ │ • Earnings Payout History    │ │ • Emergency SOS Overview    │
└───────────────┬──────────────┘ └─────────────┬────────────────┘ └───────────────────┬─────────┘
                │                              │                                      │
                └──────────────────────────────┼──────────────────────────────────────┘
                                               │ HTTP 1.1 / Session / STOMP
                                               ▼
                     ┌───────────────────────────────────────────────────┐
                     │       Spring Boot 3.x Multi-Connector Engine      │
                     ├───────────────────────────────────────────────────┤
                     │  • MultiPortTomcatConfig (Connectors: 8081, 8082) │
                     │  • PortAccessFilter (Role boundary enforcement)   │
                     │  • Security Filter Chain & BCrypt Password Hash   │
                     │  • Dynamic Fare Engine (Mini, Sedan, SUV)         │
                     │  • Ride Lifecycle State Machine Manager           │
                     │  • Cloudinary Document & Image Ingestion Service  │
                     │  • DatabaseHealthCheckController (/api/health/db) │
                     └─────────────────────────┬─────────────────────────┘
                                               │
                                               ▼
                     ┌───────────────────────────────────────────────────┐
                     │            MongoDB Atlas Cloud Cluster            │
                     ├───────────────────────────────────────────────────┤
                     │  • Database: rideease_db (Cluster0)               │
                     │  • Collections: users, drivers, vehicles,         │
                     │    bookings, payments, reviews                    │
                     │  • Geospatial Index: 2dsphere on coordinates      │
                     │  • Automatic Secondary/Primary TLS Replica Sync   │
                     └───────────────────────────────────────────────────┘
```

---

## 2. Multi-Port Configuration & Isolation Strategy

Rather than serving every dashboard on a single generic port, the application uses **Apache Tomcat Additional Connectors** customized in Java:

1. **Port 8080 (Primary Server Port):**
   - Serves the **Passenger Web Portal**.
   - Handles rider authentication, cab estimation, live road tracking, rider booking history, and account settings.
   - Any attempt to access `/driver/*` or `/admin/*` on this port is automatically intercepted by `PortAccessFilter` and redirected to their respective ports.

2. **Port 8081 (Secondary Connector):**
   - Serves the **Driver Partner Hub**.
   - Handles driver authentication, online request alerts, trip acceptance, OTP verification, and earnings history.

3. **Port 8082 (Tertiary Connector):**
   - Serves the **Fleet Administrator Console**.
   - Handles administrative login, real-time booking intervention, system metrics, and driver KYC approvals.

---

## 3. Communication Protocols

| Channel | Protocol | Format | Description |
|---|---|---|---|
| Browser <-> Port 8080 | HTTP/HTTPS | Server-rendered HTML (Thymeleaf) | Passenger operations |
| Browser <-> Port 8081 | HTTP/HTTPS | Server-rendered HTML (Thymeleaf) | Driver partner HUD |
| Browser <-> Port 8082 | HTTP/HTTPS | Server-rendered HTML (Thymeleaf) | Fleet administrative dashboard |
| Backend <-> Atlas Cluster0 | Mongo Wire Protocol (TLS) | BSON | Document persistence, queries, health checks |
| Backend <-> Cloudinary | HTTPS REST | Multipart Form-Data / JSON | KYC license and vehicle photo storage |

---

## 4. Resilience & Diagnostics
- **Live Health Diagnostics:** A dedicated diagnostic controller at `/api/health/db` allows zero-downtime latency inspection of both write (`insertOne`) and read (`find`) calls directly against Atlas.
- **Strict Role Boundaries:** Managed through `PortAccessFilter`, ensuring port and session separation across all 3 actor workflows.
