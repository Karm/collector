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
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestUtil {
    private static final Path LOG_FILE = Paths.get(".", "target", "quarkus.log").toAbsolutePath();
    private static final Pattern WARN_ERROR_DETECTION_PATTERN = Pattern.compile(
            "(?i:.*(ERROR|WARN|No such file|Not found|unknown).*)");
    private static final Pattern[] WHITELIST = new Pattern[] {
            // Maven & deps names
            Pattern.compile(".*maven-error-diagnostics.*"),
            Pattern.compile(".*errorprone.*"),
            // JDK:
            Pattern.compile("WARNING.* reflective access.*"),
            Pattern.compile("WARNING: All illegal access operations.*"),
            Pattern.compile("WARNING: Please consider reporting this to the maintainers.*"),
            // Schema during testing
            Pattern.compile(".*Error executing DDL.*"),
            Pattern.compile(".*Table 'user' already exists.*"),
            Pattern.compile(".*Table 'quarkus.image_stats' doesn't exist.*"),
            Pattern.compile(".*Table 'quarkus.token' doesn't exist.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.build_perf_stats_id_seq'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.class_stats_id_seq'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.image_size_stats_id_seq'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.image_stats_id_seq'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.perf_stat_stats_SEQ'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.simple_time_and_size_SEQ'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.token_SEQ'.*"),
            Pattern.compile(".*Unknown SEQUENCE: 'quarkus.user_SEQ'.*"),
    };

    public static Matcher parseLog(final Pattern lineMatchRegexp) {
        final AtomicReference<Matcher> result = new AtomicReference<>();
        org.awaitility.Awaitility.given().pollInterval(100, TimeUnit.MILLISECONDS).atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    assertTrue(Files.exists(LOG_FILE), "Quarkus log file " + LOG_FILE + " is missing");
                    boolean found = false;
                    Matcher matcher = null;
                    final StringBuilder sbLog = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                            new ByteArrayInputStream(Files.readAllBytes(LOG_FILE)), StandardCharsets.UTF_8))) {
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
                    assertTrue(found, "Pattern " + lineMatchRegexp.pattern() + " not found in log " + LOG_FILE
                            + ". \n" + "The log was: " + sbLog);
                    result.set(matcher);
                });
        return result.get();
    }

    public static void checkLog() throws IOException {
        try (final Scanner sc = new Scanner(LOG_FILE, UTF_8)) {
            final Set<String> offendingLines = new HashSet<>();
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                final boolean error = WARN_ERROR_DETECTION_PATTERN.matcher(line).matches();
                boolean whiteListed = false;
                if (error) {
                    for (Pattern p : WHITELIST) {
                        if (p.matcher(line).matches()) {
                            whiteListed = true;
                            break;
                        }
                    }
                    if (!whiteListed) {
                        offendingLines.add(line);
                    }
                }
            }
            assertTrue(offendingLines.isEmpty(),
                    "Log must not contain non-whitelisted warnings or errors: \n" +
                            String.join("\n", offendingLines));
        }
    }
}
