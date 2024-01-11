/*
 * Copyright (c) 2023 Contributors to the Collector project
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
package com.redhat.quarkus.mandrel.collector.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.redhat.quarkus.mandrel.collector.report.endpoints.ImageStatsResourceTest.IMPORT_URL;
import static com.redhat.quarkus.mandrel.collector.report.endpoints.ImageStatsResourceTest.toJsonString;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test how an experiment endpoint behaves
 */
@QuarkusTest
public class ExperimentTest {

    @Test
    public void experimentEndToEnd() throws IOException {
        final String token = StatsTestHelper.login(StatsTestHelper.Mode.READ_WRITE);
        final List<Long> ids = new ArrayList<>(2);
        try {
            final Map<String, List<String>> data = Map.of("A-karm",
                    List.of(StatsTestHelper.getStatString("22.3/a.json"),
                            StatsTestHelper.getStatString("22.3/a-timing.json")),
                    "B-karm", List.of(StatsTestHelper.getStatString("22.3/b.json"),
                            StatsTestHelper.getStatString("22.3/b-timing.json")));
            data.forEach((k, v) -> {
                final long statId = given().contentType(ContentType.JSON).header("token", token).body(v.get(0)).when()
                        .post(IMPORT_URL + "?t=" + k).body().as(ImageStats.class).getId();
                ids.add(statId);
                assertNotNull(given().contentType(ContentType.JSON).header("token", token).body(v.get(1)).when()
                        .put(StatsTestHelper.BASE_URL + "/" + statId).body().as(ImageStats.class));
            });
            // Find stats by ImageName
            final JsonNode result = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/experiment/q3-build-perf-karm-1.0.0-runner").body()
                    .as(JsonNode.class);

            assertEquals("[46239,46236]", result.get("fields_total").toString(), "fields_total");
            assertEquals("[7602569216,7529865216]", result.get("peak_rss_bytes").toString(), "peak_rss_bytes");
            assertEquals("[5320,4962]", result.get("gc_total_ms").toString(), "gc_total_ms");
            assertEquals("[110781,112053]", result.get("total_build_time_ms").toString(), "total_build_time_ms");

            // List image names
            final String[] imageNames = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/image-names/distinct").body()
                    .as(String[].class);
            assertEquals("q3-build-perf-karm-1.0.0-runner", imageNames[0]);

        } finally {
            if (!ids.isEmpty()) {
                given().contentType(ContentType.JSON).header("token", token)
                        .body(toJsonString(ids.toArray(new Long[0]))).when().delete(StatsTestHelper.BASE_URL)
                        .statusCode();
            }
        }
    }
}
