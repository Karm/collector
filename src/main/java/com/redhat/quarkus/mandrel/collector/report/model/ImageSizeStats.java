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
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_size_stats")
public class ImageSizeStats {

    @Id
    @SequenceGenerator(name = "imageSizeStatsSeq", sequenceName = "image_size_stats_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "imageSizeStatsSeq")
    private long id;

    @JsonProperty("total_bytes")
    @Column(name = "total_bytes", nullable = false)
    private long totalSize;

    @JsonProperty("code_cache_bytes")
    @Column(name = "code_cache_bytes", nullable = false)
    private long codeCacheSize;

    @JsonProperty("heap_bytes")
    @Column(name = "heap_bytes", nullable = false)
    private long heapSize;

    @JsonProperty("resources_bytes")
    @Column(name = "resources_bytes", nullable = false)
    private long resourcesSize;

    @JsonProperty("resources_count")
    @Column(name = "resources_count", nullable = false)
    private long resourcesCount;

    @JsonProperty("other_bytes")
    @Column(name = "other_bytes", nullable = false)
    private long otherSize;

    @JsonProperty("debuginfo_bytes")
    @Column(name = "debuginfo_bytes", nullable = true)
    private long debuginfoSize;

    public ImageSizeStats() {
        // default constructor
    }

    public ImageSizeStats(long totalSize, long codeCacheSize, long heapSize, long resourcesSize, long resourcesCount,
            long otherSize, long debuginfoSize) {
        this.totalSize = totalSize;
        this.codeCacheSize = codeCacheSize;
        this.heapSize = heapSize;
        this.resourcesSize = resourcesSize;
        this.resourcesCount = resourcesCount;
        this.otherSize = otherSize;
        this.debuginfoSize = debuginfoSize;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    public long getCodeCacheSize() {
        return codeCacheSize;
    }

    public void setCodeCacheSize(long codeCacheSize) {
        this.codeCacheSize = codeCacheSize;
    }

    public long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(long heapSize) {
        this.heapSize = heapSize;
    }

    public long getResourcesSize() {
        return resourcesSize;
    }

    public void setResourcesSize(long resourcesSize) {
        this.resourcesSize = resourcesSize;
    }

    public long getResourcesCount() {
        return resourcesCount;
    }

    public void setResourcesCount(long resourcesCount) {
        this.resourcesCount = resourcesCount;
    }

    public long getOtherSize() {
        return otherSize;
    }

    public void setOtherSize(long otherSize) {
        this.otherSize = otherSize;
    }

    public long getDebuginfoSize() {
        return debuginfoSize;
    }

    public void setDebuginfoSize(long debuginfoSize) {
        this.debuginfoSize = debuginfoSize;
    }
}
