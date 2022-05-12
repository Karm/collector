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

import com.redhat.quarkus.mandrel.collector.access.model.User;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.reactive.ReactiveMailer;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;

import static com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository.hash;
import static com.redhat.quarkus.mandrel.collector.access.auth.TokenRepository.randomStringHashed;
import static com.redhat.quarkus.mandrel.collector.access.endpoints.TokenResource.tokenPattern;

/**
 * TODO: User last login filed is not populated....
 */
@Path("/public")
public class PublicResource {

    public static final Pattern usernamePattern = Pattern.compile("[\\w-_]{3,12}");
    public static final Pattern passwordPattern = Pattern.compile("[\\p{L}\\w \\Q:!<#=$>%?&@[(\\)]*^+_,`-{.|/}~\\E]{12,512}");
    // https://owasp.org/www-community/OWASP_Validation_Regex_Repository
    public static final Pattern emailPattern = Pattern
            .compile(
                    "^[a-zA-Z0-9_+&*-]{1,35}(?:\\.[a-zA-Z0-9_+&*-]{1,35}){0,35}@(?:[a-zA-Z0-9-]{1,35}\\.){1,10}[a-zA-Z]{2,7}$");

    @Inject
    ReactiveMailer mailer;

    @ConfigProperty(name = "signup.email.message.body")
    String signupEmailMessageBody;

    @ConfigProperty(name = "signup.email.message.subject")
    String signupEmailMessageSubject;

    @ConfigProperty(name = "signup.email.contact")
    String signupEmailContact;

    @ConfigProperty(name = "signup.error.username.exists")
    String signupErrorUsernameExists;

    @ConfigProperty(name = "signup.error.email.exists")
    String signupErrorEmailExists;

    @ConfigProperty(name = "signup.error.email.match")
    String signupErrorEmailMatch;

    @ConfigProperty(name = "signup.error.username.match")
    String signupErrorUsernameMatch;

    @ConfigProperty(name = "signup.error.token.match")
    String signupErrorTokenMatch;

    @ConfigProperty(name = "signup.error.password.match")
    String signupErrorPasswordMatch;

    @ConfigProperty(name = "signup.error.token.not.found")
    String signupErrorTokenNotFound;

    @ConfigProperty(name = "signup.success.user.created")
    String signupSuccessUserCreated;

    @ConfigProperty(name = "signup.success.password.changed")
    String signupSuccessPasswordChanged;

    @ConfigProperty(name = "signup.error.user.not.found")
    String signupErrorUserNotFound;

    @ConfigProperty(name = "forgot.passwd.email.message.subject")
    String forgotPasswdEmailMessageSubject;

    @ConfigProperty(name = "forgot.passwd.success")
    String forgotPasswdSuccess;

    @ConfigProperty(name = "url")
    String url;

    @POST
    @Path("/signup")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response signup(@FormParam("username") String username, @FormParam("email") String email)
            throws NoSuchAlgorithmException {
        if (username == null || !usernamePattern.matcher(username).matches()) {
            return Response.serverError().entity(String.format(signupErrorUsernameMatch, usernamePattern)).build();
        }
        if (email == null || !emailPattern.matcher(email).matches()) {
            return Response.serverError().entity(String.format(signupErrorEmailMatch, emailPattern)).build();
        }
        if (User.count("username", username) != 0) {
            return Response.serverError().entity(signupErrorUsernameExists).build();
        }
        if (User.count("email", email) != 0) {
            return Response.serverError().entity(signupErrorEmailExists).build();
        }
        final String changePasswordToken = randomStringHashed();
        User.add(username, randomStringHashed(), changePasswordToken, "user", email);
        // Sending email might fail (SMTP down etc.), the Exception thrown then rolls back the transaction of adding User.
        mailer.send(Mail.withText(email, signupEmailMessageSubject,
                        String.format(signupEmailMessageBody, username, changePasswordToken, url)))
                .await().atMost(Duration.ofSeconds(30));
        return Response.status(Response.Status.CREATED).entity(
                String.format(signupSuccessUserCreated, signupEmailContact)).build();
    }

    @POST
    @Path("/changepasswd")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response changepasswd(
            @FormParam("token") String token,
            @FormParam("password") String password) throws NoSuchAlgorithmException {
        if (token == null || !tokenPattern.matcher(token).matches()) {
            return Response.serverError().entity(String.format(signupErrorTokenMatch, tokenPattern)).build();
        }
        if (password == null || !passwordPattern.matcher(password).matches()) {
            return Response.serverError().entity(String.format(signupErrorPasswordMatch, passwordPattern)).build();
        }
        final User user = User.find("changePasswordTokenHash", hash(token)).firstResult();
        if (user == null) {
            return Response.serverError().entity(signupErrorTokenNotFound).build();
        }
        user.changePasswordTokenHash = null;
        user.password = BcryptUtil.bcryptHash(password);
        user.persist();
        return Response.status(Response.Status.CREATED).entity(
                String.format(signupSuccessPasswordChanged, url)).build();
    }

    @POST
    @Path("/forgotpasswd")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPasswd(@FormParam("username") String username, @FormParam("email") String email)
            throws NoSuchAlgorithmException {
        if (username == null || !usernamePattern.matcher(username).matches()) {
            return Response.serverError().entity(String.format(signupErrorUsernameMatch, usernamePattern)).build();
        }
        if (email == null || !emailPattern.matcher(email).matches()) {
            return Response.serverError().entity(String.format(signupErrorEmailMatch, emailPattern)).build();
        }
        final User user = User.find("username = ?1 and email = ?2", username, email).firstResult();
        if (user == null) {
            return Response.serverError().entity(signupErrorUserNotFound).build();
        }
        final String changePasswordToken = randomStringHashed();
        user.changePasswordTokenHash = hash(changePasswordToken);
        user.persist();
        mailer.send(Mail.withText(email, forgotPasswdEmailMessageSubject,
                        String.format(signupEmailMessageBody, username, changePasswordToken, url)))
                .await().atMost(Duration.ofSeconds(10));
        return Response.status(Response.Status.CREATED).entity(
                String.format(forgotPasswdSuccess, signupEmailContact)).build();
    }
}
