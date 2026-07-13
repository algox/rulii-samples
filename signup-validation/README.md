# signup-validation

**The rulii validation framework, end to end** — built-in validators, custom
validators, severities, error codes and violation reporting, on a domain
everyone knows: a user registration form.

## What this sample shows

| Concept | Where |
|---|---|
| Built-in validators via the `Validators` entry point | `SignupValidationRules` — `notNull`, `email`, `notBlank`, `size`, `alphaNumeric`, `min`, `in`, `pattern` |
| `binding("name")` — validate a named Binding, report against it | every validator |
| Overriding `errorCode` / `message` on a built-in | the age rules |
| `Severity.WARNING` — advisory violations that don't block | the under-18 rule |
| A reusable custom validator (the built-in pattern) | `PasswordStrengthRule` + `PasswordStrengthRuleBuilder` |
| A lightweight cross-field rule adding its own violation | `PasswordsMatchRule` |
| `.validating()` — auto `ruleViolations` + `ValidationException` | `SignupValidationRules` / `SignupValidationRunner` |
| Inspecting `RuleViolations` (errors vs warnings) | `SignupValidationRunner.validate()` |

## The two ways to write a custom validation

1. **`ValueValidationRule` subclass** (`PasswordStrengthRule`) — for reusable,
   single-value validators. Implement `isValid()` and `getSupportedTypes()`,
   add a companion builder, and it composes exactly like the 37 built-ins.
   Message parameter `{0}` is always the value; add more via
   `customizeViolation()`.
2. **Plain annotated rule** (`PasswordsMatchRule`) — for one-off or cross-field
   checks. The `@Otherwise` action adds a `RuleViolation` to the shared
   `ruleViolations` binding.

## Run it

```bash
mvn compile exec:java -Dexec.mainClass=org.rulii.sample.signup.SignupValidationRunner
```

Three requests are validated: a clean one, one with every field wrong
(→ `ValidationException` listing all 8 errors in one pass — validation does
not stop at the first failure), and a 16-year-old's signup that is accepted
with a WARNING.

## Things to notice

- **All violations are collected in one run** — the user sees every problem
  with the form at once, not one error per submit.
- **`WARNING` and `INFO` don't throw.** `.validating()` only raises
  `ValidationException` for `ERROR`/`FATAL` (`hasSevereErrors()`); bind your own
  `RuleViolations` to read the advisory ones after a successful run.
- **Null and type behavior**: most validators treat `null` as a failure
  (that's what `notNull` is for), and a value of an unsupported type causes the
  rule to be `SKIPPED` rather than fail.
- The 37 built-ins cover strings (`alpha`, `ascii`, `blank`, `lowerCase`,
  `startsWith`, …), numbers (`min`, `max`, `decimalMin`, `positive`, `digits`, …),
  dates (`past`, `future`, …), collections (`notEmpty`, `size`, `in`) and more —
  see `org.rulii.validation.rules.Validators`.

## Next step

[`claims-ruleflow`](../claims-ruleflow) introduces **RuleFlow** — rulii 2.0's
pipeline API for orchestrating rules, rulesets and validations into one flow.
