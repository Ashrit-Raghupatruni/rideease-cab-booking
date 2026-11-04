package com.rideease.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "safety_contacts", indexes = {
    @Index(name = "idx_safety_contact_user", columnList = "userId")
})
public class SafetyContact {

    @Id
    @Column(length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String userId;

    private String name;
    private String phone;
    private String relationship;

    private Instant createdAt = Instant.now();

    @PrePersist
    protected void onCreate() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        if (this.createdAt == null) this.createdAt = Instant.now();
    }

    public SafetyContact() {}

    public SafetyContact(String userId, String name, String phone, String relationship) {
        this.userId = userId;
        this.name = name;
        this.phone = phone;
        this.relationship = relationship;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
