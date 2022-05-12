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
package com.redhat.quarkus.mandrel.collector.access.endpoints;

import com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository;
import com.redhat.quarkus.mandrel.collector.access.model.Token;
import com.redhat.quarkus.mandrel.collector.access.model.User;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Pattern;

@Path("/api/tokens")
public class TokenResource {

    public static final Pattern permPattern = Pattern.compile("\\b(r|w|rw|wr)\\b");
    public static final Pattern tokenPattern = Pattern.compile("[\\w-]+");

    @Inject
    TokenRepository tokenRepository;

    @ConfigProperty(name = "tokens.error.permission.match")
    String tokensErrorPermissionMatch;

    @ConfigProperty(name = "tokens.success.created")
    String tokensSuccessCreated;

    @ConfigProperty(name = "signup.error.token.match")
    String signupErrorTokenMatch;

    @ConfigProperty(name = "tokens.error.not.found")
    String tokensErrorNotFound;

    @ConfigProperty(name = "tokens.success.deleted")
    String tokensSuccessDeleted;

    @POST
    @RolesAllowed("user")
    @Path("/create/{permissions}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response create(@Context SecurityContext securityContext, @PathParam("permissions") String permissions)
            throws NoSuchAlgorithmException {
        if (permissions == null || !permPattern.matcher(permissions).matches()) {
            return Response.serverError().entity(String.format(tokensErrorPermissionMatch, permPattern)).build();
        }
        final String clearText = TokenRepository.randomStringHashed();
        final User user = User.find("username", securityContext.getUserPrincipal().getName()).firstResult();
        Token.add(user, permissions, clearText);
        return Response.status(Response.Status.CREATED).entity(String.format(tokensSuccessCreated, clearText)).build();
    }

    @DELETE
    @RolesAllowed("user")
    @Path("/delete/{token}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response delete(@Context SecurityContext securityContext, @PathParam("token") String token)
            throws NoSuchAlgorithmException {
        if (token == null || !tokenPattern.matcher(token).matches()) {
            return Response.serverError().entity(String.format(signupErrorTokenMatch, tokenPattern)).build();
        }
        final List<Token> tl = tokenRepository.findByUsernameTokenHash(securityContext.getUserPrincipal().getName(),
                TokenRepository.hash(token));
        if (tl == null || tl.isEmpty()) {
            return Response.serverError().entity(tokensErrorNotFound).build();
        }
        tl.forEach(Token::delete);
        return Response.status(Response.Status.ACCEPTED).entity(String.format(tokensSuccessDeleted, tl.size())).build();
    }

    @DELETE
    @RolesAllowed("user")
    @Path("/delete")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response delete(@Context SecurityContext securityContext) {
        final List<Token> tl = tokenRepository.findByUsername(securityContext.getUserPrincipal().getName());
        if (tl == null || tl.isEmpty()) {
            return Response.status(Response.Status.ACCEPTED).entity(String.format(tokensSuccessDeleted, 0)).build();
        }
        tl.forEach(Token::delete);
        return Response.status(Response.Status.ACCEPTED).entity(String.format(tokensSuccessDeleted, tl.size())).build();
    }

    @GET
    @RolesAllowed("user")
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response list(@Context SecurityContext securityContext) {
        final List<Token> tl = tokenRepository.findByUsername(securityContext.getUserPrincipal().getName());
        if (tl == null || tl.isEmpty()) {
            return Response.status(Response.Status.OK).entity("[]").build();
        }
        return Response.ok().entity(tl).build();
    }
}
