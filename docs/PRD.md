# RideEase — Product Requirements Document (PRD)

## 1. Document Overview
- **Project Name:** RideEase — Online Cab Booking Platform
- **Target Audience:** Daily commuters, on-demand taxi passengers, commercial cab drivers, platform operations and fleet administrators.
- **Goal:** Deliver a dependable, real-time cab hailing platform supporting passenger booking, proximity-based driver matching, fare estimation, document-verified driver onboarding, and administrator oversight.

---

## 2. Problem Statement
Urban and campus commuters face unreliable cab dispatching, unpredictable pricing, and lack of real-time visibility into driver arrival. Traditional cab services lack automated fare estimation, digital KYC document checks, and real-time trip synchronization across web and mobile interfaces.

---

## 3. User Personas

### 3.1. Passenger (Rider)
- **Profile:** Students, daily office workers, travelers needing on-demand city and airport transit.
- **Pain Points:** Hidden surges, slow matching, lack of route transparency, safety concerns.
- **Needs:** Quick pickup/destination selection, accurate upfront fare estimates, live vehicle location tracking, digital receipts, driver ratings.

### 3.2. Driver (Partner)
- **Profile:** Licensed commercial vehicle operators looking for flexible trip requests.
- **Pain Points:** Empty return trips, complex KYC approval delays, unfair dispatching.
- **Needs:** Streamlined onboarding (license/vehicle photo upload), simple toggle for Online/Offline availability, instant audio/visual ride request alerts, route navigation, earnings ledger.

### 3.3. Platform Administrator
- **Profile:** Fleet supervisors and customer satisfaction officers.
- **Pain Points:** Fraudulent accounts, unauthorized drivers, customer dispute resolution.
- **Needs:** KYC document verification dashboard, driver suspension controls, live ride monitoring, dynamic fare parameter controls, dispute ticket resolution.

---

## 4. Key Functional Requirements

| ID | Module | Requirement Description | Priority |
|---|---|---|---|
| **FR-01** | Authentication | JWT-based registration, login, and session validation for Passenger, Driver, and Admin roles | P0 (Must) |
| **FR-02** | KYC Onboarding | Driver registration with license upload to Cloudinary CDN; admin review workflow (PENDING -> APPROVED/REJECTED) | P0 (Must) |
| **FR-03** | Fare Estimation | Server-side calculation based on distance, duration, vehicle tier (Mini, Sedan, SUV), and surge factor | P0 (Must) |
| **FR-04** | Driver Matching | MongoDB `2dsphere` proximity query finding online, available, approved drivers within an 8 km radius | P0 (Must) |
| **FR-05** | Booking Lifecycle | State machine: `REQUESTED` -> `SEARCHING` -> `ACCEPTED` -> `ARRIVING` -> `IN_PROGRESS` -> `COMPLETED` / `CANCELLED` | P0 (Must) |
| **FR-06** | Real-Time Sync | Spring WebSocket (STOMP) broadcasting live driver GPS coordinates and trip state transitions | P1 (High) |
| **FR-07** | Ride Start OTP | 4-digit verification code generated on match, verified by driver before setting state to `IN_PROGRESS` | P1 (High) |
| **FR-08** | Payment & Invoicing | Record payment method (Cash, Digital, Wallet), calculate final fare, generate itemized digital receipts | P1 (High) |
| **FR-09** | Ratings & Reviews | Post-trip 1-to-5 star rating and feedback submitted by rider; driver average rating updated atomically | P1 (High) |
| **FR-10** | Admin Console | Real-time overview of active rides, pending driver verifications, fare configuration adjustments, and user controls | P1 (High) |

---

## 5. Non-Functional Requirements

- **Performance:** Fare estimate API response time < 200 ms; driver matching dispatch < 1.5 seconds.
- **Scalability:** Stateless Spring Boot backend supporting horizontal scaling; MongoDB 2dsphere indexing for high-concurrency spatial queries.
- **Availability:** 99.9% uptime target; graceful degradation if external map services experience latency.
- **Security:**
  - Passwords hashed using BCrypt.
  - JWT tokens signed with HMAC-SHA256 and expiration enforcement.
  - Role-Based Access Control (RBAC) on all protected endpoints.
  - Cloudinary credentials isolated in environment variables.

---

## 6. Success Metrics & KPIs
1. **Match Success Rate:** > 85% of ride requests matched within 60 seconds.
2. **KYC Verification Cycle Time:** < 24 hours from driver registration to administrative decision.
3. **Cancellation Rate:** < 8% passenger cancellations after driver assignment.
4. **Platform Rating:** Maintain average platform trip rating >= 4.5 / 5.0.
