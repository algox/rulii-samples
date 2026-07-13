# checkout-rules-spring

**Business rules as configuration** — a Spring Boot checkout service where
every rule is declared in XML with SpEL expressions. There is no Java rule
code: `CheckoutService` binds the order and runs two XML-declared rulesets;
everything else lives in `src/main/resources/rules/`.

## What this sample shows

| Concept | Where |
|---|---|
| `@RuleScan(xmlLocations = "classpath:rules/")` | `AppRuleConfig` — the entire setup |
| `<r:ruleset>` with predefined validator elements | `orderValidationRules` |
| The **terse attribute form** (`given=".." then=".."` one-liners) | `freeShippingRule`, `expeditedShippingRule` |
| The element form (multiple `<r:then>` actions) | `internationalSurchargeRule` |
| **`${property:default}` placeholders in rule expressions** | every threshold and fee |
| Violation messages from configuration (`errorMessage="${...}"`, `{0}` = value) | the validators + `application.yaml` |
| `initializer` as an attribute | `shippingRules` |
| **Profile-gated rules** (`<beans profile="holiday">`) | `seasonal-rules.xml` |
| `RuleRegistry` lookups with the 2.0 null-return contract | `CheckoutService` |
| XML rulesets injected as Spring beans by name | `@Autowired RuleSet<?> shippingRules` |

## Run it

```bash
mvn spring-boot:run
# and again with holiday pricing switched on:
mvn spring-boot:run -Dspring-boot.run.profiles=holiday
```

Three orders run: a US order above the free-shipping threshold ($0.00), a
small expedited order to Germany (base + surcharge + expedited fee = $27.49;
$14.99 under the holiday profile), and an invalid order that is rejected with
the configured messages.

## The declarative stack

```yaml
# application.yaml — the business people's file
shipping:
  free-threshold: 75
  expedited-fee: 12.50
```

```xml
<!-- rules/checkout-rules.xml — the rule author's file -->
<r:rule name="freeShippingRule"
        given="#ctx.total >= ${shipping.free-threshold:75}"
        then="#ctx.shippingCost = 0.0"/>
```

- `#ctx.<name>` reads/writes the Binding with that name; property navigation
  (`#ctx.order.total`) and method calls work too — expressions are SpEL.
- `${...}` placeholders resolve **once at startup** against the Spring
  environment, with `@Value` semantics: `:default` fallbacks, fail-fast on a
  missing key. Change `application.yaml`, restart, done.
- SpEL expressions run with full power — treat rule files as **trusted code**,
  like any Spring XML.

## Things to notice

- **Messages are configuration too**: `errorMessage="${checkout.messages...}"`
  pulls the violation text from `application.yaml`; `{0}` is replaced with the
  offending value. (For locale-aware bundles, rulii-spring's `MessageResolver`
  also consults Spring's `MessageSource` for rules that don't declare an
  explicit message.)
- **Profiles gate rules like any bean.** The holiday rule simply doesn't exist
  outside the `holiday` profile — `ruleRegistry.getRule("holidaySeasonRule")`
  returns `null` (2.0 contract) and the service skips it.
- The XSD is bundled with rulii-spring (`spring.schemas`), so XML validation
  works offline — the IDE gets autocomplete from the same schema.

## Next step

[`order-fulfillment-flow-spring`](../order-fulfillment-flow-spring) declares a
whole **RuleFlow** in XML — async steps, exception handling and registry
lookups included.