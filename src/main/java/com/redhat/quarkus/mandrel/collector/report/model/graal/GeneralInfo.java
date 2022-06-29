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

package com.redhat.quarkus.mandrel.collector.report.model.graal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeneralInfo {

    @JsonProperty("name")
    private String name;
    @JsonProperty("c_compiler")
    private String cCompiler;
    @JsonProperty("java_version")
    private String javaVersion;
    @JsonProperty("garbage_collector")
    private String garbageCollector;
    @JsonProperty("graalvm_version")
    private String graalVersion;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCCompiler() {
        return cCompiler;
    }
    public void setCCompiler(String cCompiler) {
        this.cCompiler = cCompiler;
    }
    public String getJavaVersion() {
        return javaVersion;
    }
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }
    public String getGarbageCollector() {
        return garbageCollector;
    }
    public void setGarbageCollector(String garbageCollector) {
        this.garbageCollector = garbageCollector;
    }
    public String getGraalVersion() {
        return graalVersion;
    }
    public void setGraalVersion(String graalVersion) {
        this.graalVersion = graalVersion;
    }
}
