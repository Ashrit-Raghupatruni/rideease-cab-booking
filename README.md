# RideEase — Online Cab Booking Platform (Toury Edition)

A full-stack, enterprise-grade cab-hailing and fleet management platform powered by **100% Java (Spring Boot + Thymeleaf)**, **MongoDB Atlas**, **Cloudinary**, and **Mappls (MapmyIndia)**.

---

## 🏛️ System Architecture & 3-Port Isolation

RideEase runs a multi-connector Tomcat configuration isolating each actor role on its own dedicated port:

```
                            ┌────────────────────────────┐
                            │     Cloudinary Media CDN   │
                            │ (KYC Docs & Vehicle Photo) │
                            └─────────────▲──────────────┘
                                          │
    ┌─────────────────────────────────────┼─────────────────────────────────────┐
    │                                     │                                     │
    ▼ (Port 8080)                         ▼ (Port 8081)                         ▼ (Port 8082)
┌───────────────────────┐             ┌───────────────────────┐             ┌───────────────────────┐
│   Passenger Portal    │             │   Driver Partner Hub  │             │   Fleet Admin Console │
│ • Email/Password Auth │             │ • Driver Registration │             │ • Fleet Operations    │
│ • Cab Booking & Fare  │             │ • Live Request Radar  │             │ • Live Trips Monitor  │
│ • Mappls Route Matrix │             │ • Accept / Start OTP  │             │ • User/Driver Ledger  │
│ • Live Road Animation │             │ • Earnings & Payouts  │             │ • Rate Matrix Controls│
│ • My Rides & Payments │             │ • Settings & Delete   │             │ • Emergency Monitor   │
│ • Settings & Delete   │             │                       │             │                       │
└───────────┬───────────┘             └───────────┬───────────┘             └───────────┬───────────┘
            │                                     │                                     │
            └─────────────────────────┬───────────┴─────────────────────────────────────┘
                                      │
                                      ▼
             ┌──────────────────────────────────────────────────┐
             │       Spring Boot 3.x Multi-Port Core API        │
             │   • PortAccessFilter (Role boundary enforcement) │
             │   • MapplsRoutingService (MapmyIndia Matrix)     │
             │   • Dynamic Fare Engine (Mini / Sedan / SUV)     │
             │   • Trip State Machine (SEARCHING -> COMPLETED)  │
             │   • BCrypt Password Security                     │
             └────────────────────────┬─────────────────────────┘
                                      │
                                      ▼
             ┌──────────────────────────────────────────────────┐
             │            MongoDB Atlas Cloud Cluster            │
             │         Database: rideease_db (Cluster0)         │
             │   • users, drivers, vehicles, bookings, reviews  │
             │   • 2dsphere Geospatial Indexing for Proximity   │
             └──────────────────────────────────────────────────┘
```

---

## 🌐 Dedicated Application Ports & URLs

| Role | Port | Direct URL | Default Credentials / Action |
|---|---|---|---|
| **Passenger / Rider** | **`8080`** | [http://localhost:8080/](http://localhost:8080/) | Register a new rider or sign in to book cabs |
| **Driver Partner** | **`8081`** | [http://localhost:8081/driver/login](http://localhost:8081/driver/login) | Register a driver partner or sign in to accept rides |
| **Fleet Administrator** | **`8082`** | [http://localhost:8082/admin/login](http://localhost:8082/admin/login) | **Email:** `admin@rideease.com` <br> **Password:** `admin123` |
| **Database Diagnostics** | **`8080`** | [http://localhost:8080/api/health/db](http://localhost:8080/api/health/db) | Live Read & Write latency diagnostics on Atlas |
| **Mappls Route API** | **`8080`** | [http://localhost:8080/api/maps/route](http://localhost:8080/api/maps/route) | Mappls (MapmyIndia) Distance & ETA Calculator |

---

## 🗺️ Mapping & Routing Integration: Mappls (MapmyIndia)

The platform is integrated with **Mappls (MapmyIndia)** for accurate Indian route distance matrices, traffic durations, and geospatial calculations:
- Configured in [`backend/src/main/resources/application.properties`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/resources/application.properties) via `mappls.api-key`, `mappls.client-id`, and `mappls.client-secret`.
- Handled by [`MapplsRoutingService.java`](file:///C:/Users/ashri/Java/PROJECT-CAB%20BOOKING/backend/src/main/java/com/rideease/service/MapplsRoutingService.java) and exposed via [`/api/maps/route`](http://localhost:8080/api/maps/route).

---

## 🛠️ Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Language & Framework** | Java 17/21, Spring Boot 3.2.5 | Embedded Tomcat with multi-connector configuration |
| **UI & Templating** | Thymeleaf, HTML5, Bootstrap 5.3, CSS3 | 100% Java-rendered frontend without Node.js dependencies |
| **Database** | MongoDB Atlas (Cluster0) | Cloud-hosted document database with TLS encryption |
| **Media Storage** | Cloudinary Java SDK 2.0 (HTTP5) | Driver KYC documents and vehicle photo uploads |
| **Mapping & Routing** | Mappls (MapmyIndia) Platform | Routing, distance matrix, and ETA |
| **Security** | Spring Security 6, BCrypt | Secure password hashing, session and role isolation |

---

## 🚦 How to Run the Application

```powershell
cd "C:\Users\ashri\Java\PROJECT-CAB BOOKING\backend"
mvn clean spring-boot:run
```

Once started:
1. Open **`http://localhost:8080/`** (User Portal)
2. Open **`http://localhost:8081/driver/login`** (Driver Portal)
3. Open **`http://localhost:8082/admin/login`** (Admin Portal: `admin@rideease.com` / `admin123`)
4. Inspect database latency at **`http://localhost:8080/api/health/db`**
5. Inspect Mappls routing at **`http://localhost:8080/api/maps/route`**
