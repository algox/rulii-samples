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
package org.rulii.sample.service.impl;

import org.rulii.bind.Bindings;
import org.rulii.context.RuleContext;
import org.rulii.context.RuleContextOptions;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.sample.model.LoanApplication;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Before rulii 2.0 this class hand-orchestrated the process: six separate
 * ruleset runs, each with its own Bindings and RuleContext, plus the loops
 * over incomes and expenses. All of that now lives in the loanDecisionFlow
 * (see RuleConfig) - the service just runs it.
 */
@Service
public class LoanServiceImpl implements LoanService {

    @Autowired
    private RuleContextOptions ruleContextOptions;
    @Autowired
    private RuleFlow<LoanDecision> loanDecisionFlow;

    public LoanServiceImpl() {
        super();
    }

    @Override
    public LoanDecision submit(LoanApplication application) {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind("application", application);

        RuleContext context = RuleContext.builder()
                .with(ruleContextOptions)
                .bindings(bindings)
                .build();

        return loanDecisionFlow.run(context);
    }
}
