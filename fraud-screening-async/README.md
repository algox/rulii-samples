# fraud-screening-async

**Parallel rule execution with RuleFlow** — a payment fraud check that fans
out three independent screens and totals the risk:

```
                 +-> credit bureau  (~150ms)
payment  --------+-> velocity       (~80ms)    -> awaitAll -> score -> decision
                 +-> watchlist      (~100ms)
```

~330ms of combined latency completes in roughly the slowest screen's time.
The elapsed timer in the output proves it.

## What this sample shows

| Concept | Where in `FraudScreeningFlow` |
|---|---|
| `asyncRun(...)` — launch a Rule or sub-RuleFlow in the background | the three screens |
| `.as("name")` — the `CompletableFuture` handle bound for later | `creditFuture`, `velocityFuture`, `watchlistFuture` |
| `awaitAll(timeout, unit, names...)` — barrier with a deadline | after the fan-out |
| `.thenRun(resultName, body)` — non-blocking continuation | logs the credit score the moment it arrives |
| Async `.onException(...)` — fires when the task fails, fail-open | the velocity outage scenario |
| `AsyncContextMode`: `withImmutableBindings()` vs SHARED | sub-flows vs the watchlist rule |
| Reading a result off a completed future | the aggregation step after `awaitAll` |
| Await timeout → `UnrulyException` | the hung-bureau scenario |

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.fraud.FraudScreeningRunner
```

## The context-mode rule of thumb (important!)

An async step runs against one of three contexts (`AsyncContextMode`):

- **SHARED** (default) — the task uses the caller's live context. Fine for a
  single Rule that writes its own pre-bound binding (the watchlist screen).
  **Not safe for concurrent sub-flows or rulesets**: they push scopes onto the
  caller's one shared scope stack, and concurrent push/pop interleaves —
  parameter lookups start reading each other's scopes. (This sample originally
  ran the sub-screens SHARED; the velocity check happily reported a "velocity"
  of 720 — the credit score.)
- **IMMUTABLE** (`withImmutableBindings()`) — the task gets its own context over
  a read-only snapshot of the bindings. Concurrent tasks can't corrupt each
  other; results travel back through the `CompletableFuture`. Use this for
  value-returning parallel work (the credit + velocity screens).
- **CUSTOM** (`withContext(ctx)`) — full control, e.g. a dedicated executor.

## Things to notice

- **A handled async failure completes the future with `null`** — `awaitAll`
  doesn't throw, and the pre-bound default (`velocityCount -> 0`) simply stays:
  fail-open in three lines.
- **The continuation (`thenRun`) is guaranteed to finish before the future
  completes**, so anything awaiting the future also sees the continuation's
  effects.
- The async pool's worker threads are non-daemon — a `main()` that leaves a
  task hanging should `System.exit(0)` (or supply its own executor via a
  custom context).

## Next step

[`dynamic-promo-scripting`](../dynamic-promo-scripting) moves rule logic out
of Java entirely — conditions written as scripts, loaded at runtime.