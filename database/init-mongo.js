// MongoDB GeoJSON Index Initialization and Collection Schemas for RideEase
// Run this script in mongosh against your database:
// mongosh "$SPRING_DATA_MONGODB_URI" init-mongo.js

db = db.getSiblingDB('rideease_db');

print("Initializing RideEase database collections and geospatial indexes...");

// 1. Drivers collection 2dsphere index for real-time proximity matching
db.drivers.createIndex({ currentLocation: "2dsphere" });
db.drivers.createIndex({ userId: 1 }, { unique: true });
db.drivers.createIndex({ verificationStatus: 1, online: 1, available: 1 });

// 2. Users collection indexes
db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ phone: 1 }, { unique: true });

// 3. Vehicles collection indexes
db.vehicles.createIndex({ driverId: 1 }, { unique: true });
db.vehicles.createIndex({ registrationNumber: 1 }, { unique: true });
db.vehicles.createIndex({ category: 1 });

// 4. Bookings collection indexes
db.bookings.createIndex({ passengerId: 1, createdAt: -1 });
db.bookings.createIndex({ driverId: 1, createdAt: -1 });
db.bookings.createIndex({ status: 1 });
db.bookings.createIndex({ pickupLocation: "2dsphere" });

print("All RideEase indexes created successfully!");
