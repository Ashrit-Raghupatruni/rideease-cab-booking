# RideEase — Technical Specification & API Contract

## 1. Technical Stack Specifications

| Layer | Component | Version | Notes |
|---|---|---|---|
| Runtime | Java OpenJDK | 17 LTS / 21 LTS | High-performance multi-port server |
| Framework | Spring Boot | 3.2.5 | Multi-connector Tomcat (Ports 8080, 8081, 8082) |
| Database | MongoDB Atlas | 7.x Cluster | GeoJSON 2dsphere proximity & document storage |
| CDN / Media | Cloudinary Java SDK | 2.0.0 (HTTP5) | KYC document storage, transforms, auto-format |
| Maps & Routing | Mappls (MapmyIndia) | REST API | Distance matrix, routes, Indian road network calibration |
| WebSockets | Spring WebSocket + STOMP | 3.2.5 | Real-time bidirectional message broker |
| Auth & Tokens | Spring Security + BCrypt | 6.x | Stateless session and role-based protection |
| Web Frontend | Java Thymeleaf + Bootstrap 5 | 5.3 | 100% Java-rendered without Node.js dependencies |

---

## 2. Dedicated Port Contracts

- **Port 8080:** Passenger Web Application (`/user/**`)
- **Port 8081:** Driver Partner Hub (`/driver/**`)
- **Port 8082:** Fleet Administrator Console (`/admin/**`)

---

## 3. Core REST & Routing Endpoints Specification

### 3.1. Mappls (MapmyIndia) Routing Controller (`/api/maps`)

#### `GET /api/maps/route`
Calculates distance and duration using Mappls (MapmyIndia) engine.
- **Query Parameters:**
  - `pickupLon`: `77.5801`
  - `pickupLat`: `12.9720`
  - `dropoffLon`: `77.6050`
  - `dropoffLat`: `12.9810`
- **Response (200 OK):**
  ```json
  {
    "distanceKm": 8.5,
    "durationMinutes": 22,
    "provider": "Mappls (MapmyIndia)",
    "apiKeyActive": true
  }
  ```

### 3.2. Diagnostics & Health Controller (`/api/health`)

#### `GET /api/health/db`
Performs live read and write tests directly against MongoDB Atlas Cluster0.
- **Response (200 OK):**
  ```json
  {
    "status": "UP",
    "database": "MongoDB Atlas Cluster0",
    "databaseName": "rideease_db",
    "writeTest": "SUCCESS (112 ms)",
    "readTest": "SUCCESS (84 ms)",
    "message": "Database is 100% OPERATIONAL for both READ and WRITE operations!"
  }
  ```

---

## 4. Environment Variables (`.env`)

```env
# Mappls (MapmyIndia) Credentials
MAPPLS_API_KEY=
MAPPLS_CLIENT_ID=
MAPPLS_CLIENT_SECRET=

# Cloudinary
CLOUDINARY_URL=cloudinary://<api_key>:<api_secret>@<cloud_name>

# MongoDB Atlas
SPRING_DATA_MONGODB_URI=mongodb+srv://<username>:<password>@cluster0.example.mongodb.net/rideease_db?retryWrites=true&w=majority&appName=Cluster0
```
