package com.fintech.ledger;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
public class Position{

    @Id
    @GeneratedValue
    private UUID positionId;

    @Column(nullable=false)
    private UUID accountId;

    @Column(nullable=false)
    private String symbol;

    @Column(nullable=false)
    private int quantity;

     @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal avgPrice;

    protected Position(){}

    public Position(UUID accountId, String symbol, int quantity, BigDecimal avgPrice){
        this.accountId = accountId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.avgPrice = avgPrice;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public void setAvgPrice(BigDecimal avgPrice){
        this.avgPrice = avgPrice;
    }

    public UUID getPositionId() { return positionId; }
    public UUID getAccountId() { return accountId; }
    public String getSymbol() { return symbol; }
    public int getQuantity() { return quantity; }
    public BigDecimal getAvgPrice() { return avgPrice; }

}