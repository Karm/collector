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

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.security.credential.TokenCredential;
import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.AuthenticationRequest;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.interceptor.Interceptor;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.redhat.quarkus.mandrel.collector.access.endpoints.TokenResource.tokenPattern;

@Priority(Interceptor.Priority.APPLICATION)
@ApplicationScoped
public class TokenAuthMechanism implements HttpAuthenticationMechanism {

    public static final String TOKEN_HEADER = "token";
    public static final String TOKEN_TYPE = "API_TOKEN";

    @Override
    public Uni<SecurityIdentity> authenticate(RoutingContext context, IdentityProviderManager identityProviderManager) {
        final String token = context.request().headers().get(TOKEN_HEADER);
        if (token == null || !tokenPattern.matcher(token).matches()) {
            // No valid token header, no token auth...
            return Uni.createFrom().optional(Optional.empty());
        }
        return identityProviderManager
                .authenticate(new TokenAuthenticationRequest(new TokenCredential(token, TOKEN_TYPE)));
    }

    @Override
    public Uni<ChallengeData> getChallenge(RoutingContext context) {
        return Uni.createFrom().item(new ChallengeData(HttpResponseStatus.UNAUTHORIZED.code(), null, null));
    }

    @Override
    public Set<Class<? extends AuthenticationRequest>> getCredentialTypes() {
        return Collections.singleton(TokenAuthenticationRequest.class);
    }
}
