# order-fulfillment-flow-spring

**A complete RuleFlow declared in XML, running on Spring** — an order
fulfillment workflow with parallel external calls, exception routing, and
full dependency injection into rules:

```
stock check -> (backordered?) -> insurance review
            -> charge payment  \
            -> shipping label  / in parallel -> delivery estimate -> SHIPPED
```

## What this sample shows

| Concept | Where |
|---|---|
| `<r:ruleflow>` — the full flow DSL in XML | `rules/fulfillment-flow.xml` |
| `run name="..."` — RuleRegistry lookup at execution time | `inventoryCheckRule`, `deliveryEstimateRule` |
| `run bean-ref="..."` — Spring bean resolved at wiring time | `highValueReviewRule` |
| `async-run` + `await-all` (IMMUTABLE mode sub-flows) | payment + shipping label |
| Step-level and global `on-exception` | declined card / inventory outage |
| `when` / `exit` early-exit paths | backorder, payment failure |
| Combined `@RuleScan` (class scan **and** XML locations) | `AppRuleConfig` |
| **DI into rules** via SpringObjectFactory (`@Autowired`, `@Value`) | `InventoryCheckRule`, `DeliveryEstimateRule` |
| Overriding `rulii.executorService` **by name** | `AppRuleConfig` (watch the `fulfillment-N` threads) |
| Listener beans auto-registered on the Tracer | `FlowAuditListener` (`@Component`, zero wiring) |
| Flows as registry citizens: `ruleRegistry.getRuleFlow(...)` | `FulfillmentService` |
| Deterministic tests with a fixed `Clock` bean | `FulfillmentFlowTest` |

## Run it

```bash
mvn spring-boot:run   # four orders: shipped / backordered / declined / manual review
mvn test              # @SpringBootTest with a fixed Clock
```

## Things to notice

- **Exporting results from a flow.** A flow's own bindings vanish with its
  scope, and the XML `<r:exit/>` has no result-extractor form — so the flow
  reports through bindings the caller pre-binds (`status`, `paymentRef`,
  `trackingNumber`, ...). `<r:returning>` covers the normal path.
- **Service exceptions inside SpEL surface as
  `org.rulii.script.EvaluationException`** — that's the type a step's
  `on-exception` must name in an XML flow. (In Java-built flows you'd match
  your own `UnrulyException` subclass directly — see `claims-ruleflow`.)
- **Calling services from expressions**: the caller binds the service beans
  (`bindings.bind("paymentService", paymentService)`) and scripts invoke them
  (`#ctx.paymentService.charge(...)`). Note that `<r:bind ref="bean"/>` does
  something different — it loads the bean's *properties* as bindings.
- **The executor override is by NAME**: the bean must be called
  `rulii.executorService`; any other `ExecutorService` bean is deliberately
  ignored by the auto-configuration.
- **Testability**: the fixed `Clock` bean is honored both by rulii's
  `ruleContextOptions` and by the `@Autowired Clock` in
  `DeliveryEstimateRule` — the delivery date in the test is exact.

## Next step

The [`spring-boot-sample`](../spring-boot-sample) (auto loan) is the capstone:
a REST service combining XML validation rulesets, Java rulesets and a
RuleFlow orchestrating the whole decision.