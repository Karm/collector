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
package com.redhat.quarkus.mandrel.collector;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import java.util.regex.Pattern;

import static com.redhat.quarkus.mandrel.collector.TestUtil.parseLog;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test how humans sign-up and log-in.
 */
@QuarkusTest
public class AuthTest {

    @Test
    public void apiAccessibleSanity() {
        assertEquals(Response.Status.OK.getStatusCode(), given().when().get("/public/version").statusCode(),
                "API should be up and running.");
    }

    @Test
    public void adminLogin() {
        final CookieFilter cookies = new CookieFilter();
        // Login
        RestAssured.given()
                .filter(cookies)
                .contentType(ContentType.URLENC)
                .body("j_username=admin&j_password=This is my password.")
                .post("/j_security_check").then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY);
        // Authenticated request, authorized with the role admin
        RestAssured.given()
                .filter(cookies).when().get("/api/admin/me").then()
                .body(is("admin")).statusCode(HttpStatus.SC_OK);
    }

    @Test
    public void userSignUpOK() {
        // SignUp - username and email
        final String username = "test_user";
        RestAssured.given()
                .contentType(ContentType.URLENC)
                .body("username=" + username + "&email=me@example.com")
                .post("/public/signup").then()
                .body("message", containsString("User was successfully created."))
                .statusCode(HttpStatus.SC_CREATED);
        parseLog(Pattern.compile(".*Sending email Welcome to the Collector collective!.*"));
        parseLog(Pattern.compile(".*Use this one time token to set your password:.*"));

        // Set password using the token from email
        final String tokenFromEmail = parseLog(Pattern.compile(".*token=(?<token>[\\w-]+).*")).group("token");
        final String newPassword = "This is my password.";
        RestAssured.given()
                .contentType(ContentType.URLENC)
                .body("password=" + newPassword + "&token=" + tokenFromEmail)
                .post("/public/changepasswd").then()
                .body("message", containsString("Your password was successfully changed."))
                .statusCode(HttpStatus.SC_CREATED);

        // Login with our new username and password
        final CookieFilter cookies = new CookieFilter();
        RestAssured.given()
                .filter(cookies)
                .contentType(ContentType.URLENC)
                .body("j_username=" + username + "&j_password=" + newPassword)
                .post("/j_security_check").then().statusCode(HttpStatus.SC_MOVED_TEMPORARILY);

        // Try to do something only a logged-in user can do
        RestAssured.given()
                .filter(cookies).when().get("/api/user/me").then()
                .body(is(username)).statusCode(HttpStatus.SC_OK);
    }
}
