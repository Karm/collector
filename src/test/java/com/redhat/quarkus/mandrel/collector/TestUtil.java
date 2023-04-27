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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtil {

    public static Matcher parseLog(final Pattern lineMatchRegexp) {
        final Path logFilePath = Paths.get(".", "target", "quarkus.log").toAbsolutePath();
        final AtomicReference<Matcher> result = new AtomicReference<>();
        org.awaitility.Awaitility.given().pollInterval(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertTrue(Files.exists(logFilePath), "Quarkus log file " + logFilePath + " is missing");
                    boolean found = false;
                    Matcher matcher = null;
                    final StringBuilder sbLog = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            new ByteArrayInputStream(Files.readAllBytes(logFilePath)), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sbLog.append(line).append("\r\n");
                            matcher = lineMatchRegexp.matcher(line);
                            found = matcher.matches();
                            if (found) {
                                break;
                            }
                        }
                    }
                    assertTrue(found, "Pattern " + lineMatchRegexp.pattern() + " not found in log " + logFilePath
                            + ". \n" + "The log was: " + sbLog);
                    result.set(matcher);
                });
        return result.get();
    }
}
