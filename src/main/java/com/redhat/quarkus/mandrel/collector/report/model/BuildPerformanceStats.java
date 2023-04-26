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

package com.redhat.quarkus.mandrel.collector.report.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "build_perf_stats")
public class BuildPerformanceStats {

    private long id;
    @JsonProperty("total_build_time_sec")
    private double totalTimeSeconds;
    @JsonProperty("gc_time_sec")
    private double gcTimeSeconds;
    @JsonProperty("num_cpu_cores")
    private long numCpuCores;
    @JsonProperty("total_machine_memory")
    private long builderMachineMemTotal;
    @JsonProperty("peak_rss_bytes")
    private long peakRSSBytes;
    @JsonProperty("cpu_load")
    private double builderCpuLoad;

    @Id
    @SequenceGenerator(name = "buildPerfStatsSeq", sequenceName = "build_perf_stats_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "buildPerfStatsSeq")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getTotalTimeSeconds() {
        return totalTimeSeconds;
    }

    public void setTotalTimeSeconds(double totalTimeSeconds) {
        this.totalTimeSeconds = totalTimeSeconds;
    }

    public double getGcTimeSeconds() {
        return gcTimeSeconds;
    }

    public void setGcTimeSeconds(double gcTimeSeconds) {
        this.gcTimeSeconds = gcTimeSeconds;
    }

    public long getNumCpuCores() {
        return numCpuCores;
    }

    public void setNumCpuCores(long numCpuCores) {
        this.numCpuCores = numCpuCores;
    }

    public long getBuilderMachineMemTotal() {
        return builderMachineMemTotal;
    }

    public void setBuilderMachineMemTotal(long builderMachineMemTotal) {
        this.builderMachineMemTotal = builderMachineMemTotal;
    }

    public long getPeakRSSBytes() {
        return peakRSSBytes;
    }

    public void setPeakRSSBytes(long peakRSSBytes) {
        this.peakRSSBytes = peakRSSBytes;
    }

    public double getBuilderCpuLoad() {
        return builderCpuLoad;
    }

    public void setBuilderCpuLoad(double builderCpuLoad) {
        this.builderCpuLoad = builderCpuLoad;
    }
}
