package com.rideease.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "safety_contacts")
public class SafetyContact {

    @Id
    private String id;
    private String userId;
    private String name;
    private String phone;
    private String relationship;

    @CreatedDate
    private Instant createdAt = Instant.now();

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
