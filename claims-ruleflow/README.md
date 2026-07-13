# claims-ruleflow

**RuleFlow — the headline feature of rulii 2.0** — shown on an insurance-claim
intake pipeline:

```
validate -> look up policy -> coverage checks -> price each line item
         -> compute payout -> decision
```

Before RuleFlow, this orchestration was hand-written service code: create
bindings, build contexts, run each ruleset, check results, try/catch around
every service call. `ClaimsFlow` replaces all of it with one declarative
pipeline.

## What this sample shows

| Concept | Where in `ClaimsFlow` |
|---|---|
| `param` — declared, required flow inputs | `.param("claim", Claim.class, true)` |
| `bind` — working state for the flow | `rejected`, `notes`, `approvedTotal`, `payout` |
| `run(RuleSet)` as a pipeline step | validation + coverage steps |
| `apply(Function)` with `.as("policy")` — enrich & bind a result | the policy lookup |
| **Step-level `onException`** — exception bound as `ex` | validation + policy-lookup steps |
| `when(condition, body)` + `exit(extractor)` — early exit | the three rejection gates |
| `forEach(source, "item", body)` — per-element processing | line-item pricing |
| `scope(name, body)` — transient working bindings | payout computation (`cappedTotal` dies with the scope) |
| **Global `onException`** — the safety net | service outage → `MANUAL_REVIEW` |
| `finalizer` — always runs, even on early exit | the processed log line |
| `returning(extractor)` — shape the final result | builds the `ClaimDecision` |
| `RuleFlowListener` on a `Tracer` — audit trail | `AuditListener` + `RuleContext.builder().traceUsing(tracer)` |

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.claims.ClaimsFlowRunner
```

Six claims are processed: a clean approval (with the audit trail switched on),
a claim with an oversized line item (capped, not rejected), a malformed claim,
an unknown policy, an inactive policy, and a simulated policy-service outage.

## Things to notice

- **Step handlers vs the global handler.** A step's `onException` handles
  *expected* failures right where they occur and the flow continues; the global
  handler is the last line of defense — after it runs, remaining steps are
  skipped and `returning` still produces a result. A step handler is checked
  first; unmatched exceptions fall through to the global one.
- **Make business exceptions extend `UnrulyException`.** The rule pipeline
  passes `UnrulyException` subclasses through unwrapped, so
  `onException(PolicyNotFoundException.class, ...)` can match it directly
  (see `PolicyService.PolicyNotFoundException`). Exceptions of other types get
  wrapped in `UnrulyException` on the way out.
- **Scopes contain the mess.** `cappedTotal` exists only inside `payoutScope`;
  the result is exported by `setValue` on the pre-bound `payout` binding —
  the documented pattern for getting values out of a scope (or a whole flow).
- **The audit trail is free.** Nothing in `ClaimsFlow` knows about auditing;
  the `AuditListener` is attached to the `Tracer` on the RuleContext at
  run time.

## Next step

[`fraud-screening-async`](../fraud-screening-async) runs flow steps **in
parallel** — `asyncRun`, `awaitAll`, continuations and async error handling.