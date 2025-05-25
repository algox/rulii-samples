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
package org.rulii.sample.model;

public class LoanDecision {

    public enum DECISION {APPROVED, DECLINED, PENDING}

    public DECISION decision = DECISION.PENDING;
    private double monthlyPayment;

    private double vehiclePrice;

    private String decisionReasonCode;

    public LoanDecision() {
        super();
    }

    public DECISION getDecision() {
        return decision;
    }

    public void setDecision(DECISION decision) {
        this.decision = decision;
    }

    public double getMonthlyPayment() {
        return monthlyPayment;
    }

    public void setMonthlyPayment(double monthlyPayment) {
        this.monthlyPayment = monthlyPayment;
    }

    public String getDecisionReasonCode() {
        return decisionReasonCode;
    }

    public void setDecisionReasonCode(String decisionReasonCode) {
        this.decisionReasonCode = decisionReasonCode;
    }

    public boolean isPending() {
        return decision == DECISION.PENDING;
    }

    public double getVehiclePrice() {
        return vehiclePrice;
    }

    public void setVehiclePrice(double vehiclePrice) {
        this.vehiclePrice = vehiclePrice;
    }

    @Override
    public String toString() {
        return "LoanDecision{" +
                "decision=" + decision +
                ", monthlyPayment=" + monthlyPayment +
                ", vehiclePrice=" + vehiclePrice +
                ", decisionReasonCode='" + decisionReasonCode + '\'' +
                '}';
    }
}
