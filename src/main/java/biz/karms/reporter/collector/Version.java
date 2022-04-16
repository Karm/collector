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
package biz.karms.reporter.collector;

import io.quarkus.logging.Log;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;
import java.util.Scanner;

@ApplicationScoped
public class Version {

    private String git;

    @PostConstruct
    public void init() {
        try (Scanner s = new Scanner(Objects.requireNonNull(Version.class.getClassLoader()
                .getResourceAsStream("/version.txt"))).useDelimiter("\\A")) {
            git = s.hasNext() ? s.next() : "";
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    public String getGit() {
        return git;
    }
}
