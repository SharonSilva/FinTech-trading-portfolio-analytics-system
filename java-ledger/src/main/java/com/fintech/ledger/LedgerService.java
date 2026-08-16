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
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        BigDecimal cost = price.multiply(BigDecimal.valueOf(quantity));

        if (account.getCashBalance().compareTo(cost) < 0) {
            throw new IllegalStateException("Insufficient funds: balance "
                    + account.getCashBalance() + " < cost " + cost);
        }

        Trade trade = new Trade(accountId, symbol, Trade.Side.BUY, quantity, price);
        tradeRepository.save(trade);

        BigDecimal newBalance = account.getCashBalance().subtract(cost);
        account.setCashBalance(newBalance);
        accountRepository.save(account);

        LedgerEntry debit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.DEBIT, cost, newBalance);
        ledgerEntryRepository.save(debit);

        LedgerEntry credit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.CREDIT, cost, newBalance);
        ledgerEntryRepository.save(credit);

        Position position = positionRepository
                .findByAccountIdAndSymbol(accountId, symbol)
                .orElse(new Position(accountId, symbol, 0, BigDecimal.ZERO));

        // Weighted-average cost basis:
        // newAvg = (oldQty*oldAvg + buyQty*buyPrice) / (oldQty + buyQty)
        int oldQty = position.getQuantity();
        BigDecimal oldCostTotal = position.getAvgPrice().multiply(BigDecimal.valueOf(oldQty));
        BigDecimal addedCostTotal = price.multiply(BigDecimal.valueOf(quantity));
        int newQty = oldQty + quantity;
        BigDecimal newAvg = oldCostTotal.add(addedCostTotal)
                .divide(BigDecimal.valueOf(newQty), 4, java.math.RoundingMode.HALF_UP);

        position.setQuantity(newQty);
        position.setAvgPrice(newAvg);
        positionRepository.save(position);

        return trade;
    }

    @Transactional
    public Trade executeSell(UUID accountId, String symbol, int quantity, BigDecimal price) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        Position position = positionRepository
                .findByAccountIdAndSymbol(accountId, symbol)
                .orElseThrow(() -> new IllegalStateException("No position in " + symbol + " to sell"));

        if (position.getQuantity() < quantity) {
            throw new IllegalStateException("Insufficient shares: holding "
                    + position.getQuantity() + " < sell quantity " + quantity);
        }

        BigDecimal proceeds = price.multiply(BigDecimal.valueOf(quantity));

        Trade trade = new Trade(accountId, symbol, Trade.Side.SELL, quantity, price);
        tradeRepository.save(trade);

        BigDecimal newBalance = account.getCashBalance().add(proceeds);
        account.setCashBalance(newBalance);
        accountRepository.save(account);

        // Double-entry: CREDIT cash (money coming in), DEBIT the position (shares leaving)
        LedgerEntry credit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.CREDIT, proceeds, newBalance);
        ledgerEntryRepository.save(credit);

        LedgerEntry debit = new LedgerEntry(
                trade.getTradeId(), accountId,
                LedgerEntry.EntryType.DEBIT, proceeds, newBalance);
        ledgerEntryRepository.save(debit);

        // Reduce the position. Avg price stays the same when selling
        // (selling doesn't change your cost basis on remaining shares).
        int newQty = position.getQuantity() - quantity;
        position.setQuantity(newQty);
        positionRepository.save(position);

        return trade;
    }
}