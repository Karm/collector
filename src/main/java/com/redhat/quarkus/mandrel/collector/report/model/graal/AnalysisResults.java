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

public class AnalysisResults {

    // 0.9.0 schema used 'classes'
    @JsonProperty("classes")
    private ExecutableStats classStats;
    // 0.9.1 schema uses 'types'
    @JsonProperty("types")
    private ExecutableStats typeStats;
    @JsonProperty("fields")
    private ExecutableStats fieldStats;
    @JsonProperty("methods")
    private MethodStats methodStats;

    public ExecutableStats getClassStats() {
        // Prefer newer type stats over older class stats
        if (typeStats != null) {
            return typeStats;
        }
        return classStats;
    }

    public void setClassStats(ExecutableStats classStats) {
        this.classStats = classStats;
    }

    public ExecutableStats getTypeStats() {
        return typeStats;
    }

    public void setTypeStats(ExecutableStats typeStats) {
        this.typeStats = typeStats;
    }

    public ExecutableStats getFieldStats() {
        return fieldStats;
    }

    public void setFieldStats(ExecutableStats fieldStats) {
        this.fieldStats = fieldStats;
    }

    public MethodStats getMethodStats() {
        return methodStats;
    }

    public void setMethodStats(MethodStats methodStats) {
        this.methodStats = methodStats;
    }
}
