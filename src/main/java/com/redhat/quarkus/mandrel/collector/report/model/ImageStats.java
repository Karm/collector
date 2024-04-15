/*
 * Copyright (c) 2022, 2024 Contributors to the Collector project
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
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "image_stats")
//@formatter:off
@NamedQuery(name = "ImageStats.findAll",
        query = "SELECT s FROM ImageStats s ORDER BY s.imageName")
@NamedQuery(name = "ImageStats.findByTag",
        query = "SELECT s FROM ImageStats s WHERE s.tag = :tag_name ORDER BY s.tag, s.imageName")
@NamedQuery(name = "ImageStats.findByImageName",
        query = "SELECT s FROM ImageStats s "
                + "WHERE s.imageName = :image_name ORDER BY s.resourceStats.totalTimeSeconds, s.imageName, s.tag")
@NamedQuery(name = "ImageStats.distinctTags",
        query = "SELECT distinct s.tag FROM ImageStats s")
/*
 * We want only distinct experiment names. The ID printed alongside with it is just the ID of
 * the latest recorded occurrence of such experiment. It facilitates communication with UI.
 */
@NamedQuery(name = "ImageStats.distinctImageNames",
        query = "SELECT s.imageName, MAX(s.id) AS id FROM ImageStats s GROUP BY s.imageName ORDER BY s.imageName DESC")
@NamedQuery(name = "ImageStats.distinctImageNamesByKeyword",
        query = "SELECT s.imageName, MAX(s.id) AS id FROM ImageStats s WHERE "
                + "s.imageName LIKE CONCAT('%',:keyword,'%') GROUP BY s.imageName ORDER BY s.imageName DESC")
// Delete by image name and date created
@NamedQuery(name = "ImageStats.deleteByImageNameAndDate",
        query = "DELETE FROM ImageStats s WHERE s.imageName = :image_name AND "
                + "s.createdAt > :date_created_oldest AND s.createdAt < :date_created_newest")
//@formatter:on
public class ImageStats extends TimestampedEntity {
    @JsonProperty("tag")
    @Column(name = "tag_name", nullable = true)
    private String tag;

    @JsonProperty("img_name")
    @Column(name = "image_name")
    private String imageName;

    @JsonProperty("generator_version")
    private String graalVersion;

    @JsonProperty("image_size_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "size_stats_id")
    private ImageSizeStats sizeStats;

    @JsonProperty("jni_classes_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "jni_stats_id")
    private JNIAccessStats jniStats;

    @JsonProperty("reflection_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "reflection_stats_id")
    private ReflectionRegistrationStats reflectionStats;

    @JsonProperty("build_perf_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "perf_stats_id")
    private BuildPerformanceStats resourceStats;

    @JsonProperty("total_classes_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "total_stats_id")
    private TotalClassesStats totalStats;

    @JsonProperty("reachability_stats")
    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "reachable_stats_id")
    private ReachableImageStats reachableStats;

    @JsonProperty("runner_info")
    @OneToOne(optional = true, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "runner_info_id")
    private RunnerInfo runnerInfo;

    @Id
    @SequenceGenerator(name = "imageStatsSeq", sequenceName = "image_stats_id_seq", allocationSize = 1, initialValue = 1)
    @GeneratedValue(generator = "imageStatsSeq")
    private long id;

    public ImageStats(String name) {
        this.imageName = name;
    }

    public void setJniStats(JNIAccessStats jniStats) {
        this.jniStats = jniStats;
    }

    public void setReflectionStats(ReflectionRegistrationStats reflectionStats) {
        this.reflectionStats = reflectionStats;
    }

    public void setResourceStats(BuildPerformanceStats resourceStats) {
        this.resourceStats = resourceStats;
    }

    public void setTotalStats(TotalClassesStats totalStats) {
        this.totalStats = totalStats;
    }

    public void setReachableStats(ReachableImageStats reachableStats) {
        this.reachableStats = reachableStats;
    }

    public void setRunnerInfo(RunnerInfo runnerInfo) {
        this.runnerInfo = runnerInfo;
    }

    // Hibernate needs this
    public ImageStats() {
        // default constructor
    }

    public ImageSizeStats getSizeStats() {
        return sizeStats;
    }

    public void setSizeStats(ImageSizeStats sizeStats) {
        this.sizeStats = sizeStats;
    }

    public JNIAccessStats getJniStats() {
        return jniStats;
    }

    public ReflectionRegistrationStats getReflectionStats() {
        return reflectionStats;
    }

    public BuildPerformanceStats getResourceStats() {
        return resourceStats;
    }

    public String getImageName() {
        return imageName;
    }

    public TotalClassesStats getTotalStats() {
        return totalStats;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public ReachableImageStats getReachableStats() {
        return reachableStats;
    }

    public RunnerInfo getRunnerInfo() {
        return runnerInfo;
    }

    public String getGraalVersion() {
        return graalVersion;
    }

    public void setGraalVersion(String graalVersion) {
        this.graalVersion = graalVersion;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ImageStats)) {
            return false;
        }
        ImageStats o = (ImageStats) other;
        return getImageName().equals(o.getImageName());
    }
}
