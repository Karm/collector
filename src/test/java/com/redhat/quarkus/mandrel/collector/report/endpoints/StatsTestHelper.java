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

package com.redhat.quarkus.mandrel.collector.report.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;
import io.restassured.RestAssured;
import io.restassured.filter.cookie.CookieFilter;
import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

public class StatsTestHelper {

    public static final String BASE_URL = "/api/v1/image-stats";

    public static enum Mode {

        READ("r"),
        READ_WRITE("rw"),
        WRITE("w");

        private final String mode;

        private Mode(String mode) {
            this.mode = mode;
        }

        String getMode() {
            return mode;
        }
    }

    public static String login(Mode mode) {
        final CookieFilter cookies = new CookieFilter();
        // Login
        RestAssured.given().filter(cookies).contentType(ContentType.URLENC)
                .body("j_username=user&j_password=This is my password.").post("/j_security_check").then()
                .statusCode(HttpStatus.SC_OK);
        // Authenticated request, only 'user' user can create tokens for now.
        RestAssured.given().filter(cookies).when().get("/api/user/me").then().body(is("user"))
                .statusCode(HttpStatus.SC_OK);

        // Generate a new token
        final String token = RestAssured.given().filter(cookies).when().post("/api/tokens/create/" + mode.getMode())
                .then().statusCode(HttpStatus.SC_CREATED)
                .body("message", containsString("Save the token safely. This is the only time")).extract()
                .path("token");
        return token;
    }

    public static GraalStats parseV090Stat() {
        return parseJson(getV090StatString());
    }

    public static GraalStats parseV091Stat() {
        return parseJson(getV091StatString());
    }

    private static GraalStats parseJson(String raw) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(raw, GraalStats.class);
        } catch (JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    public static String getV090StatString() {
        return getStatString("build-stats-0.9.0.json");
    }

    public static String getV091StatString() {
        return getStatString("build-stats-0.9.1.json");
    }

    public static String getStatString(String name) {
        try (InputStream is = StatsTestHelper.class.getResourceAsStream("/" + name)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AssertionError("Error reading file: " + name, e);
        }
    }
}
