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
package org.rulii.sample.signup;

/**
 * A user registration form. Each JavaBean property becomes a Binding
 * when loaded via bindings.loadProperties(request).
 */
public class SignupRequest {

    private final String email;
    private final String username;
    private final String password;
    private final String confirmPassword;
    private final Integer age;
    private final String country;
    private final String referralCode;

    public SignupRequest(String email, String username, String password, String confirmPassword,
                         Integer age, String country, String referralCode) {
        super();
        this.email = email;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.age = age;
        this.country = country;
        this.referralCode = referralCode;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public Integer getAge() {
        return age;
    }

    public String getCountry() {
        return country;
    }

    public String getReferralCode() {
        return referralCode;
    }
}
