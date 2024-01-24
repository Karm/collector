/*
 * Copyright (c) 2022 Contributors to the Collector project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.redhat.quarkus.mandrel.collector.access.model;

import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.security.jpa.Password;
import io.quarkus.security.jpa.Roles;
import io.quarkus.security.jpa.UserDefinition;
import io.quarkus.security.jpa.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import static com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository.hash;

@Entity
@Table(name = "user")
@UserDefinition
public class User extends PanacheEntity {
    @Username
    @Column(unique = true, length = 12)
    public String username;
    @Password
    public String password;
    @Roles
    public String role;
    @Column(unique = true)
    public String email;
    public long lastLogin;
    // One time. Cleartext sent to email.
    @Column(unique = true, length = 128)
    public String changePasswordTokenHash;

    public static void add(String username, String password, String changePasswordToken, String role, String email) {
        final User user = new User();
        user.username = username;
        user.password = BcryptUtil.bcryptHash(password);
        user.role = role;
        user.email = email;
        user.lastLogin = -1L;
        user.changePasswordTokenHash = hash(changePasswordToken);
        user.persist();
    }
}
