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

public class ResourceUsage {

    private static final double UNSET = -1;
    @JsonProperty("total_secs")
    private double totalTimeSecs;
    @JsonProperty("memory")
    private MemoryInfo memory;
    @JsonProperty("garbage_collection")
    private GCInfo gc;
    @JsonProperty("cpu")
    private CPUInfo cpu;

    public ResourceUsage() {
        this.totalTimeSecs = UNSET;
    }

    public double getTotalTimeSecs() {
        return totalTimeSecs;
    }

    public void setTotalTimeSecs(double totalTimeSecs) {
        this.totalTimeSecs = totalTimeSecs;
    }

    public MemoryInfo getMemory() {
        return memory;
    }

    public void setMemory(MemoryInfo memory) {
        this.memory = memory;
    }

    public GCInfo getGc() {
        return gc;
    }

    public void setGc(GCInfo gc) {
        this.gc = gc;
    }

    public CPUInfo getCpu() {
        return cpu;
    }

    public void setCpu(CPUInfo cpu) {
        this.cpu = cpu;
    }
}
