/*
 * Copyright (c) 2024 Contributors to the Collector project
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
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "runner_info")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER, name = "stats_type")
public class RunnerInfo extends PanacheEntity {

    @JsonProperty
    private String testVersion;
    @JsonProperty
    private String mandrelVersion;
    @JsonProperty
    private String jdkVersion;
    @JsonProperty
    private String operatingSystem;
    @JsonProperty
    private String architecture;
    @JsonProperty
    private long memorySizeBytes;
    @JsonProperty
    private String description;

    public String getTestVersion() {
        return testVersion;
    }

    public void setTestVersion(String testVersion) {
        this.testVersion = testVersion;
    }

    public String getMandrelVersion() {
        return mandrelVersion;
    }

    public void setMandrelVersion(String mandrelVersion) {
        this.mandrelVersion = mandrelVersion;
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public void setJdkVersion(String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public String getOperatingSystem() {
        return operatingSystem;
    }

    public void setOperatingSystem(String operatingSystem) {
        this.operatingSystem = operatingSystem;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public long getMemorySizeBytes() {
        return memorySizeBytes;
    }

    public void setMemorySizeBytes(long memorySizeBytes) {
        this.memorySizeBytes = memorySizeBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
