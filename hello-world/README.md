# hello-world

**Your first rulii Rule — in 5 minutes.**

A Rule in rulii is simply:

```
if (Condition) then Action(s) [otherwise Action]
```

Inputs are supplied through named **Bindings**. rulii matches Binding names to the
parameter names of your conditions and actions — no casting, no context lookups.

## What this sample shows

| Concept | Where |
|---|---|
| Class-based rule (`@Rule` / `@Given` / `@Then` / `@Otherwise`) | `HelloWorldRule` |
| Builder-based rule (`Rule.builder().given(...).then(...)`) | `HelloWorldRuleRunner.builderBasedRule()` |
| Supplying inputs via a `Bindings` object | `classBasedRule()` |
| Supplying inputs via `BindingDeclaration` lambdas (`visitorName -> "Kaia"`) | `builderBasedRule()` |
| Supplying inputs via a POJO (each bean property becomes a Binding) | `pojoBindings()` |
| Reading the outcome (`RuleResult` → `PASS` / `FAIL` / `SKIPPED` / `ERROR`) | `classBasedRule()` |

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.HelloWorldRuleRunner
```

Or run `HelloWorldRuleRunner.main()` from your IDE.

## Expected output

```
[class-based] Hello Kaia, welcome to rulii!
[class-based] status: PASS
[builder-based] Hello Kaia, welcome to rulii!
[builder-based] Welcome back Riley!
[class-based] Hello Max, welcome to rulii!
```

## Things to notice

- **Binding names carry the data.** The lambda parameter name in
  `rule.run(visitorName -> "Kaia", firstVisit -> true)` *is* the binding name —
  rulii reads it from the compiled parameter names (compile with debug info,
  which is Maven's default).
- The `@Otherwise` / `.otherwise(...)` action runs when the condition fails —
  a failed rule is a normal outcome (`FAIL`), not an error.
- Both styles produce the same `Rule` type; pick class-based for reusable,
  testable rules and builder-based for quick inline composition.

## Next step

Head to the [`retail-pricing`](../retail-pricing) sample to see many rules
working together in a **RuleSet**.
