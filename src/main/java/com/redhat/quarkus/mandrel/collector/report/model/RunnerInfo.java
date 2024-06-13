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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Entity
@Table(name = "runner_info", indexes = {
        @Index(columnList = "testVersion, graalvmVersion, quarkusVersion, triggeredBy") })
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER, name = "stats_type")
public class RunnerInfo extends PanacheEntity {

    private String testVersion;
    private String graalvmVersion;
    private String quarkusVersion;
    private String jdkVersion;
    private String operatingSystem;
    private String architecture;
    private long memorySizeBytes;
    private long memoryAvailableBytes;
    private String description;
    private String triggeredBy;
    // We use underscore for the column name for consistency with ImageStats.
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date createdAt;

    public String getTestVersion() {
        return testVersion;
    }

    public void setTestVersion(String testVersion) {
        this.testVersion = testVersion;
    }

    public String getGraalvmVersion() {
        return graalvmVersion;
    }

    public void setGraalvmVersion(String graalvmVersion) {
        this.graalvmVersion = graalvmVersion;
    }

    public String getQuarkusVersion() {
        return quarkusVersion;
    }

    public void setQuarkusVersion(String quarkusVersion) {
        this.quarkusVersion = quarkusVersion;
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

    public long getMemoryAvailableBytes() {
        return memoryAvailableBytes;
    }

    public void setMemoryAvailableBytes(long memoryAvailableBytes) {
        this.memoryAvailableBytes = memoryAvailableBytes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
