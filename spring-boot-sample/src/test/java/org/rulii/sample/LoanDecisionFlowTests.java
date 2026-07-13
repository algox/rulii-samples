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
package org.rulii.sample;

import org.junit.jupiter.api.Test;
import org.rulii.sample.model.Address;
import org.rulii.sample.model.Applicant;
import org.rulii.sample.model.Expense;
import org.rulii.sample.model.Income;
import org.rulii.sample.model.LoanApplication;
import org.rulii.sample.model.LoanDecision;
import org.rulii.sample.model.Vehicle;
import org.rulii.sample.service.LoanService;
import org.rulii.validation.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LoanDecisionFlowTests {

    @Autowired
    private LoanService loanService;

    @Test
    void healthyApplicationIsApproved() {
        LoanDecision decision = loanService.submit(application(30, Address.STATE.GA, 20_000.00));

        assertEquals(LoanDecision.DECISION.APPROVED, decision.getDecision());
        assertTrue(decision.getMonthlyPayment() > 0);
    }

    @Test
    void underageApplicantIsDeclined() {
        LoanDecision decision = loanService.submit(application(16, Address.STATE.GA, 20_000.00));

        assertEquals(LoanDecision.DECISION.DECLINED, decision.getDecision());
    }

    @Test
    void unsupportedStateIsDeclined() {
        LoanDecision decision = loanService.submit(application(30, Address.STATE.AK, 20_000.00));

        assertEquals(LoanDecision.DECISION.DECLINED, decision.getDecision());
    }

    @Test
    void malformedApplicationFailsValidationWithConfiguredMessages() {
        LoanApplication application = application(30, Address.STATE.GA, 20_000.00);
        application.getApplicant().setFirstName(null);
        application.setTermInMonths(6);

        ValidationException e = assertThrows(ValidationException.class,
                () -> loanService.submit(application));

        // The messages come from application.properties (loan.error.*)
        assertTrue(e.getViolations().getViolations().stream()
                .anyMatch(v -> "Term must be at least 12 months.".equals(v.getErrorMessage())));
    }

    private static LoanApplication application(int age, Address.STATE state, double monthlyIncome) {
        Address address = new Address();
        address.setStreetNumber("125");
        address.setStreetName("Peach Tree St");
        address.setCity("Atlanta");
        address.setState(state);
        address.setZipcode("30301");

        Income income = new Income();
        income.setType(Income.Type.SALARY);
        income.setMonthlyAmount(monthlyIncome);

        Expense expense = new Expense();
        expense.setType(Expense.Type.RENT);
        expense.setMonthlyAmount(500.00);

        Applicant applicant = new Applicant();
        applicant.setFirstName("Kaia");
        applicant.setLastName("Rivers");
        applicant.setSsn("111-22-3333");
        applicant.setPhoneNumber("404-555-0100");
        applicant.setDateOfBirth(LocalDate.now().minusYears(age).minusDays(1));
        applicant.setAddress(address);
        applicant.setIncomes(List.of(income));
        applicant.setExpenses(List.of(expense));

        Vehicle vehicle = new Vehicle();
        vehicle.setMake("Honda");
        vehicle.setModel("Accord");
        vehicle.setYear(2024);

        LoanApplication application = new LoanApplication();
        application.setApplicant(applicant);
        application.setVehicle(vehicle);
        application.setDownPayment(5_000.00);
        application.setTermInMonths(48);
        return application;
    }
}
