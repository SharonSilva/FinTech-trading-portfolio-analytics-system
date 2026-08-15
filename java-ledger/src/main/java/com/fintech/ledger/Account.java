package com.fintech.ledger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID accountId;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal cashBalance;

    @Column(nullable = false)
    private Instant createdAt;

    protected Account() {}

    public Account(String ownerName, BigDecimal cashBalance) {
        this.ownerName = ownerName;
        this.cashBalance = cashBalance;
        this.createdAt = Instant.now();
    }

    public UUID getAccountId() { return accountId; }
    public String getOwnerName() { return ownerName; }
    public BigDecimal getCashBalance() { return cashBalance; }
    public Instant getCreatedAt() { return createdAt; }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }
}