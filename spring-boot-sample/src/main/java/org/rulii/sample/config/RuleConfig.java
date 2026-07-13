/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.sample.config;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.model.action.Action;
import org.rulii.rule.Rule;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleset.RuleSet;
import org.rulii.sample.model.Applicant;
import org.rulii.sample.model.DeclineReasons;
import org.rulii.sample.model.Expense;
import org.rulii.sample.model.Income;
import org.rulii.sample.model.LoanApplication;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.model.Vehicle;
import org.rulii.sample.rules.*;
import org.rulii.sample.service.PaymentCalculatorService;
import org.rulii.sample.service.VehicleService;
import org.rulii.spring.annotation.RuleScan;
import org.rulii.util.reflect.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

@Configuration
@RuleScan(
        scanBasePackages = "org.rulii.sample.rules",
        xmlLocations     = "classpath:rules/"
)
public class RuleConfig {

    public RuleConfig() {
        super();
    }

    /**
     * The whole loan process as one RuleFlow (new in rulii 2.0). This replaces
     * the hand-written orchestration that used to live in LoanServiceImpl -
     * six manual ruleset runs, each with its own Bindings and RuleContext.
     *
     * Each validation section runs in its own scope: the section's properties
     * are exposed as flat bindings for the XML validators, and disappear when
     * the scope ends (so "type" from Income never clashes with "type" from
     * Expense).
     */
    @Bean
    public RuleFlow<LoanDecision> loanDecisionFlow(RuleSet<?> applicationRules,
                                                   RuleSet<?> applicantRules,
                                                   RuleSet<?> addressRules,
                                                   RuleSet<?> vehicleRules,
                                                   RuleSet<?> incomeRules,
                                                   RuleSet<?> expenseRules,
                                                   RuleSet<LoanDecision> loanRules) {
        return RuleFlow.builder()
                .name("loanDecisionFlow")
                .description("Validates a loan application and produces a decision.")
                .param("application", LoanApplication.class, true)

                // ---- Shape validation (the XML rulesets throw ValidationException on failure)
                .scope(body -> body
                        .execute(loadProperties(application -> application))
                        .run(applicationRules))
                .scope(body -> body
                        .execute(loadProperties(LoanApplication::getApplicant))
                        .run(applicantRules))
                .forEach(function((LoanApplication application) -> application.getApplicant().getIncomes()), "income",
                        body -> body.scope(inner -> inner
                                .execute(action((RuleContext ruleContext, Income income) ->
                                        ruleContext.getBindings().loadProperties(income)))
                                .run(incomeRules)))
                .forEach(function((LoanApplication application) -> application.getApplicant().getExpenses()), "expense",
                        body -> body.scope(inner -> inner
                                .execute(action((RuleContext ruleContext, Expense expense) ->
                                        ruleContext.getBindings().loadProperties(expense)))
                                .run(expenseRules)))
                .scope(body -> body
                        .execute(loadProperties(application -> application.getApplicant().getAddress()))
                        .run(addressRules))
                .scope(body -> body
                        .execute(loadProperties(LoanApplication::getVehicle))
                        .run(vehicleRules))

                // ---- Decision. A fresh LoanDecision per run (apply is evaluated
                // per execution; a builder-time bind would be a constant).
                .apply(function(() -> new LoanDecision()), spec -> spec.as("decision"))
                .execute(loadProperties(application -> application))
                .run(loanRules)

                .<LoanDecision>returning(function((LoanDecision decision) -> decision))
                .build();
    }

    /**
     * Exposes a section of the application as flat bindings in the current
     * scope - the binding names the XML validators reference.
     */
    private static Action loadProperties(java.util.function.Function<LoanApplication, Object> section) {
        return action((RuleContext ruleContext, LoanApplication application) ->
                ruleContext.getBindings().loadProperties(section.apply(application)));
    }

    @Bean
    public RuleSet<LoanDecision> loanRules(VehicleService vehicleService, PaymentCalculatorService paymentCalculatorService,
                                           ObjectFactory objectFactory) {
        // The Spring ObjectFactory creates the class-based rules through the
        // ApplicationContext, so @Value / @Autowired inside them work.
        return RuleSet.builder().with("loanRules")
                .param("applicant", Applicant.class)
                .param("vehicle", Vehicle.class)
                .param("downPayment", Double.class)
                .param("termInMonths", Integer.class)
                .stopCondition(condition((LoanDecision decision) -> !decision.isPending()))
                .rule(Rule.builder()
                        .name("vehiclePriceCalculatingRule")
                        .then(action((RuleContext ruleContext, Vehicle vehicle) -> ruleContext.getBindings()
                                .bind("vehiclePrice", vehicleService.getVehiclePrice(vehicle))))
                        .build())
                .rule(Rule.builder()
                        .name("paymentCalculatingRule")
                        .then(action((Bindings bindings, Double vehiclePrice, Double downPayment, Integer termInMonths) ->
                                bindings.bind("monthlyPayment", paymentCalculatorService.calculateMonthlyPayment(vehiclePrice, downPayment, termInMonths))))
                        .build())
                .rule(Rule.builder()
                        .name("downPaymentCheckRule")
                        .given(condition((Double vehiclePrice, Double downPayment) -> downPayment < vehiclePrice))
                        .otherwise(action((LoanDecision decision) -> {
                            decision.setDecision(LoanDecision.DECISION.DECLINED);
                            decision.setDecisionReasonCode(DeclineReasons.DOWN_PAYMENT_MORE_THAN_VEHICLE_PRICE);
                        }))
                        .build())
                .rule(Rule.builder().build(MinimumAgeRule.class, objectFactory))
                .rule(Rule.builder().build(SpecialZipCodeRule.class, objectFactory))
                .rule(Rule.builder().build(UnsupportedStateRule.class, objectFactory))
                .rule(Rule.builder().build(ExpenseRatioCalculatingRule.class, objectFactory))
                .rule(Rule.builder().build(Expense50UpRatioRule.class, objectFactory))
                .rule(Rule.builder().build(Expense40UpRatioRule.class, objectFactory))
                .rule(Rule.builder()
                        .name("defaultApproveRule")
                        .then(action((LoanDecision decision) -> decision.setDecision(LoanDecision.DECISION.APPROVED)))
                        .build())
                .finalizer(action((LoanDecision decision, Double vehiclePrice, Double monthlyPayment) -> {
                    decision.setVehiclePrice(vehiclePrice);
                    decision.setMonthlyPayment(monthlyPayment);
                }))
                .resultExtractor(function((LoanDecision decision) -> decision))
                .build();
    }
}
