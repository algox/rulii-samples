# spring-boot-sample (Auto Loan)

**The capstone: a REST service where XML validation rulesets, a Java RuleSet
and a RuleFlow work together** to decide an auto-loan application.

```
POST /autoloan/api/v1/loan
        |
   loanDecisionFlow (RuleFlow)
        |- applicationRules   (XML validating ruleset)
        |- applicantRules     (XML)
        |- forEach income  -> incomeRules  (XML)
        |- forEach expense -> expenseRules (XML)
        |- addressRules, vehicleRules (XML)
        |- loanRules (Java RuleSet: pricing, payment, age, state,
        |             expense-ratio and approval rules)
        '- returning LoanDecision
```

## The rulii 2.0 upgrade story

Before 2.0, `LoanServiceImpl` hand-orchestrated the process: six separate
ruleset runs, each with its own `Bindings` and `RuleContext`, plus manual
loops over incomes and expenses. That entire method is now one declarative
`RuleFlow` bean (`RuleConfig.loanDecisionFlow`) and the service shrank to
"bind the application, run the flow".

Other 2.0-era touches worth studying:

- **Scopes prevent name clashes**: each validation section runs in its own
  flow scope, where the section's properties are exposed as flat bindings —
  `Income.type` and `Expense.type` never collide.
- **`SpringObjectFactory` for class-based rules**:
  `Rule.builder().build(MinimumAgeRule.class, objectFactory)` creates rules
  through the ApplicationContext, so `@Value("${applicant.min.age}")` and
  `@Autowired` inside them actually work. (Built with the default factory,
  the injections are silently skipped — the minimum-age rule used to compare
  against 0!)
- **Configured violation messages**: the XML validators now carry
  `errorMessage="${loan.error.NNN}"`, resolving the messages that were
  already sitting in `application.properties`.
- A fresh `LoanDecision` per run via `.apply(function(() -> new LoanDecision()),
  spec -> spec.as("decision"))` — a builder-time `bind` would be evaluated
  once and shared across requests.

## Run it

```bash
mvn spring-boot:run
```

```bash
curl -X POST http://localhost:8080/autoloan/api/v1/loan \
  -H "Content-Type: application/json" \
  -d '{
        "applicant": {
          "firstName": "Kaia", "lastName": "Rivers",
          "ssn": "111-22-3333", "phoneNumber": "404-555-0100",
          "dateOfBirth": "14-03-1996",
          "address": {"streetNumber": "125", "streetName": "Peach Tree St",
                      "city": "Atlanta", "state": "GA", "zipcode": "30301"},
          "incomes":  [{"type": "SALARY", "monthlyAmount": 20000}],
          "expenses": [{"type": "RENT",   "monthlyAmount": 500}]
        },
        "vehicle": {"make": "Honda", "model": "Accord", "year": 2024},
        "downPayment": 5000, "termInMonths": 48
      }'
```

Or run the tests:

```bash
mvn test    # approval, underage decline, unsupported state, validation messages
```

## Where to look

| Piece | File |
|---|---|
| The RuleFlow | `config/RuleConfig.java` (`loanDecisionFlow`) |
| The decision RuleSet (Java) | `config/RuleConfig.java` (`loanRules`) |
| XML validation rulesets | `resources/rules/validation-rules.xml` |
| Class-based rules with DI | `rules/MinimumAgeRule.java` etc. |
| The (thin) service | `service/impl/LoanServiceImpl.java` |
| End-to-end tests | `test/.../LoanDecisionFlowTests.java` |
