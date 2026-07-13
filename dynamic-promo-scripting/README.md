# dynamic-promo-scripting

**Rule logic as scripts, changeable without recompiling** — marketing
promotions whose eligibility conditions and discount actions live in
JavaScript files under `src/main/resources/promos/`:

```
promos/
  summer-sale/  condition.js  action.js
  first-order/  condition.js  action.js
  gold-tier/    condition.js  action.js
```

Edit a `.js` file, restart — no Java changes, no redeployment of rule code.

## What this sample shows

| Concept | Where |
|---|---|
| GraalJS auto-discovery (ServiceLoader) — zero setup | just the two GraalJS deps in `pom.xml` |
| `Script.builder().build("js", text)` | `PromoRunner.loadPromoRules()` |
| A scripted **Condition** (`Condition.builder().build(script)`) | each promo's `condition.js` |
| A scripted **Action** mutating bindings (`ctx.discountPercent = ...`) | each promo's `action.js` |
| A scripted **Function** returning a value | the final payable computation |
| Scripts calling Java methods (`ctx.order.getTotal()`) | every condition |
| Compile-time failure → `BuildScriptException` | the broken-syntax demo |
| Run-time failure → `EvaluationException` | the missing-method demo |

## The `ctx` variable

Inside a script, `ctx` **is** the rulii `Bindings`:

- read a binding: `ctx.order`, `ctx.orderMonth`
- write a binding: `ctx.discountPercent = ctx.discountPercent + 15`
- call Java methods on bound objects: `ctx.order.getTotal()`,
  `ctx.appliedPromos.add('...')` (host access is enabled by default)

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.promo.PromoRunner
```

## Things to notice

- **No registration code.** Putting `org.graalvm.js:js-scriptengine` and
  `org.graalvm.polyglot:js-community` on the classpath is enough — rulii's
  `ScriptProcessorManager` discovers `GraalJsScriptProcessorFactory`
  (language name `"js"`) via the ServiceLoader. The same mechanism picks up
  the Janino (Java-expression) processor, and rulii-spring adds SpEL.
- **Scripts are compiled once** and cached; a syntax error surfaces at
  `Script.builder().build(...)` time — before any order is processed.
- **Reading an undefined binding yields JS `undefined`**, not an error —
  guard optional inputs in the script, or bind explicit defaults like this
  sample does.
- Scripts run with full host access; treat rule scripts as **trusted code**
  (same trust level as the application itself), never as user input.

## Next step

[`checkout-rules-spring`](../checkout-rules-spring) goes fully declarative —
rules as Spring XML with SpEL expressions, no Java rule code at all.