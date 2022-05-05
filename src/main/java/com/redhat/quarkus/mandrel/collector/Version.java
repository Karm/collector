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

import io.quarkus.logging.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@ApplicationScoped
public class Version {

    private String git;

    @PostConstruct
    public void init() {
        try (final InputStream is = Objects.requireNonNull(Version.class.getClassLoader()
                .getResourceAsStream("version.txt"))) {
            git = new String(is.readAllBytes(), StandardCharsets.US_ASCII);
        } catch (Exception e) {
            git = "Unknown";
            Log.error("Failed to determine version. Missing version.txt resource?", e);
        }
    }

    public String getGit() {
        return git;
    }
}
