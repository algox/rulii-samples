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
package org.rulii.sample.web;

import org.rulii.sample.model.LoanApplication;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AutoLoanController {

    @Autowired
    private LoanService loanService;

    public AutoLoanController() {
        super();
    }

    @PostMapping(path = "/api/v1/loan", consumes = "application/json", produces = "application/json")
    public ResponseEntity<LoanDecision> submit(@RequestBody LoanApplication application) {
        LoanDecision decision = loanService.submit(application);
        return ResponseEntity.ok(decision);
    }
}
