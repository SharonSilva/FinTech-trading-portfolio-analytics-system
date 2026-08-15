package com.fintech.ledger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
public class LedgerEntry {

    public enum EntryType {
        DEBIT,
        CREDIT
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID entryId;

    @Column(nullable = false)
    private UUID tradeId;

    @Column(nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntry() {}

    public LedgerEntry(UUID tradeId, UUID accountId, EntryType entryType, BigDecimal amount, BigDecimal balanceAfter) {
        this.tradeId = tradeId;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.createdAt = Instant.now();
    }

    public UUID getEntryId() { return entryId; }
    public UUID getTradeId() { return tradeId; }
    public UUID getAccountId() { return accountId; }
    public EntryType getEntryType() { return entryType; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public Instant getCreatedAt() { return createdAt; }
}