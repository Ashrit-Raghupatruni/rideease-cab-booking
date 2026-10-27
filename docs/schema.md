# RideEase — Database Schema & Data Dictionary

This document details the MongoDB collections, fields, types, indexes, and document relationships in the `rideease_db` database on MongoDB Atlas.

---

## 1. Entity Relationship Overview

```
 ┌──────────────────────┐         1:1         ┌──────────────────────┐
 │        users         ├────────────────────►│       drivers        │
 │                      │                     │                      │
 │ _id                  │                     │ _id, userId          │
 │ email, phone, role   │                     │ licenseNumber        │
 └──────────┬───────────┘                     │ currentLocation [Geo]│
            │                                 └──────────┬───────────┘
            │ 1:N (as Passenger)                         │ 1:1
            ▼                                            ▼
 ┌──────────────────────┐         1:N         ┌──────────────────────┐
 │       bookings       │◄────────────────────┤       vehicles       │
 │                      │  (as Driver)        │                      │
 │ _id, passengerId     │                     │ _id, driverId        │
 │ driverId, status     │                     │ category, regNumber  │
 │ pickupLocation [Geo] │                     └──────────────────────┘
 │ dropoffLocation[Geo] │
 └──────────┬───────────┘
            │
            │ 1:1
            ▼
 ┌──────────────────────┐
 │       reviews        │
 │                      │
 │ _id, bookingId       │
 │ rating (1-5), comment│
 └──────────────────────┘
```

---

## 2. Collections & Document Structures

### 2.1. `users` Collection
Stores core authentication and profile information for all system actors.

```json
{
  "_id": "ObjectId('662cc...1')",
  "name": "Rahul Sharma",
  "email": "rahul.sharma@example.com",
  "phone": "+919876543210",
  "password": "$2a$10$e7...hashed_bcrypt_password...",
  "role": "PASSENGER", // PASSENGER | DRIVER | ADMIN
  "profilePhotoUrl": "https://res.cloudinary.com/sy4opzcj/image/upload/v1/rideease/profiles/user_1.jpg",
  "active": true,
  "createdAt": "ISODate('2026-10-27T10:00:00Z')",
  "updatedAt": "ISODate('2026-10-27T10:00:00Z')"
}
```
**Indexes:**
- `{ email: 1 }` (Unique)
- `{ phone: 1 }` (Unique)

---

### 2.2. `drivers` Collection
Stores professional credentials, verification state, availability, and real-time GPS location.

```json
{
  "_id": "ObjectId('662cc...2')",
  "userId": "662cc...1",
  "licenseNumber": "DL-042021008899",
  "licensePhotoUrl": "https://res.cloudinary.com/sy4opzcj/image/upload/v1/rideease/kyc/license_d1.jpg",
  "verificationStatus": "APPROVED", // PENDING | APPROVED | REJECTED | SUSPENDED
  "online": true,
  "available": true,
  "currentLocation": {
    "type": "Point",
    "coordinates": [77.5946, 12.9716] // [longitude, latitude]
  },
  "rating": 4.88,
  "totalTrips": 340
}
```
**Indexes:**
- `{ currentLocation: "2dsphere" }` (Geospatial proximity search)
- `{ userId: 1 }` (Unique)
- `{ verificationStatus: 1, online: 1, available: 1 }` (Compound index for candidate filtering)

---

### 2.3. `vehicles` Collection
Stores vehicle specifications and category mapping tied to an approved driver.

```json
{
  "_id": "ObjectId('662cc...3')",
  "driverId": "662cc...2",
  "category": "SEDAN", // MINI | SEDAN | SUV
  "make": "Honda",
  "model": "City",
  "color": "White",
  "registrationNumber": "KA-01-MJ-4050",
  "vehiclePhotoUrl": "https://res.cloudinary.com/sy4opzcj/image/upload/v1/rideease/vehicles/car_1.jpg"
}
```
**Indexes:**
- `{ driverId: 1 }` (Unique)
- `{ registrationNumber: 1 }` (Unique)
- `{ category: 1 }`

---

### 2.4. `bookings` Collection
Represents the full trip lifecycle from estimate request to settlement.

```json
{
  "_id": "ObjectId('662cc...4')",
  "passengerId": "662cc...1",
  "driverId": "662cc...2",
  "vehicleCategory": "SEDAN",
  "pickupAddress": "North Campus Gate, College Road",
  "pickupLocation": {
    "type": "Point",
    "coordinates": [77.5801, 12.9720]
  },
  "dropoffAddress": "City Central Railway Station",
  "dropoffLocation": {
    "type": "Point",
    "coordinates": [77.6050, 12.9810]
  },
  "distanceKm": 8.5,
  "estimatedDurationMinutes": 22,
  "estimatedFare": 210.00,
  "finalFare": 210.00,
  "status": "COMPLETED", // REQUESTED | SEARCHING | ACCEPTED | ARRIVING | IN_PROGRESS | COMPLETED | CANCELLED
  "otp": "8492",
  "paymentStatus": "PAID", // PENDING | PAID | FAILED
  "paymentMethod": "CASH", // CASH | ONLINE | WALLET
  "createdAt": "ISODate('2026-10-27T10:15:00Z')",
  "updatedAt": "ISODate('2026-10-27T10:42:00Z')"
}
```
**Indexes:**
- `{ passengerId: 1, createdAt: -1 }`
- `{ driverId: 1, createdAt: -1 }`
- `{ status: 1 }`
- `{ pickupLocation: "2dsphere" }`

---

### 2.5. `reviews` Collection
Stores post-ride passenger feedback and star ratings.

```json
{
  "_id": "ObjectId('662cc...5')",
  "bookingId": "662cc...4",
  "passengerId": "662cc...1",
  "driverId": "662cc...2",
  "rating": 5, // Integer 1 - 5
  "comment": "Driver was polite, arrived quickly, and vehicle was very clean.",
  "createdAt": "ISODate('2026-10-27T10:45:00Z')"
}
```
**Indexes:**
- `{ bookingId: 1 }` (Unique)
- `{ driverId: 1 }`
