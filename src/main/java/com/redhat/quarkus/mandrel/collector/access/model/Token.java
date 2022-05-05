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

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.json.bind.annotation.JsonbTransient;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.security.NoSuchAlgorithmException;

import static com.redhat.quarkus.mandrel.collector.access.TokenRepository.hash;

/**
 * Basic DB backed homemade token. We might switch to JWT later if needed.
 */
@Entity
@Table(name = "token", indexes = {
        @Index(columnList = "tokenHash")
})
public class Token extends PanacheEntity {

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    public User user;
    @Column(length = 2)
    public String rw;
    @Column(unique = true, length = 128)
    public String tokenHash;
    public Long lastUse;

    public static void add(User user, String rw, String clearTextToken) {
        final Token token = new Token();
        token.user = user;
        token.rw = rw;
        try {
            token.tokenHash = hash(clearTextToken);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot hash the token.", e);
        }
        token.lastUse = -1L;
        token.persist();
    }
}
