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
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;

import static com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository.hash;

/**
 * Basic DB backed homemade token. We might switch to JWT later if needed.
 */
@Entity(name = "token")
@Table(name = "token", indexes = {
        @Index(columnList = "tokenHash")
})
@NamedQuery(
        name = "findByHash",
        query = "SELECT t from token t WHERE t.tokenHash = ?1",
        // TODO: Test whether caching does what it seems to do.
        // So far, the DB is queried each time anyway :(
        hints = {
                @QueryHint(name = "org.hibernate.cacheable", value = "true"),
                @QueryHint(name = "org.hibernate.cacheMode", value = "GET"),
                @QueryHint(name = "org.hibernate.cacheRegion", value = "Token"),
                @QueryHint(name = "org.hibernate.readOnly", value = "true")
        }
)
public class Token extends PanacheEntity {

    @JsonbTransient
    @ManyToOne(fetch = FetchType.LAZY)
    public User user;
    @Column(length = 2)
    public String rw;
    @Column(unique = true, length = 128)
    public String tokenHash;
    // TODO: not used atm
    public Long lastUse;

    public static void add(User user, String rw, String clearTextToken) {
        final Token token = new Token();
        token.user = user;
        token.rw = rw;
        token.tokenHash = hash(clearTextToken);
        token.lastUse = -1L;
        token.persist();
    }
}
