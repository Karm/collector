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
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "class_stats")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.INTEGER, name = "stats_type")
public class ClassStats {

    @Id
    @SequenceGenerator(name = "classStatsSeq", sequenceName = "class_stats_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "classStatsSeq")
    private long id;
    @JsonProperty("classes")
    private long numClasses;
    @JsonProperty("fields")
    private long numFields;
    @JsonProperty("methods")
    private long numMethods;

    public ClassStats() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getNumClasses() {
        return numClasses;
    }

    public void setNumClasses(long numClasses) {
        this.numClasses = numClasses;
    }

    public long getNumFields() {
        return numFields;
    }

    public void setNumFields(long numFields) {
        this.numFields = numFields;
    }

    public long getNumMethods() {
        return numMethods;
    }

    public void setNumMethods(long numMethods) {
        this.numMethods = numMethods;
    }
}
