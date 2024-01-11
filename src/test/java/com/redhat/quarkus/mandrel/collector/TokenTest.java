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
import jakarta.ws.rs.core.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test how robots use the auth API.
 */
@QuarkusTest
public class TokenTest {

    @Test
    public void apiAccessibleSanity() {
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), given().when().get("/api/report/test").statusCode(),
                "API should be protected.");
    }

    @Test
    public void readOnlyToken() {
        final CookieFilter cookies = new CookieFilter();
        // Login
        RestAssured.given().filter(cookies).contentType(ContentType.URLENC)
                .body("j_username=user&j_password=This is my password.").post("/j_security_check").then()
                .statusCode(HttpStatus.SC_OK);

        // Generate a new read-only token
        final String token = RestAssured.given().filter(cookies).when().post("/api/tokens/create/r").then()
                .statusCode(HttpStatus.SC_CREATED)
                .body("message", containsString("Save the token safely. This is the only time")).extract()
                .path("token");

        // Use the token to read. Note there are no cookies.
        RestAssured.given().accept(ContentType.JSON).header("token", token).get("/api/report/test").then()
                .contentType(ContentType.JSON).statusCode(HttpStatus.SC_OK).body(containsString("test"));

        // Use the read-only token to write something
        RestAssured.given().accept(ContentType.JSON).contentType(ContentType.JSON).header("token", token)
                .body("{\"something\":\"hahaha\",\"data\":42}").post("/api/report/test").then()
                .statusCode(HttpStatus.SC_FORBIDDEN);

        // Revoke (delete) the token
        RestAssured.given().filter(cookies).when().delete("/api/tokens/delete/" + token).then()
                .statusCode(HttpStatus.SC_ACCEPTED).body("message", is("Deleted tokens: 1"));

        // See that token doesn't work. Note: When caching finally works, this test should IMHO fail...
        RestAssured.given().accept(ContentType.JSON).header("token", token).get("/api/report/test").then()
                .statusCode(HttpStatus.SC_UNAUTHORIZED);
    }
}
