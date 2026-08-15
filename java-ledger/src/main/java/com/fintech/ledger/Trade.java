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
public class Trade{

    public enum Side{
        BUY,
        SELL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tradeId;

    @Column(nullable=false)
    private UUID accountId;

    @Column(nullable=false)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private Side side;

    @Column(nullable=false)
    private int quantity;

    @Column(nullable=false,precision=19,scale=4)
    private BigDecimal price;

    @Column(nullable=false)
    private Instant executedAt;

    protected Trade() {}

    public Trade(UUID accountId, String symbol, Side side, int quantity, BigDecimal price){
        this.accountId = accountId;
        this.symbol = symbol;
        this.side = side;
        this.quantity = quantity;
        this.price = price;
        this.executedAt = Instant.now();
    }

    public UUID getTradeId(){return tradeId;}
    public UUID getAccountId(){return accountId;}
    public String getSymbol(){return symbol;}
    public Side getSide(){return side;}
    public int getQuantity(){return quantity;}
    public BigDecimal getPrice(){return price;}
    public Instant getExecutedAt(){return executedAt;}
}