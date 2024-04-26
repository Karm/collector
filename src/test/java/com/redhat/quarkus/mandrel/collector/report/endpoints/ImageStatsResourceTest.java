/*
 * Copyright (c) 2022, 2024 Contributors to the Collector project
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redhat.quarkus.mandrel.collector.TestUtil;
import com.redhat.quarkus.mandrel.collector.report.endpoints.StatsTestHelper.Mode;
import com.redhat.quarkus.mandrel.collector.report.model.BuildPerformanceStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageSizeStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.JNIAccessStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReachableImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReflectionRegistrationStats;
import com.redhat.quarkus.mandrel.collector.report.model.RunnerInfo;
import com.redhat.quarkus.mandrel.collector.report.model.TotalClassesStats;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import io.restassured.response.ResponseBody;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class ImageStatsResourceTest {

    public static final String IMPORT_URL = StatsTestHelper.BASE_URL + "/import";

    @BeforeAll
    public static void setup() {
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    public void testImportManyToOne() throws Exception {
        final String runnerInfo1 = StatsTestHelper.getStatString("23.1/quarkus-runner.json");
        final String resultA = StatsTestHelper.getStatString("23.1/quarkus-awt.json");
        final String resultB = StatsTestHelper.getStatString("23.1/quarkus-main.json");
        final String resultC = StatsTestHelper.getStatString("23.1/quarkus-no-awt.json");
        final String runnerInfo2 = StatsTestHelper.getStatString("24.0/quarkus-runner.json");
        final String resultD = StatsTestHelper.getStatString("24.0/quarkus-jaxb.json");

        final String token = StatsTestHelper.login(Mode.READ_WRITE);

        final RunnerInfo r1 = given().contentType(ContentType.JSON).header("token", token).body(runnerInfo1)
                .when().post(StatsTestHelper.BASE_URL + "/runner-info").body().as(RunnerInfo.class);

        final ImageStats rA = given().contentType(ContentType.JSON).header("token", token).body(resultA)
                .when().post(StatsTestHelper.BASE_URL + "/import?t=mehTagA" + "&rid=" + r1.id).body().as(ImageStats.class);
        final ImageStats rB = given().contentType(ContentType.JSON).header("token", token).body(resultB)
                .when().post(StatsTestHelper.BASE_URL + "/import?t=mehTagB" + "&rid=" + r1.id).body().as(ImageStats.class);
        final ImageStats rC = given().contentType(ContentType.JSON).header("token", token).body(resultC)
                .when().post(StatsTestHelper.BASE_URL + "/import?t=mehTagC" + "&rid=" + r1.id).body().as(ImageStats.class);

        // Insert another run with another image stat:
        final RunnerInfo r2 = given().contentType(ContentType.JSON).header("token", token).body(runnerInfo2)
                .when().post(StatsTestHelper.BASE_URL + "/runner-info").body().as(RunnerInfo.class);

        final ImageStats rD = given().contentType(ContentType.JSON).header("token", token).body(resultD)
                .when().post(StatsTestHelper.BASE_URL + "/import?t=mehTagD" + "&rid=" + r2.id).body().as(ImageStats.class);
        try {

            assertTrue(r1.id > 0);
            assertEquals("https://github.com/quarkusio/quarkus/pull/66666666", r1.getGhPR());
            assertEquals("Mandrel-23.1.1.0-Final", r1.getGraalvmVersion());

            assertTrue(rA.getId() > 0);
            assertTrue(rB.getId() > 0);
            assertTrue(rC.getId() > 0);
            assertEquals(r1.id, rA.getRunnerInfo().id);
            assertEquals(r1.id, rB.getRunnerInfo().id);
            assertEquals(r1.id, rC.getRunnerInfo().id);
            assertEquals("quarkus-integration-test-awt-999-SNAPSHOT-runner", rA.getImageName());
            assertEquals("quarkus-integration-test-main-999-SNAPSHOT-runner", rB.getImageName());
            assertEquals("quarkus-integration-test-no-awt-999-SNAPSHOT-runner", rC.getImageName());

            assertTrue(r2.id > 0);
            assertEquals("https://github.com/quarkusio/quarkus/pull/66666666", r2.getGhPR());
            assertEquals("Mandrel-24.0.1.0-Final", r2.getGraalvmVersion());

            // Lookup all stats from all runs that belong to
            // a particular PR using RestAssured:
            final ImageStats[] results = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/lookup?ghPR=https://github.com/quarkusio/quarkus/pull/66666666").body()
                    .as(ImageStats[].class);
            assertEquals(4, results.length);

            final ImageStats[] run1Results = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/lookup?rid=" + r1.id).body()
                    .as(ImageStats[].class);
            assertEquals(3, run1Results.length);

            final ImageStats[] run2Results = given().when().contentType(ContentType.JSON).header("token", token)
                    .get(StatsTestHelper.BASE_URL + "/lookup?rid=" + r2.id).body()
                    .as(ImageStats[].class);
            assertEquals(1, run2Results.length);
            assertEquals("https://github.com/quarkusio/quarkus/pull/66666666", run2Results[0].getRunnerInfo().getGhPR());
            assertEquals("Mandrel-24.0.1.0-Final", run2Results[0].getRunnerInfo().getGraalvmVersion());
            assertEquals("quarkus-integration-test-jaxb-999-SNAPSHOT-runner", run2Results[0].getImageName());

        } finally {
            // Delete ImgeStats:
            Stream.of(rA.getId(), rB.getId(), rC.getId(), rD.getId()).forEach(id -> {
                given().contentType(ContentType.JSON).header("token", token).when()
                        .delete(StatsTestHelper.BASE_URL + "/" + id).then().statusCode(200);
            });
            // Delete RunnerInfo
            Stream.of(r1.id, r2.id).forEach(id -> {
                given().contentType(ContentType.JSON).header("token", token).when()
                        .delete(StatsTestHelper.BASE_URL + "/runner-info/" + id).then().statusCode(200);
            });
            TestUtil.checkLog();
        }
    }

    @Test
    public void testListEmpty() throws IOException {
        String token = StatsTestHelper.login(Mode.READ);
        given().when().contentType(ContentType.JSON).header("token", token).get(StatsTestHelper.BASE_URL).then()
                .statusCode(200).body(is("[]"));
        TestUtil.checkLog();
    }

    @Test
    public void testAddRemove() throws Exception {
        String rtoken = StatsTestHelper.login(Mode.READ);
        String wtoken = StatsTestHelper.login(Mode.WRITE);
        ImageStats imageStats = createImageStat("hello");
        String json = toJsonString(imageStats);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", wtoken).body(json).when()
                .post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertEquals(imageStats.getGraalVersion(), result.getGraalVersion());
        assertEquals(imageStats.getImageName(), result.getImageName());
        assertTrue(result.getId() > 0);

        // Ensure we can listOne the result
        given().contentType(ContentType.JSON).header("token", rtoken).when()
                .get(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(200)
                .body(containsString(imageStats.getImageName()), containsString(imageStats.getGraalVersion()));

        // Delete the created resource again
        given().contentType(ContentType.JSON).header("token", wtoken).when()
                .delete(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(200)
                .body(containsString(imageStats.getImageName()), containsString(imageStats.getGraalVersion()));

        // Now list one should no longer find the resource
        given().contentType(ContentType.JSON).header("token", rtoken).when()
                .get(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(204).body(is(""));
    }

    @Test
    public void testImport() throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        String myTag = "import-test";
        List<Long> statIds = new ArrayList<>();
        String json = StatsTestHelper.getV090StatString();
        GraalStats gStat = StatsTestHelper.parseV090Stat();

        // Import v0.9.0 schema graal json
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(IMPORT_URL + "?t=" + myTag).body();
        ImageStats result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        assertEquals(gStat.getAnalysisResults().getClassStats().getReachable(),
                result.getReachableStats().getNumClasses());
        assertTrue(result.getResourceStats().getTotalTimeSeconds() < 0);
        statIds.add(result.getId());

        // Import v0.9.1 schema graal json
        json = StatsTestHelper.getV091StatString();
        gStat = StatsTestHelper.parseV091Stat();
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(IMPORT_URL + "?t=" + myTag).body();
        result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        assertEquals(gStat.getAnalysisResults().getTypeStats().getReachable(),
                result.getReachableStats().getNumClasses());
        assertTrue(result.getResourceStats().getTotalTimeSeconds() > 0);
        statIds.add(result.getId());

        // Find stats by tag
        ImageStats[] results = given().when().contentType(ContentType.JSON).header("token", token)
                .get(StatsTestHelper.BASE_URL + "/tag/" + myTag).body().as(ImageStats[].class);
        assertEquals(2, results.length);
        for (ImageStats s : results) {
            assertEquals(myTag, s.getTag());
        }

        // Delete them again
        String imageIdsJson = toJsonString(statIds.toArray(new Long[0]));
        ImageStats[] deletedIds = given().contentType(ContentType.JSON).header("token", token).body(imageIdsJson).when()
                .delete(StatsTestHelper.BASE_URL).body().as(ImageStats[].class);
        assertEquals(2, deletedIds.length);

        // no more image stats
        given().when().header("token", token).get(StatsTestHelper.BASE_URL).then().statusCode(200).body(is("[]"));
    }

    @Test
    public void testAddTags() throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        String myTag = "some-run";
        List<Long> statIds = new ArrayList<>();
        ImageStats stats = createImageStat("foo-stat", myTag);
        String json = toJsonString(stats);
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Add another one with a tag using the query param
        stats = createImageStat("other");
        json = toJsonString(stats);
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL + "?t=" + myTag).body();
        result = body.as(ImageStats.class);
        assertEquals(myTag, result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Add a third one without a tag
        stats = createImageStat("third");
        json = toJsonString(stats);
        body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL).body();
        result = body.as(ImageStats.class);
        assertNull(result.getTag());
        assertTrue(result.getId() > 0);
        statIds.add(result.getId());

        // Find stats by tag
        ImageStats[] results = given().when().contentType(ContentType.JSON).header("token", token)
                .get(StatsTestHelper.BASE_URL + "/tag/" + myTag).body().as(ImageStats[].class);
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
        given().when().header("token", token).get(StatsTestHelper.BASE_URL).then().statusCode(200).body(is("[]"));
    }

    @Test
    public void testDistinctTags() throws Exception {
        String token = StatsTestHelper.login(Mode.READ_WRITE);
        String[] myTags = new String[] { "tag1", "tag2", "tag 3", null };
        List<ImageStats> stats = new ArrayList<>();
        // Add stats
        for (int i = 0; i < myTags.length; i++) {
            String statName = "image_name_" + i + "_" + myTags[i];
            ImageStats s = createImageStat(statName, myTags[i]);
            String json = toJsonString(s);
            ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                    .post(StatsTestHelper.BASE_URL).body();
            ImageStats result = body.as(ImageStats.class);
            assertNotEquals(0, result.getId());
            stats.add(result);
        }

        // Find distinct tags
        String[] results = given().when().contentType(ContentType.JSON).header("token", token)
                .get(StatsTestHelper.BASE_URL + "/tags/distinct").body().as(String[].class);
        assertEquals(myTags.length, results.length);
        Set<String> resultsSet = new HashSet<>(Arrays.asList(results));
        for (String myTag : myTags) {
            assertTrue(resultsSet.contains(myTags[0]), "Expected resultset to contain: " + myTag);
        }

        // Delete them again
        List<Long> statIds = new ArrayList<>();
        stats.forEach(a -> statIds.add(a.getId()));
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
        ResponseBody<?> body = given().contentType(ContentType.JSON).header("token", token).body(json).when()
                .post(StatsTestHelper.BASE_URL).body();
        ImageStats result = body.as(ImageStats.class);
        assertTrue(result.getId() > 0);

        // Update build time
        body = given().contentType(ContentType.JSON).header("token", token).body(updateJSON).when()
                .put(StatsTestHelper.BASE_URL + "/" + result.getId()).body();
        ImageStats update = body.as(ImageStats.class);
        double buildTimeSec = update.getResourceStats().getTotalTimeSeconds();
        if (expectedTimeMilis > 0) {
            // Avoid precise floating point comparison for this test
            double expectedSecs = ((double) expectedTimeMilis) / 1000;
            long expectedTime = (long) Math.floor(expectedSecs);
            assertTrue(expectedTime < buildTimeSec);
            assertTrue(expectedTime + 1 > buildTimeSec);
        } else {
            assertTrue(0 > buildTimeSec);
        }

        // Delete the created resource again
        given().contentType(ContentType.JSON).header("token", token).when()
                .delete(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(200)
                .body(containsString(imageStats.getImageName()), containsString(imageStats.getGraalVersion()));

        // Now list one should no longer find the resource
        given().contentType(ContentType.JSON).header("token", token).when()
                .get(StatsTestHelper.BASE_URL + "/" + result.getId()).then().statusCode(204).body(is(""));
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
        RunnerInfo runnerInfo = new RunnerInfo();
        imageStats.setRunnerInfo(runnerInfo);
        if (tag != null) {
            imageStats.setTag(tag);
        }
        ImageSizeStats sizeStats = new ImageSizeStats(1_000, 700, 200, 100, 100, 100, 100);
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

    public static String toJsonString(Object imageStats) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(baos, imageStats);
        return baos.toString(StandardCharsets.UTF_8);
    }
}
