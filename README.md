# Rulii Samples

**Real-world, runnable examples for [rulii](https://github.com/algox/rulii) and
[rulii-spring](https://github.com/algox/rulii-spring) 2.0** — each sample is a
standalone Maven project with its own README, a relatable domain, and expected
output you can verify.

## The learning path

Work through them in order — each sample owns one concept and builds on the
previous ones.

### Core rulii (plain Java)

| # | Sample | Domain | What it teaches |
|---|---|---|---|
| 1 | [`hello-world`](hello-world) | Greetings | Your first Rule, two ways (annotated class / builder); Bindings basics; `RuleResult` |
| 2 | [`retail-pricing`](retail-pricing) | Cart pricing | The **RuleSet** lifecycle: params & defaults, pre/stop conditions, initializer/finalizer, resultExtractor, errorHandler |
| 3 | [`signup-validation`](signup-validation) | User registration | The **validation framework**: 37 built-in validators, custom validators, severities, `RuleViolations`, `ValidationException` |
| 4 | [`claims-ruleflow`](claims-ruleflow) | Insurance claims | **RuleFlow** (the 2.0 headline): run/apply/execute, when/exit, forEach, scope, step & global exception handlers, listeners |
| 5 | [`fraud-screening-async`](fraud-screening-async) | Payment fraud | **Async RuleFlow**: asyncRun, awaitAll, continuations, async error handling, context modes (SHARED vs IMMUTABLE) |
| 6 | [`dynamic-promo-scripting`](dynamic-promo-scripting) | Promotions | **Scripting**: rule logic in JavaScript files (GraalJS), script-backed Conditions/Actions/Functions |

### rulii-spring (Spring Boot)

| # | Sample | Domain | What it teaches |
|---|---|---|---|
| 7 | [`checkout-rules-spring`](checkout-rules-spring) | Checkout/shipping | **Declarative rules**: XML + SpEL, `@RuleScan`, `${property}` placeholders, configured messages, profile-gated rules |
| 8 | [`order-fulfillment-flow-spring`](order-fulfillment-flow-spring) | Order fulfillment | **`<r:ruleflow>` in XML**: async steps, exception routing, registry lookups, DI into rules, listeners, executor override, testing with a fixed Clock |
| 9 | [`spring-boot-sample`](spring-boot-sample) | Auto loan (REST) | **The capstone**: XML validation + Java rulesets + a RuleFlow orchestrating a real loan decision end to end |

## Feature index

Looking for "how do I …"?

| Feature | Sample(s) |
|---|---|
| Write a rule (class / builder / lambda) | hello-world |
| Pass data in (Bindings, declarations, POJOs) | hello-world, retail-pricing |
| Group rules, control execution order | retail-pricing |
| Validate input & collect violations | signup-validation, checkout-rules-spring |
| Write a custom validator | signup-validation |
| Orchestrate rules into a pipeline | claims-ruleflow, spring-boot-sample |
| Handle errors (step / global / fallback) | retail-pricing, claims-ruleflow, order-fulfillment-flow-spring |
| Run rules in parallel | fraud-screening-async, order-fulfillment-flow-spring |
| Load rule logic at runtime (scripts) | dynamic-promo-scripting |
| Author rules in XML / SpEL | checkout-rules-spring, order-fulfillment-flow-spring |
| Externalize thresholds & messages to config | checkout-rules-spring, spring-boot-sample |
| Inject Spring beans into rules | order-fulfillment-flow-spring, spring-boot-sample |
| Trace / audit rule execution | claims-ruleflow, order-fulfillment-flow-spring |
| Test rules deterministically | order-fulfillment-flow-spring, spring-boot-sample |

## Requirements

- Java 17+
- Maven 3.9+
- rulii 2.0.0 / rulii-spring 2.0.0 (from Maven Central)

Each sample builds independently:

```bash
cd <sample>
mvn test          # or: mvn spring-boot:run / mvn compile exec:java (see the sample's README)
```