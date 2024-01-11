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
package com.redhat.quarkus.mandrel.collector.access.auth;

import com.redhat.quarkus.mandrel.collector.access.model.Token;
import io.quarkus.security.AuthenticationFailedException;
import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.security.runtime.QuarkusPrincipal;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.interceptor.Interceptor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.FlushMode;

import java.util.HashSet;
import java.util.Set;

import static com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository.hash;

@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class TokenIdentityProvider implements IdentityProvider<TokenAuthenticationRequest> {

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Override
    public Class<TokenAuthenticationRequest> getRequestType() {
        return TokenAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(TokenAuthenticationRequest request,
            AuthenticationRequestContext context) {
        return context.runBlocking(() -> {
            final Token t;
            final EntityManager em = entityManagerFactory.createEntityManager();
            try (em) {
                ((org.hibernate.Session) em).setHibernateFlushMode(FlushMode.MANUAL);
                ((org.hibernate.Session) em).setDefaultReadOnly(true);

                t = em.createNamedQuery("findByHash", Token.class).setParameter(1, hash(request.getToken().getToken()))
                        .getResultStream().findFirst().orElse(null);

                /*
                 * It seems the caching doesn't work. e.g. the execution count corresponds to the number requests,
                 * although the hit count is always 0. It is a TODO. final Statistics stats =
                 * entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
                 * Log.infof("getQueryExecutionCount: %d", stats.getQueryExecutionCount());
                 * Log.infof("getQueryCacheHitCount: %d", stats.getQueryCacheHitCount());
                 * Log.infof("getSecondLevelCacheHitCount: %d", stats.getSecondLevelCacheHitCount());
                 * Log.infof("getSecondLevelCacheMissCount: %d", stats.getSecondLevelCacheMissCount());
                 */

            }
            if (t == null) {
                throw new AuthenticationFailedException("Token is invalid.");
            }
            final Set<String> roles = new HashSet<>(2);
            for (byte c : t.rw.getBytes()) {
                if (c == 'r') {
                    roles.add("token_read");
                }
                if (c == 'w') {
                    roles.add("token_write");
                }
            }
            return QuarkusSecurityIdentity.builder().setPrincipal(new QuarkusPrincipal("token")).addRoles(roles)
                    .addCredential(request.getToken()).build();
        });
    }
}
