package com.rideease.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_booking", columnList = "bookingId"),
    @Index(name = "idx_review_driver", columnList = "driverId"),
    @Index(name = "idx_review_passenger", columnList = "passengerId")
})
public class Review {

    @Id
    @Column(length = 64)
    private String id;

    @Column(length = 64)
    private String bookingId;

    @Column(length = 64)
    private String passengerId;

    @Column(length = 64)
    private String driverId;

    private int rating; // 1 to 5 stars

    @Column(length = 1000)
    private String comment;

    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    public Review() {}

    public Review(String id, String bookingId, String passengerId, String driverId, int rating, String comment, Instant createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    public String getPassengerId() { return passengerId; }
    public void setPassengerId(String passengerId) { this.passengerId = passengerId; }

    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
