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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import com.redhat.quarkus.mandrel.collector.TestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper.Mode;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ResponseBody;

@QuarkusTest
public class GraalImageStatsResourceTest {

    private static final List<Long> idsToDelete = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void testImport() throws Exception {
        String json = createGraalJSON();
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/import").body();
        ImageStats result = body.as(ImageStats.class);

        assertTrue(result.getId() > 0);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("GraalVM 22.3.0-dev Java 11 Mandrel Distribution", result.getGraalVersion());
        idsToDelete.add(result.getId());

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON).header("token", token).when()
                .get(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(200)
                .body(containsString(result.getImageName()), containsString(result.getGraalVersion()));
        TestUtil.checkLog();
    }

    @Test
    public void testImportAndRunnerInfoUpdate() throws Exception {
        String json = createGraalJSON();
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/import").body();
        ImageStats result = body.as(ImageStats.class);

        long originalId = result.getId();
        assertTrue(originalId > 0);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("GraalVM 22.3.0-dev Java 11 Mandrel Distribution", result.getGraalVersion());

        idsToDelete.add(originalId);

        // Now add some runner info
        json = createRunnerJSON();
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "/update-runner-info/" + originalId).body();
        result = body.as(ImageStats.class);

        assertEquals(result.getId(), originalId);
        assertEquals("foo-bar", result.getImageName());
        assertEquals("Github Runner 2.315.0", result.getRunnerInfo().getDescription());
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

    private String createGraalJSON() {
        // @formatter:off
        return "{\n"
                + "  \"resource_usage\": {\n"
                + "    \"memory\": {\n"
                + "      \"system_total\": 33260355584,\n"
                + "      \"peak_rss_bytes\": 3127443456\n"
                + "    },\n"
                + "    \"garbage_collection\": {\n"
                + "      \"count\": 20,\n"
                + "      \"total_secs\": 1.097\n"
                + "    },\n"
                + "    \"cpu\": {\n"
                + "      \"load\": 6.307451297470753,\n"
                + "      \"total_cores\": 8\n"
                + "    }\n"
                + "  },\n"
                + "  \"image_details\": {\n"
                + "    \"debug_info\": {\n"
                + "      \"bytes\": 7694974\n"
                + "    },\n"
                + "    \"code_area\": {\n"
                + "      \"bytes\": 4181808,\n"
                + "      \"compilation_units\": 7040\n"
                + "    },\n"
                + "    \"total_bytes\": 20157545,\n"
                + "    \"image_heap\": {\n"
                + "      \"bytes\": 7233536,\n"
                + "      \"resources\": {\n"
                + "        \"bytes\": 142884,\n"
                + "        \"count\": 5\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"general_info\": {\n"
                + "    \"c_compiler\": \"gcc (redhat, x86_64, 11.3.1)\",\n"
                + "    \"name\": \"foo-bar\",\n"
                + "    \"java_version\": null,\n"
                + "    \"garbage_collector\": \"Serial GC\",\n"
                + "    \"graalvm_version\": \"GraalVM 22.3.0-dev Java 11 Mandrel Distribution\"\n"
                + "  },\n"
                + "  \"analysis_results\": {\n"
                + "    \"methods\": {\n"
                + "      \"total\": 27123,\n"
                + "      \"reflection\": 267,\n"
                + "      \"jni\": 52,\n"
                + "      \"reachable\": 12255\n"
                + "    },\n"
                + "    \"classes\": {\n"
                + "      \"total\": 3725,\n"
                + "      \"reflection\": 27,\n"
                + "      \"jni\": 58,\n"
                + "      \"reachable\": 2709\n"
                + "    },\n"
                + "    \"fields\": {\n"
                + "      \"total\": 6388,\n"
                + "      \"reflection\": 0,\n"
                + "      \"jni\": -1,\n"
                + "      \"reachable\": 3430\n"
                + "    }\n"
                + "  }\n"
                + "}";
        // @formatter:on
    }

    // We intentionally leave out the "operatingSystem" entry to test how missing entries are handled
    private String createRunnerJSON() {
        return """
                 {
                   "testVersion": "1.0.0",
                   "mandrelVersion": "22.3.0",
                   "jdkVersion": "17.0.7",
                   "architecture": "x86_64",
                   "memorySizeBytes": 274877906944,
                   "description" : "Github Runner 2.315.0"
                 }
                """;
    }
}
