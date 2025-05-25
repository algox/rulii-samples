/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2025, Algorithmx Inc.
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
package org.rulii.sample.service.impl;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextOptions;
import org.rulii.ruleset.RuleSet;
import org.rulii.sample.model.Expense;
import org.rulii.sample.model.Income;
import org.rulii.sample.model.LoanApplication;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private RuleContextOptions ruleContextOptions;
    @Autowired
    private RuleSet<?> applicationRules;
    @Autowired
    private RuleSet<?> applicantRules;
    @Autowired
    private RuleSet<?> addressRules;
    @Autowired
    private RuleSet<?> vehicleRules;
    @Autowired
    private RuleSet<?> incomeRules;
    @Autowired
    private RuleSet<?> expenseRules;
    @Autowired
    private RuleSet<LoanDecision> loanRules;
    public LoanServiceImpl() {
        super();
    }

    @Override
    public LoanDecision submit(LoanApplication application) {
        validate(application);

        Bindings bindings = Bindings.builder().standard();
        bindings.loadProperties(application);
        bindings.bind("application", application);
        bindings.bind("decision", new LoanDecision());

        RuleContext context = RuleContext.builder()
                .with(ruleContextOptions)
                .bindings(bindings)
                .build();

        return loanRules.run(context);
    }

    private void validate(LoanApplication application) {
        runRules(application, applicationRules);
        runRules(application.getApplicant(), applicantRules);

        for (Income income : application.getApplicant().getIncomes()) {
            runRules(income, incomeRules);
        }

        for (Expense expense : application.getApplicant().getExpenses()) {
            runRules(expense, expenseRules);
        }

        runRules(application.getApplicant().getAddress(), addressRules);
        runRules(application.getVehicle(), vehicleRules);
    }

    private <T> T runRules(Object bean, RuleSet<T> rules) {
        Bindings bindings = Bindings.builder().standard();
        bindings.loadProperties(bean);

        RuleContext context = RuleContext.builder()
                .with(ruleContextOptions)
                .bindings(bindings)
                .build();

        return rules.run(context);
    }
}
