# RideEase — Database Schema & Data Dictionary (MySQL 8.0 & Spring Data JPA)

This document details the relational database schema, tables, columns, indexes, foreign keys, and entity relationships in the `rideease_db` database on MySQL.

---

## 1. Entity Relationship Diagram (ERD)

```
 ┌──────────────────────┐         1:1         ┌──────────────────────┐
 │        users         ├────────────────────►│       drivers        │
 │                      │                     │                      │
 │ id (PK)              │                     │ id (PK), user_id(FK) │
 │ email (UK), phone(UK)│                     │ license_number       │
 │ role, wallet_balance │                     │ current_latitude     │
 └──────────┬───────────┘                     │ current_longitude    │
            │                                 │ verification_status  │
            │ 1:N (as Passenger)              └──────────┬───────────┘
            ▼                                            │ 1:1
 ┌──────────────────────┐         1:N                    ▼
 │       bookings       │◄────────────────────┌──────────────────────┐
 │                      │  (as Driver)        │       vehicles       │
 │ id (PK)              │                     │                      │
 │ passenger_id (FK)    │                     │ id (PK), driver_id   │
 │ driver_id (FK)       │                     │ category, reg_number │
 │ pickup_lat, lon      │                     │ rc_book_url          │
 │ dropoff_lat, lon     │                     │ insurance_policy_url │
 │ status, otp, fare    │                     └──────────────────────┘
 └──────────┬───────────┘
            │
            │ 1:1
            ▼
 ┌──────────────────────┐
 │       reviews        │
 │                      │
 │ id (PK), booking_id  │
 │ driver_id, rating    │
 └──────────────────────┘
```

---

## 2. Table Specifications

### 2.1. `users` Table
Stores authentication credentials, profile data, roles, and digital wallet balances.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(64)` | PRIMARY KEY | Unique user identifier (UUID) |
| `name` | `VARCHAR(255)` | NULLABLE | Full name of rider/driver/admin |
| `email` | `VARCHAR(255)` | NOT NULL, UNIQUE | Primary login email |
| `phone` | `VARCHAR(255)` | NOT NULL, UNIQUE | Contact mobile number |
| `password` | `VARCHAR(255)` | NOT NULL | BCrypt hashed password |
| `role` | `VARCHAR(32)` | NOT NULL | `PASSENGER`, `DRIVER`, or `ADMIN` |
| `profile_photo_url` | `VARCHAR(500)` | NULLABLE | Cloudinary image URL |
| `active` | `BOOLEAN` | DEFAULT TRUE | Account activity status |
| `wallet_balance` | `DOUBLE` | DEFAULT 1500.0 | Digital wallet balance (INR) |
| `created_at` | `TIMESTAMP` | CURRENT_TIMESTAMP | Account creation timestamp |
| `updated_at` | `TIMESTAMP` | AUTO-UPDATE | Last modification timestamp |

### 2.2. `drivers` Table
Stores captain records, KYC verification status, Cloudinary document links, and real-time GPS coordinates.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(64)` | PRIMARY KEY | Unique driver identifier |
| `user_id` | `VARCHAR(64)` | NOT NULL, UNIQUE | Foreign key referencing `users.id` |
| `license_number` | `VARCHAR(255)` | NULLABLE | Driving license number |
| `license_photo_url`| `VARCHAR(500)` | NULLABLE | Cloudinary license scan URL |
| `driver_photo_url` | `VARCHAR(500)` | NULLABLE | Cloudinary driver face photo |
| `rc_document_url`  | `VARCHAR(500)` | NULLABLE | Cloudinary RC certificate URL |
| `insurance_document_url` | `VARCHAR(500)` | NULLABLE | Cloudinary insurance policy URL |
| `permit_document_url` | `VARCHAR(500)` | NULLABLE | Cloudinary permit scan URL |
| `verification_status` | `VARCHAR(32)` | DEFAULT 'PENDING'| `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` |
| `verification_notes` | `VARCHAR(500)`| NULLABLE | Admin verification feedback |
| `online` | `BOOLEAN` | DEFAULT FALSE | Duty toggle status |
| `available` | `BOOLEAN` | DEFAULT TRUE | Availability for new dispatch |
| `current_latitude` | `DOUBLE` | NULLABLE | High-accuracy GPS latitude |
| `current_longitude`| `DOUBLE` | NULLABLE | High-accuracy GPS longitude |
| `rating` | `DOUBLE` | DEFAULT 5.0 | Driver star rating |
| `total_trips` | `INT` | DEFAULT 0 | Completed trip count |

### 2.3. `vehicles` Table
Stores vehicle attributes, category tier, and compliance documentation.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(64)` | PRIMARY KEY | Unique vehicle identifier |
| `driver_id` | `VARCHAR(64)` | NOT NULL, UNIQUE | Foreign key to `drivers.id` |
| `category` | `VARCHAR(32)` | NOT NULL | `MINI`, `SEDAN`, `SUV` |
| `make` | `VARCHAR(255)` | NULLABLE | Manufacturer (e.g. Maruti, Tata) |
| `model` | `VARCHAR(255)` | NULLABLE | Model name (e.g. Dzire, Nexon) |
| `color` | `VARCHAR(255)` | NULLABLE | Exterior vehicle color |
| `manufacturing_year` | `INT` | NULLABLE | Production year |
| `registration_number` | `VARCHAR(255)` | NOT NULL, UNIQUE | Vehicle license plate |
| `vehicle_photo_url` | `VARCHAR(500)` | NULLABLE | Cloudinary vehicle photo |
| `rc_book_url` | `VARCHAR(500)` | NULLABLE | Cloudinary RC book scan |
| `insurance_policy_url`| `VARCHAR(500)` | NULLABLE | Cloudinary insurance document |
| `fitness_certificate_url` | `VARCHAR(500)` | NULLABLE | Cloudinary permit/fitness certificate |
| `verification_status` | `VARCHAR(32)` | DEFAULT 'PENDING'| `PENDING`, `APPROVED`, `REJECTED` |

### 2.4. `bookings` Table
Core transactional ledger capturing ride dispatches, route coordinates, OTP validation, and payment settlement.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `VARCHAR(64)` | PRIMARY KEY | Unique booking identifier |
| `passenger_id` | `VARCHAR(64)` | INDEXED | Foreign key to `users.id` |
| `driver_id` | `VARCHAR(64)` | INDEXED | Foreign key to `drivers.id` |
| `vehicle_category` | `VARCHAR(32)` | NOT NULL | `MINI`, `SEDAN`, `SUV` |
| `pickup_address` | `VARCHAR(500)` | NULLABLE | Full address string of pickup |
| `pickup_latitude` | `DOUBLE` | NOT NULL | Pickup GPS latitude |
| `pickup_longitude` | `DOUBLE` | NOT NULL | Pickup GPS longitude |
| `dropoff_address` | `VARCHAR(500)` | NULLABLE | Full address string of destination |
| `dropoff_latitude` | `DOUBLE` | NOT NULL | Destination GPS latitude |
| `dropoff_longitude`| `DOUBLE` | NOT NULL | Destination GPS longitude |
| `distance_km` | `DOUBLE` | NOT NULL | OSRM computed distance in KM |
| `estimated_duration_minutes` | `INT` | NOT NULL | Estimated travel time |
| `estimated_fare` | `DOUBLE` | NOT NULL | Upfront road-verified fare (INR) |
| `final_fare` | `DOUBLE` | NULLABLE | Final billed fare after trip |
| `status` | `VARCHAR(32)` | INDEXED | `REQUESTED`, `SEARCHING`, `ACCEPTED`, `ARRIVING`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED` |
| `otp` | `VARCHAR(8)` | NULLABLE | Cryptographic 4-digit ride OTP |
| `otp_attempts` | `INT` | DEFAULT 0 | Failed verification attempt counter |
| `payment_status` | `VARCHAR(32)` | NULLABLE | `PENDING`, `PAID`, `FAILED` |
| `payment_method` | `VARCHAR(32)` | NULLABLE | `CASH`, `WALLET`, `UPI`, `CARD` |
| `payment_transaction_id` | `VARCHAR(64)` | NULLABLE | Settlement receipt ID |
| `paid_at` | `TIMESTAMP` | NULLABLE | Payment confirmation time |
