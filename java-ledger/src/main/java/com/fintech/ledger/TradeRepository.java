package com.fintech.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<Trade, UUID>{
    List<Trade> findByAccountId(UUID accountId);
}