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

import org.rulii.sample.service.PaymentCalculatorService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PaymentCalculatorServiceImpl implements PaymentCalculatorService {

    public PaymentCalculatorServiceImpl() {
        super();
    }

    @Override
    public double calculateMonthlyPayment(double vehiclePrice, double downPayment, int termInMonths) {
        BigDecimal result = BigDecimal.valueOf((vehiclePrice - downPayment) / termInMonths).setScale(2, RoundingMode.HALF_UP);
        return result.doubleValue();
    }
}
