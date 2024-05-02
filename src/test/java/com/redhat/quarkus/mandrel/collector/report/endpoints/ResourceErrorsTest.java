/*
 * Copyright (c) 2024 Contributors to the Collector project
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

import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper.Mode;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@Order(Order.DEFAULT + 999)
public class ResourceErrorsTest {

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void testImportErrors() {
        final String token = StatsTestHelper.login(Mode.READ_WRITE);

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .body(StatsTestHelper.getStatString("23.1/quarkus-awt.json"))
                .when()
                .post(StatsTestHelper.BASE_URL + "/import?t=mehTagA&runnerid=blabla")
                .then()
                .statusCode(404)
                .body("exceptionType", containsString("NotFoundException"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .body(StatsTestHelper.getStatString("23.1/quarkus-awt.json"))
                .when()
                .post(StatsTestHelper.BASE_URL + "/import?t=mehTagA&runnerid=55555555555")
                .then()
                .statusCode(404)
                .body("error", containsString("RunnerInfo with ID 55555555555 not found."));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .post(StatsTestHelper.BASE_URL)
                .then()
                .statusCode(400)
                .body("error", containsString("ImageStats must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .post(StatsTestHelper.BASE_URL + "/import")
                .then()
                .statusCode(400)
                .body("error", containsString("GraalStats must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .put(StatsTestHelper.BASE_URL + "/666")
                .then()
                .statusCode(400)
                .body("error", containsString("GraalBuildInfo for statId 666 must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .body(StatsTestHelper.getStatString("23.1/quarkus-awt.json"))
                .when()
                .put(StatsTestHelper.BASE_URL + "/666")
                .then()
                .statusCode(404)
                .body("error", containsString("Stat with id 666 not found"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .post(StatsTestHelper.BASE_URL + "/update-runner-info/666")
                .then()
                .statusCode(400)
                .body("error", containsString("RunnerInfo for statId 666 must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .body(StatsTestHelper.getStatString("23.1/quarkus-runner.json"))
                .when()
                .post(StatsTestHelper.BASE_URL + "/update-runner-info/666")
                .then()
                .statusCode(404)
                .body("error", containsString("Stat with id 666 not found"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .post(StatsTestHelper.BASE_URL + "/runner-info")
                .then()
                .statusCode(400)
                .body("error", containsString("RunnerInfo must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL + "/runner-info/")
                .then()
                .statusCode(405)
                .body("exceptionType", containsString("NotAllowedException"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL + "/runner-info/mustBeInteger")
                .then()
                .statusCode(404)
                .body("error", containsString("Unable to find matching target resource method"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL + "/runner-info/666")
                .then()
                .statusCode(404)
                .body("error", containsString("RunnerInfo with ID 666 not found"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL + "/666")
                .then()
                .statusCode(404)
                .body("error", containsString("ImageStats with ID 666 not found"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL)
                .then()
                .statusCode(400)
                .body("error", containsString("ids must not be null"));

        given()
                .contentType(ContentType.JSON)
                .header("token", token)
                .when()
                .delete(StatsTestHelper.BASE_URL + "/image-name/NAME?dateNewest=2022-07-05 15:27:54.794")
                .then()
                .statusCode(400)
                .body("error", containsString("imageName, dateOldest and dateNewest must be set"));

        given().contentType(ContentType.JSON).header("token", token)
                .when()
                .get(StatsTestHelper.BASE_URL + "/lookup/runner-info/XXX?key=YYY")
                .then()
                .statusCode(400)
                .body("error", containsString(
                        "column must be one of id, testVersion, graalvmVersion, quarkusVersion, jdkVersion, description, triggeredBy"));

        given().contentType(ContentType.JSON).header("token", token)
                .when()
                .get(StatsTestHelper.BASE_URL + "/lookup/runner-info/testVersion?Blaba=Blabla")
                .then()
                .statusCode(400)
                .body("error", containsString("key must not be null"));
    }
}
