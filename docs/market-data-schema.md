# Market Data Tick Schema

| Field     | Type      | Description                          |
|-----------|-----------|---------------------------------------|
| symbol    | string    | e.g. "AAPL"                          |
| price     | decimal   | last traded price                    |
| bid       | decimal   | best bid price                       |
| ask       | decimal   | best ask price                       |
| volume    | integer   | volume traded in this tick/interval  |
| timestamp | ISO 8601  | when this tick was generated (UTC)   |

Example JSON:
{
  "symbol": "AAPL",
  "price": 178.32,
  "bid": 178.30,
  "ask": 178.34,
  "volume": 500,
  "timestamp": "2026-08-12T10:15:30Z"
}