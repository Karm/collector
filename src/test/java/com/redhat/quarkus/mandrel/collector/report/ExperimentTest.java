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
import com.redhat.quarkus.mandrel.collector.TestUtil;
import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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
            //@formatter:off
            // Order matters as we test for particular IDs.
            final SortedMap<String, List<String>> data = new TreeMap<>(){{
                    put("A-karm", List.of(StatsTestHelper.getStatString("22.3/a.json"), StatsTestHelper.getStatString("22.3/a-timing.json")));
                    put("B-karm", List.of(StatsTestHelper.getStatString("22.3/b.json"), StatsTestHelper.getStatString("22.3/b-timing.json")));
                    // New GraalVM/Mandrel versions don't have separate timing data json,
                    // it's all in a one JSON.
                    put("C-karm", List.of(StatsTestHelper.getStatString("23.1/quarkus.json")));
                    put("D-karm", List.of(StatsTestHelper.getStatString("24.0/quarkus.json")));
            }};
            //@formatter:on
            data.forEach((k, v) -> {
                final long statId = given().contentType(ContentType.JSON).header("token", token).body(v.get(0)).when()
                        .post(IMPORT_URL + "?t=" + k).body().as(ImageStats.class).getId();
                ids.add(statId);
                if (v.size() > 1) {
                    assertNotNull(given().contentType(ContentType.JSON).header("token", token).body(v.get(1)).when()
                            .put(StatsTestHelper.BASE_URL + "/" + statId).body().as(ImageStats.class));
                }
            });
            // Find stats by ImageName
            final JsonNode result22 = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/experiment/q3-build-perf-karm-1.0.0-runner").body()
                    .as(JsonNode.class);
            assertEquals("[46239,46236]", result22.get("fields_total").toString(), "fields_total");
            assertEquals("[7602569216,7529865216]", result22.get("peak_rss_bytes").toString(), "peak_rss_bytes");
            assertEquals("[5320,4962]", result22.get("gc_total_ms").toString(), "gc_total_ms");
            assertEquals("[110781,112053]", result22.get("total_build_time_ms").toString(), "total_build_time_ms");
            final JsonNode result2324 = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/experiment/experiment-1-build-perf-karm-graal-1.0.0-runner").body()
                    .as(JsonNode.class);
            assertEquals("[61,61]", result2324.get("classes_jni").toString(), "classes_jni");
            assertEquals("[58215,64697]", result2324.get("fields_total").toString(), "fields_total");
            assertEquals("[6541160448,7943794688]", result2324.get("peak_rss_bytes").toString(), "peak_rss_bytes");
            assertEquals("[12114,10565]", result2324.get("gc_total_ms").toString(), "gc_total_ms");
            assertEquals("[125289,132016]", result2324.get("total_build_time_ms").toString(), "total_build_time_ms");
            // List image names as a map id:name
            final SortedMap<Long, String> imageNames = given().when().contentType(ContentType.JSON)
                    .header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/image-names/distinct").body()
                    .as(new TypeRef<>() {
                    });
            assertEquals("q3-build-perf-karm-1.0.0-runner", imageNames.get(2L));
            assertEquals("experiment-1-build-perf-karm-graal-1.0.0-runner", imageNames.get(4L));
            final SortedMap<Long, String> searchOne = given().when().contentType(ContentType.JSON)
                    .header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/image-names/distinct/experiment-1").body()
                    .as(new TypeRef<>() {
                    });
            assertEquals("experiment-1-build-perf-karm-graal-1.0.0-runner", searchOne.get(4L));
            assertEquals(1, searchOne.size());
            TestUtil.checkLog();
        } finally {
            if (!ids.isEmpty()) {
                given().contentType(ContentType.JSON).header("token", token)
                        .body(toJsonString(ids.toArray(new Long[0]))).when().delete(StatsTestHelper.BASE_URL)
                        .statusCode();
            }
        }
    }
}
