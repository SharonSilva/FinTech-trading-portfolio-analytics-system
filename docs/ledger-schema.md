# Ledger Schema

## Account
| Field       | Type      | Description                        |
|-------------|-----------|-------------------------------------|
| account_id  | UUID      | unique identifier                  |
| owner_name  | string    | account holder                     |
| cash_balance| decimal   | current available cash             |
| created_at  | ISO 8601  | when account was opened            |

## Position
| Field       | Type      | Description                        |
|-------------|-----------|-------------------------------------|
| position_id | UUID      | unique identifier                  |
| account_id  | UUID      | FK -> Account                      |
| symbol      | string    | e.g. "AAPL"                        |
| quantity    | integer   | shares currently held               |
| avg_price   | decimal   | average cost basis per share       |

## Trade
| Field       | Type      | Description                        |
|-------------|-----------|-------------------------------------|
| trade_id    | UUID      | unique identifier                  |
| account_id  | UUID      | FK -> Account                      |
| symbol      | string    | e.g. "AAPL"                        |
| side        | enum      | BUY or SELL                        |
| quantity    | integer   | shares traded                      |
| price       | decimal   | execution price                    |
| executed_at | ISO 8601  | when the trade happened            |

## LedgerEntry (double-entry log)
| Field       | Type      | Description                        |
|-------------|-----------|-------------------------------------|
| entry_id    | UUID      | unique identifier                  |
| trade_id    | UUID      | FK -> Trade                        |
| account_id  | UUID      | FK -> Account                      |
| entry_type  | enum      | DEBIT or CREDIT                    |
| amount      | decimal   | value of this entry                |
| balance_after| decimal  | account balance after this entry   |
| created_at  | ISO 8601  | immutable — never updated          |