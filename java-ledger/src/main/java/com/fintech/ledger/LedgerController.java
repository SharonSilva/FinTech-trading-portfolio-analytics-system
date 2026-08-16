package com.fintech.ledger;

import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class LedgerController {

    private final AccountRepository accountRepository;
    private final LedgerService ledgerService;

    public LedgerController(AccountRepository accountRepository, LedgerService ledgerService) {
        this.accountRepository = accountRepository;
        this.ledgerService = ledgerService;
    }

    @PostMapping("/accounts")
    public Account createAccount(@RequestBody CreateAccountRequest request) {
        Account account = new Account(request.ownerName(), request.startingCash());
        return accountRepository.save(account);
    }

    @PostMapping("/trades/buy")
    public Trade buy(@RequestBody BuyRequest request) {
        return ledgerService.executeBuy(
                request.accountId(),
                request.symbol(),
                request.quantity(),
                request.price());
    }

    public record CreateAccountRequest(String ownerName, BigDecimal startingCash) {}
    public record BuyRequest(UUID accountId, String symbol, int quantity, BigDecimal price) {}
}