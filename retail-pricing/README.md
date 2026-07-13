# retail-pricing

**Many rules working together — the RuleSet lifecycle, end to end.**

A checkout pricing engine for a shopping cart: membership discounts, bulk
discounts, coupon codes, a discount cap, and a computed final price.

## The RuleSet lifecycle

```
params          declare (and type-check) the inputs; defaults created if missing
preCondition    should this RuleSet run at all? (false -> nothing runs)
initializer     one-time setup before the first rule
rules           run in declaration order, talking to each other via Bindings
stopCondition   checked after every rule; true -> stop early
finalizer       always runs after the rules
resultExtractor turns the final Bindings into the caller's return value
errorHandler    converts an execution error into a fallback result
```

## What this sample shows

| Concept | Where in `PricingRules` |
|---|---|
| Typed params, required params | `.param("subtotal", Double.class, true)` |
| Param **defaults**, computed from other bindings | `.param("total", Double.class, function((Double subtotal) -> subtotal))` |
| RuleSet `preCondition` (empty cart → nothing runs) | `.preCondition(...)` |
| `initializer` creating a scoped working binding | `.initializer(...)` binds `coupon` |
| Rules communicating via Bindings | every rule reads/updates `discountPercent` |
| Rule-level `preCondition` → `SKIPPED` status | `couponRule` when no coupon supplied |
| `otherwise` action on condition failure | `couponRule` for an invalid code |
| `stopCondition` (early exit at the 20% cap) | `.stopCondition(...)` |
| `finalizer` computing the final price | `.finalizer(...)` |
| `resultExtractor` + reserved `ruleSetStatus` binding | `.resultExtractor(...)` |
| `errorHandler` + reserved `ex` binding | `.errorHandler(...)` |
| Per-rule results (`PASS` / `FAIL` / `SKIPPED`) | `ruleTrace` in the output |

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.pricing.RetailPricingRunner
```

Six scenarios run — a gold member hitting the discount cap (stopCondition fires
before the coupon rule ever runs), a silver member stacking a coupon, an invalid
coupon (otherwise action), a missing coupon (rule SKIPPED), an empty cart
(RuleSet preCondition fails), and a simulated coupon-service outage
(errorHandler returns a safe fallback with no discounts).

## Things to notice

- **`FAIL` is a normal outcome.** `goldMemberRule=FAIL` just means "this cart
  isn't a gold member" — the rule's condition didn't pass. Errors are a separate
  status (`ERROR`).
- **`loadProperties(cart)` binds read-only values.** Rules can read the cart's
  properties but attempts to `setValue` them throw — inputs stay clean, and all
  mutable working state is declared explicitly as params with defaults.
- **Scopes**: the `coupon` binding created by the initializer lives in the
  RuleSet's scope and vanishes when the run ends; the caller never sees it.
- The `errorHandler` receives the exception through the reserved `ex` binding;
  the `resultExtractor` can see per-rule results through the reserved
  `ruleSetStatus` binding.

## Next step

[`signup-validation`](../signup-validation) covers rulii's validation framework —
the 37 built-in validators and how to write your own.
