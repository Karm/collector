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

import com.redhat.quarkus.mandrel.collector.TestUtil;
import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper.Mode;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class GraalImageStatsResourceTest {

    private static final List<Long> idsToDelete = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void testImport() throws Exception {
        final String json = StatsTestHelper.getStatString("22.3/c.json");
        final String token = StatsTestHelper.login(Mode.READ_WRITE);
        final ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/import").body();
        final ImageStats result = body.as(ImageStats.class);
        idsToDelete.add(result.getId());

        assertTrue(result.getId() > 0);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("GraalVM 22.3.0-dev Java 11 Mandrel Distribution", result.getGraalVersion());

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON).header("token", token).when()
                .get(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(200)
                .body(containsString(result.getImageName()), containsString(result.getGraalVersion()));
        TestUtil.checkLog();
    }

    @Test
    public void testImportAndRunnerInfoUpdate() throws Exception {
        String json = StatsTestHelper.getStatString("22.3/c.json");
        final String token = StatsTestHelper.login(Mode.READ_WRITE);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token)
                .body(json).when()
                .post(StatsTestHelper.BASE_URL + "/import?t=meh").body();
        ImageStats result = body.as(ImageStats.class);

        final long originalId = result.getId();
        idsToDelete.add(originalId);

        assertTrue(originalId > 0);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("GraalVM 22.3.0-dev Java 11 Mandrel Distribution", result.getGraalVersion());
        assertNull(result.getRunnerInfo());

        // Now add some runner info
        json = StatsTestHelper.getStatString("22.3/c-runner.json");
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/update-runner-info/" + originalId).body();
        result = body.as(ImageStats.class);

        assertEquals(result.getId(), originalId);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("Github Runner 2.315.0", result.getRunnerInfo().getDescription());
        assertEquals(274877906944L, result.getRunnerInfo().getMemorySizeBytes());
        assertEquals("https://github.com/quarkusio/quarkus/pull/31490", result.getRunnerInfo().getTriggeredBy());

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON).header("token", token).when()
                .get(StatsTestHelper.BASE_URL + "/" + originalId).then().statusCode(200)
                .body(containsString(result.getImageName()),
                        containsString(result.getGraalVersion()),
                        containsString(result.getRunnerInfo().getDescription()));
        TestUtil.checkLog();
    }

    @Test
    public void testImportAndRunnerM24InfoUpdate() throws Exception {
        String json = StatsTestHelper.getStatString("24.0/quarkus.json");
        final String token = StatsTestHelper.login(Mode.READ_WRITE);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/import").body();
        ImageStats result = body.as(ImageStats.class);

        long originalId = result.getId();
        idsToDelete.add(originalId);

        assertTrue(originalId > 0);
        assertEquals("experiment-1-build-perf-karm-graal-1.0.0-runner", result.getImageName());
        assertEquals("Mandrel-24.0.0.0-dev18889be7190", result.getGraalVersion());

        // Now add some runner info
        json = StatsTestHelper.getStatString("24.0/c-runner-missing-stuff.json");
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/update-runner-info/" + originalId).body();
        result = body.as(ImageStats.class);

        assertEquals(result.getId(), originalId);
        assertEquals("experiment-1-build-perf-karm-graal-1.0.0-runner", result.getImageName());
        assertEquals("Github Runner 666", result.getRunnerInfo().getDescription());
        assertEquals("3.8.4.Final", result.getRunnerInfo().getQuarkusVersion());
        assertEquals(274877906944L, result.getRunnerInfo().getMemorySizeBytes());

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON).header("token", token).when()
                .get(StatsTestHelper.BASE_URL + "/" + originalId).then().statusCode(200)
                .body(containsString(result.getImageName()),
                        containsString(result.getGraalVersion()),
                        containsString(result.getRunnerInfo().getDescription()));
        TestUtil.checkLog();
    }

    @AfterEach
    public void delete() {
        // Delete the created resources again
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        for (Long id : idsToDelete) {
            given().contentType(ContentType.JSON).header("token", token).when()
                    .delete(StatsTestHelper.BASE_URL + "/" + id).then().statusCode(200);
        }
        idsToDelete.clear();
    }
}
