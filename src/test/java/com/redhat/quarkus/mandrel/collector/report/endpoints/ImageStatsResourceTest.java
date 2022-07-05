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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.exc.StreamWriteException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper.Mode;
import com.redhat.quarkus.mandrel.collector.report.model.BuildPerformanceStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageSizeStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.JNIAccessStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReachableImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReflectionRegistrationStats;
import com.redhat.quarkus.mandrel.collector.report.model.TotalClassesStats;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ResponseBody;

@QuarkusTest
public class ImageStatsResourceTest {

    

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    

    @Test
    public void testListEmpty() {
        String token = StatsTestHelper.login(Mode.READ);
        given()
                .when().contentType(ContentType.JSON)
                .header("token", token).get(StatsTestHelper.BASE_URL)
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testAddRemove() throws Exception {
        String rtoken = StatsTestHelper.login(Mode.READ);
        String wtoken = StatsTestHelper.login(Mode.WRITE);
        ImageStats imageStats = createImageStat("hello");
        String json = toJsonString(imageStats);
        ResponseBody<?> body = given().contentType(ContentType.JSON)
                .header("token", wtoken)
                .body(json)
                .when().post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertEquals(imageStats.getGraalVersion(), result.getGraalVersion());
        assertEquals(imageStats.getImageName(), result.getImageName());
        assertTrue(result.getId() > 0);

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON)
                .header("token", rtoken).when().get(StatsTestHelper.BASE_URL + "/" + result.getId()).then()
                .statusCode(200).body(
                        containsString(imageStats.getImageName()),
                        containsString(imageStats.getGraalVersion()));

        // Delete the created resource again
        given().contentType(ContentType.JSON).header("token", wtoken).when().delete(StatsTestHelper.BASE_URL + "/" + result.getId()).then()
                .statusCode(200).body(
                        containsString(imageStats.getImageName()),
                        containsString(imageStats.getGraalVersion()));

        // Now list one should no longer find the resource
        given().contentType(ContentType.JSON).header("token", rtoken).when().get(StatsTestHelper.BASE_URL + "/" + result.getId()).then()
                .statusCode(204)
                .body(is(""));
    }

    @Test
    public void testAddTags() throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        String myTag = "some-run";
        List<Long> statIds = new ArrayList<>();
        ImageStats stats = createImageStat("foo-stat", myTag);
        String json = toJsonString(stats);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json)
                .when().post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Add another one with a tag using the query param
        stats = createImageStat("other");
        json = toJsonString(stats);
        body = given().contentType(ContentType.JSON).header("token", token).body(json)
                .when().post(StatsTestHelper.BASE_URL + "?t=" + myTag).body();
        result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Add a third one without a tag
        stats = createImageStat("third");
        json = toJsonString(stats);
        body = given().contentType(ContentType.JSON).header("token", token).body(json)
                .when().post(StatsTestHelper.BASE_URL).body();
        result = body.as(ImageStats.class);
        assertNull(result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Find stats by tag
        ImageStats[] results = given()
                .when().contentType(ContentType.JSON).header("token", token).get(StatsTestHelper.BASE_URL + "/tag/" + myTag)
                .body().as(ImageStats[].class);
        assertEquals(2, results.length);
        for (ImageStats s : results) {
            assertEquals(myTag, s.getTag());
            assertTrue("other".equals(s.getImageName()) || "foo-stat".equals(s.getImageName()));
        }

        // Delete them again
        String imageIdsJson = toJsonString(statIds.toArray(new Long[0]));
        ImageStats[] deletedIds = given().contentType(ContentType.JSON).header("token", token).body(imageIdsJson).when()
                .delete(StatsTestHelper.BASE_URL).body().as(ImageStats[].class);
        assertEquals(3, deletedIds.length);

        // no more image stats
        given()
                .when().header("token", token).get(StatsTestHelper.BASE_URL)
                .then()
                .statusCode(200)
                .body(is("[]"));
    }

    @Test
    public void testDistinctTags() throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        String[] myTags = new String[] {
                "tag1", "tag2", "tag 3", null
        };
        List<ImageStats> stats = new ArrayList<>();
        // Add stats
        for (int i = 0; i < myTags.length; i++) {
            String statName = "image_name_" + i + "_" + myTags[i];
            ImageStats s = createImageStat(statName, myTags[i]);
            String json = toJsonString(s);
            ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json)
                    .when().post(StatsTestHelper.BASE_URL).body();
            ImageStats result = body.as(ImageStats.class);
            assertNotEquals(0, result.getId());
            stats.add(result);
        }

        // Find distinct tags
        String[] results = given()
                .when().contentType(ContentType.JSON).header("token", token).get(StatsTestHelper.BASE_URL + "/tags/distinct")
                .body().as(String[].class);
        assertEquals(myTags.length, results.length);
        Set<String> resultsSet = new HashSet<>(Arrays.asList(results));
        for (int i = 0; i < myTags.length; i++) {
            assertTrue(resultsSet.contains(myTags[0]), "Expected resultset to contain: " + myTags[i]);
        }

        // Delete them again
        List<Long> statIds = new ArrayList<>();
        stats.stream().forEach(a -> {
            statIds.add(a.getId());
        });
        String imageIdsJson = toJsonString(statIds.toArray(new Long[0]));
        ImageStats[] deletedIds = given().contentType(ContentType.JSON).header("token", token).body(imageIdsJson).when()
                .delete(StatsTestHelper.BASE_URL).body().as(ImageStats[].class);
        assertEquals(4, deletedIds.length);
    }
    
    @Test
    public void testUpdateBuildTime() throws Exception {
        long timeInMilis = 31700;
        String updateJson = String.format("{ \"total_time\": %s }", timeInMilis);
        doUpdateTest(updateJson, timeInMilis);
    }
    
    private void doUpdateTest(String updateJSON, long expectedTimeMilis) throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        ImageStats imageStats = createImageStat("build-time");
        BuildPerformanceStats perfStats = imageStats.getResourceStats();
        perfStats.setTotalTimeSeconds(-1);
        String json = toJsonString(imageStats);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json)
                .when().post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertTrue(result.getId() > 0);
        
        // Update build time
        body = given().contentType(ContentType.JSON).header("token", token).body(updateJSON)
                .when().put(StatsTestHelper.BASE_URL + "/" + result.getId()).body();
        ImageStats update = body.as(ImageStats.class);
        double buildTimeSec = update.getResourceStats().getTotalTimeSeconds();
        if (expectedTimeMilis > 0) {
            // Avoid precise floating point comparison for this test
            double expectedSecs = ((double)expectedTimeMilis) / 1000;
            long expectedTime = (long)Math.floor(expectedSecs);
            assertTrue(expectedTime < buildTimeSec);
            assertTrue(expectedTime + 1 > buildTimeSec);
        } else {
            assertTrue(0 > buildTimeSec);
        }
        
        // Delete the created resource again
        given().contentType(ContentType.JSON).header("token", token).when().delete(StatsTestHelper.BASE_URL + "/" + result.getId()).then()
            .statusCode(200).body(
                    containsString(imageStats.getImageName()),
                    containsString(imageStats.getGraalVersion()));
        
        // Now list one should no longer find the resource
        given().contentType(ContentType.JSON).header("token", token).when().get(StatsTestHelper.BASE_URL + "/" + result.getId()).then()
            .statusCode(204)
            .body(is(""));
    }
    
    @Test
    public void testUpdateBuildTimeZero() throws Exception {
        // total_time missing from JSON, thus no update expected.
        String updateJson = "{ \"foo-bar\": 31700 }";
        doUpdateTest(updateJson, -1);
    }

    private ImageStats createImageStat(String name) {
        return createImageStat(name, null);
    }

    private ImageStats createImageStat(String name, String tag) {
        ImageStats imageStats = new ImageStats(name);
        imageStats.setGraalVersion("GraalVM 21.3 (Java 17) Mandrel Distribution");
        if (tag != null) {
            imageStats.setTag(tag);
        }
        ImageSizeStats sizeStats = new ImageSizeStats(1_000, 700, 200, 100, 100);
        imageStats.setSizeStats(sizeStats);
        JNIAccessStats jniStats = new JNIAccessStats();
        jniStats.setNumClasses(100);
        jniStats.setNumMethods(30);
        jniStats.setNumFields(0);
        TotalClassesStats totalStats = new TotalClassesStats();
        totalStats.setNumClasses(1_000);
        totalStats.setNumMethods(1_000);
        totalStats.setNumFields(1_000);
        ReachableImageStats reachableStats = new ReachableImageStats();
        reachableStats.setNumClasses(500);
        reachableStats.setNumMethods(500);
        reachableStats.setNumFields(500);
        ReflectionRegistrationStats reflectStats = new ReflectionRegistrationStats();
        reflectStats.setNumClasses(300);
        reflectStats.setNumMethods(200);
        reflectStats.setNumFields(100);
        imageStats.setJniStats(jniStats);
        imageStats.setReachableStats(reachableStats);
        imageStats.setReflectionStats(reflectStats);
        imageStats.setTotalStats(totalStats);
        BuildPerformanceStats perfStats = new BuildPerformanceStats();
        perfStats.setBuilderCpuLoad(2.0);
        perfStats.setBuilderMachineMemTotal(10000);
        perfStats.setNumCpuCores(8);
        perfStats.setPeakRSSBytes(8888);
        imageStats.setResourceStats(perfStats);
        return imageStats;
    }

    private static String toJsonString(Object imageStats) throws IOException, StreamWriteException, DatabindException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(baos, imageStats);
        return baos.toString(StandardCharsets.UTF_8);
    }

}
