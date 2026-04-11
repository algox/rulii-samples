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
import org.rulii.rule.Rule;
import org.rulii.ruleset.RuleSet;
import org.rulii.sample.model.Applicant;
import org.rulii.sample.model.DeclineReasons;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.model.Vehicle;
import org.rulii.sample.rules.*;
import org.rulii.sample.service.PaymentCalculatorService;
import org.rulii.sample.service.VehicleService;
import org.rulii.spring.annotation.RuleScan;
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

    @Bean
    public RuleSet<LoanDecision> loanRules(VehicleService vehicleService, PaymentCalculatorService paymentCalculatorService) {
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
                .rule(Rule.builder().build(MinimumAgeRule.class))
                .rule(Rule.builder().build(SpecialZipCodeRule.class))
                .rule(Rule.builder().build(UnsupportedStateRule.class))
                .rule(Rule.builder().build(ExpenseRatioCalculatingRule.class))
                .rule(Rule.builder().build(Expense50UpRatioRule.class))
                .rule(Rule.builder().build(Expense40UpRatioRule.class))
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
