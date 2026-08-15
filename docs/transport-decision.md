# Transport Decision: Event Queue over REST

## Decision
Services communicate via Kafka topics, not direct REST calls.

## Why
- Go publishes market ticks continuously (many per second) — REST would mean
  Java/Python polling constantly, wasting resources and adding lag.
- A queue decouples producers from consumers: Go doesn't need to know who's
  listening, or whether they're even online. If Python's risk service crashes
  and restarts, it just resumes consuming — no missed calls, no retry logic
  needed on Go's side.
- This mirrors real trading infrastructure, where market data fan-out to many
  downstream consumers (risk engines, ledgers, dashboards) is a textbook
  pub/sub problem, not a request/response one.

## Where REST still makes sense
- Java's ledger exposes REST endpoints for the dashboard to fetch current
  positions/balances on demand (a "give me current state" query, not a
  stream) — pub/sub is for events, REST is for point-in-time lookups.

## Topics (initial)
- `market-data.ticks` — published by Go, consumed by Python + dashboard
- `ledger.updates` — published by Java, consumed by Python
