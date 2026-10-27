# RideEase — Engineering Rules, Standards & Guardrails

This document establishes coding conventions, state machine invariants, security standards, and operational rules for the RideEase codebase.

---

## 1. Architectural & Code Integrity Rules

1. **Layered Separation of Concerns:**
   - **Controllers:** Handle HTTP parsing, DTO validation (`@Valid`), and HTTP response wrapping (`ResponseEntity`). No database queries or pricing calculations in controllers.
   - **Services:** Encapsulate all business logic, transactions, state validation, and pricing formulas.
   - **Repositories:** Strictly data access interfaces extending `MongoRepository`. Custom geospatial queries must use Spring Data's `Point` and `Distance`.
   - **Models:** MongoDB entity documents annotated with `@Document`, `@Id`, and `@Indexed`.

2. **No Plain Secrets:**
   - Never commit API keys, database credentials, or JWT signing keys to version control.
   - All secrets must be loaded via environment variables (`.env`) or Spring property placeholders (`${ENV_VAR}`).

3. **Geospatial Coordinate Order Rule (CRITICAL):**
   - GeoJSON specification requires coordinates in **`[longitude, latitude]`** order.
   - *Never* reverse them to `[lat, lon]` when saving or querying `GeoJsonPoint`. Doing so places the coordinates in the wrong hemisphere or ocean.

---

## 2. Booking State Machine Invariants

To avoid double-booking, ghost rides, or invalid trip progress, the following state transitions must be strictly enforced on the server:

```
                  ┌──────────────┐
                  │  REQUESTED   │
                  └──────┬───────┘
                         │ (Search Initiated)
                         ▼
                  ┌──────────────┐
       ┌──────────┤  SEARCHING   ├──────────┐
       │          └──────┬───────┘          │
       │ (Timeout)       │ (Driver Match)   │ (Passenger Cancel)
       ▼                 ▼                  ▼
┌──────────────┐  ┌──────────────┐   ┌─────────────┐
│NO_DRIVER_FND │  │   ACCEPTED   │   │  CANCELLED  │
└──────────────┘  └──────┬───────┘   └─────────────┘
                         │ (Driver Arrives) │ (Cancel before pickup)
                         ▼                  │
                  ┌──────────────┐          │
                  │   ARRIVING   ├──────────┘
                  └──────┬───────┘
                         │ (OTP Verified)
                         ▼
                  ┌──────────────┐
                  │ IN_PROGRESS  │
                  └──────┬───────┘
                         │ (Trip Ended)
                         ▼
                  ┌──────────────┐
                  │  COMPLETED   │
                  └──────────────┘
```

### Invariant Checks:
- A ride cannot move to `IN_PROGRESS` without the correct 4-digit OTP provided by the passenger.
- Once a ride reaches `IN_PROGRESS`, it can **never** be cancelled via `/cancel`; it can only transition to `COMPLETED` or trigger an emergency `SOS`.
- Driver availability flag (`driver.available`) must atomically toggle to `false` when accepting a ride, and return to `true` when the ride is `COMPLETED` or `CANCELLED`.

---

## 3. Security & Access Control Guardrails

1. **Role-Based Authorization:**
   - **`PASSENGER`:** Can only access `/api/rides`, `/api/users/me`, and write reviews for their own bookings.
   - **`DRIVER`:** Can access `/api/drivers/**`, accept assigned rides, and stream GPS locations. Must have `verificationStatus == APPROVED` to go online.
   - **`ADMIN`:** Can access `/api/admin/**`, inspect KYC documents, modify platform fare parameters, and suspend accounts.

2. **Ownership Verification:**
   - Every trip update (`accept`, `start`, `complete`, `cancel`) must verify that the requesting user's JWT ID matches either the passenger or assigned driver ID on the booking record.

3. **Password Security:**
   - Always hash passwords using `BCryptPasswordEncoder` with a strength factor of at least 10 before saving to MongoDB. Plaintext passwords must never hit the database.

---

## 4. REST API Convention Standards

- Standard HTTP verbs:
  - `GET` for retrieval (idempotent, no side effects)
  - `POST` for resource creation (returns `201 Created`)
  - `PATCH` for state changes and partial updates
  - `DELETE` for removal
- Consistent JSON error response structure:
  ```json
  {
    "timestamp": "2026-10-27T14:00:00Z",
    "status": 400,
    "error": "Bad Request",
    "message": "Invalid OTP code provided",
    "path": "/api/rides/123/start"
  }
  ```
