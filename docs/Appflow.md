# RideEase — Application Flow & User Journeys

This document details the multi-port application flows and lifecycle transitions across all platform actors.

---

## 1. End-to-End Ride Booking, Acceptance & Trip Completion Flow

```mermaid
sequenceDiagram
    autonumber
    actor Passenger as Passenger (Port 8080)
    participant UserPort as Web Server :8080
    participant Backend as Spring Boot Core
    participant DB as MongoDB Atlas
    participant DriverPort as Driver Server :8081
    actor Driver as Driver Captain (Port 8081)
    participant AdminPort as Admin Server :8082
    actor Admin as Fleet Admin (Port 8082)

    Note over Passenger,UserPort: Phase 1: Rider Books a Cab
    Passenger->>UserPort: Enter Pickup & Dropoff, Select Sedan Tier
    UserPort->>Backend: POST /user/book
    Backend->>DB: Save Booking (Status: SEARCHING, Generates 4-digit OTP)
    Backend-->>UserPort: Redirect to /user/ride/{id}/track
    UserPort-->>Passenger: Show "Waiting for Captain to accept the ride..." (Auto-polling)

    Note over Driver,DriverPort: Phase 2: Live Dispatch & Driver Acceptance
    DriverPort->>DB: Auto-refreshes /driver/dashboard (Reads SEARCHING bookings)
    DriverPort-->>Driver: Incoming Ride Alert Pops Up! (Pickup: North Campus, Fare: ₹213)
    Driver->>DriverPort: Click "Accept This Ride Request"
    DriverPort->>Backend: POST /driver/ride/accept
    Backend->>DB: Update Booking (Status: ACCEPTED, Assign Driver ID)
    Backend-->>DriverPort: Show Trip under "My Accepted Trips"

    Note over Passenger,UserPort: Phase 3: Transition to Live Moving Car Screen
    UserPort->>DB: Polling /user/ride/{id}/track detects status = ACCEPTED
    UserPort-->>Passenger: Update View: "Captain Accepted Your Ride!" + Shows Start OTP (e.g. 8492)
    UserPort-->>Passenger: Render CSS Animated Moving Car on Highway

    Note over Driver,DriverPort: Phase 4: Trip Verification & Start
    Passenger->>Driver: Shares 4-digit OTP in person
    Driver->>DriverPort: Enter OTP (8492) & Click "Verify & Start"
    DriverPort->>Backend: POST /driver/ride/start
    Backend->>DB: Validate OTP & Update Booking (Status: IN_PROGRESS)
    Backend-->>DriverPort: Transition to In-Progress Navigation View

    Note over Driver,DriverPort: Phase 5: Arrival & Settlement
    Driver->>DriverPort: Click "Complete Trip"
    DriverPort->>Backend: POST /driver/ride/complete
    Backend->>DB: Update Booking (Status: COMPLETED, Payment: PAID)
    Backend-->>DriverPort: Settle Earnings (+₹213 in Driver Payments Ledger)
    Backend-->>UserPort: Show "Ride Completed Successfully!"

    Note over Admin,AdminPort: Continuous Audit
    AdminPort->>DB: GET /admin/dashboard
    AdminPort-->>Admin: Displays Active Rides, Revenue ₹213, User & Driver Records
```

---

## 2. User & Driver Account Management & Deletion Flow

```mermaid
flowchart TD
    A[User / Driver on Dashboard] -->|Click Settings| B[Settings View: /user/settings or /driver/settings]
    B --> C{User Action}
    C -->|View History| D[My Rides / Payments Ledger]
    C -->|Log Out| E[POST /auth/logout -> Session Invalidated -> Redirect to Login]
    C -->|Delete Account| F[Confirm Prompt in Browser]
    F -->|Confirmed| G[POST /account/delete]
    G --> H[(MongoDB: userRepository.deleteById)]
    H --> I[Session Invalidated & Redirect with deleted=true message]
```

---

## 3. Trip State Machine Transition Rules

| Initial State | Trigger Event | Target State | Authorized Actor | Real-Time UI Reflection |
|---|---|---|---|---|
| *(New)* | User submits booking form | `SEARCHING` | Passenger (Port 8080) | User views spinner: *"Waiting for Captain to accept..."* |
| `SEARCHING` | Driver clicks "Accept Ride" | `ACCEPTED` | Driver (Port 8081) | User views Captain details, OTP code & animated car |
| `SEARCHING` | Rider cancels request | `CANCELLED` | Passenger (Port 8080) | Removed from Driver's incoming radar |
| `ACCEPTED` | Driver enters matching 4-digit OTP | `IN_PROGRESS` | Driver (Port 8081) | Animated highway car shows *"Trip in Progress"* |
| `IN_PROGRESS`| Driver reaches destination & completes | `COMPLETED` | Driver (Port 8081) | Fare recorded as `PAID`, added to Driver earnings |
| `SEARCHING` / `ACCEPTED` | Admin cancels | `CANCELLED` | Admin (Port 8082) | Status marked cancelled across both portals |
