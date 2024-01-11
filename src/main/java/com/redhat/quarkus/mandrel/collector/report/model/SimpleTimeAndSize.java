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

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity(name = "simple_time_and_size")
@Table(name = "simple_time_and_size")
@Immutable
public class SimpleTimeAndSize extends PanacheEntity {
    // TODO create enums
    // System
    public String quarkusVersion;
    public String mandrelVersion;
    public String jdkVersion;
    public String os;
    public String arch;
    public int ramAvailableMB;
    public String runnerDescription;

    // Record
    @CreationTimestamp
    public LocalDateTime created;
    public String testApp;

    // Build
    public int timeInGCS;
    public int numberOfGC;
    public int peakRSSMB;
    public int classes;
    public int classesReachable;
    public int fields;
    public int fieldsReachable;
    public int methods;
    public int methodsReachable;
    public int classesForReflection;
    public int fieldsForReflection;
    public int methodsForReflection;
    public int classesForJNIAccess;
    public int fieldsForJNIAccess;
    public int methodsForJNIAccess;
    public int executableSizeMB;
    public int buildTimeS;

    // Settings
    public int nativeImageXmXMB;
}
