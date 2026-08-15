package com.fintech.ledger;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class LedgerService {

    private final AccountRepository accountRepository;
    private final TradeRepository tradeRepository;
    private final PositionRepository positionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public LedgerService(AccountRepository accountRepository,
                         TradeRepository tradeRepository,
                         PositionRepository positionRepository,
                         LedgerEntryRepository ledgerEntryRepository) {
        this.accountRepository = accountRepository;
        this.tradeRepository = tradeRepository;
        this.positionRepository = positionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Trade executeBuy(UUID accountId, String symbol, int quantity, BigDecimal price) {
        // 1. Load the account (must exist)
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        // 2. Compute the total cost of this buy
        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));

        // 3. Check sufficient funds
        if (account.getCashBalance().compareTo(cost) < 0) {
            throw new IllegalStateException("Insufficient funds: balance "
                    + account.getCashBalance() + " < cost " + cost);
        }

        // 4. Record the trade
        Trade trade = new Trade(accountId, symbol, Trade.Side.BUY, quantity, price);
        tradeRepository.save(trade);

        // 5. Update cash balance
        BigDecimal newBalance = account.getCashBalance().subtract(cost);
        account.setCashBalance(newBalance);
        accountRepository.save(account);

        // 6. Double-entry: DEBIT cash (money leaving the account)
        LedgerEntry debit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.DEBIT, cost, newBalance);
        ledgerEntryRepository.save(debit);

        // 7. Double-entry: CREDIT the position (shares acquired)
        LedgerEntry credit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.CREDIT, cost, newBalance);
        ledgerEntryRepository.save(credit);

        // 8. Update or create the position
        Position position = positionRepository
                .findByAccountIdAndSymbol(accountId, symbol)
                .orElse(new Position(accountId, symbol, 0, BigDecimal.ZERO));

        int newQty = position.getQuantity() + quantity;
        position.setQuantity(newQty);
        position.setAvgPrice(price); // simplified for now; weighted avg comes later
        positionRepository.save(position);

        return trade;
    }
}