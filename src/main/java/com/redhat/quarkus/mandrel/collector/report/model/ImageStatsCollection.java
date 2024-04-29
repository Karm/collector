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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ImageStatsCollection {

    public static final String CREATED_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final DateTimeFormatter CREATED_DATE_FORMATTER = DateTimeFormatter.ofPattern(CREATED_DATE_FORMAT);

    @Inject
    EntityManager em;

    @Transactional
    public ImageStats add(ImageStats stat, String tag, Long runnerInfoId) {
        if (tag != null) {
            stat.setTag(tag);
        }
        if (runnerInfoId != null) {
            // Override the RunnerInfo if the ID is provided.
            final RunnerInfo runnerInfo = getSingleRunnerInfo(runnerInfoId);
            if (runnerInfo == null) {
                throw new WebApplicationException("RunnerInfo with ID " + runnerInfoId + " not found.",
                        Response.Status.NOT_FOUND);
            }
            stat.setRunnerInfo(runnerInfo);
        } else {
            if (stat.getRunnerInfo() != null && stat.getRunnerInfo().id == null) {
                // If the RunnerInfo is not persisted yet, do it now
                // to avoid a "save the transient instance before flushing" exception.
                stat.getRunnerInfo().setCreatedAt(new Date());
                em.persist(stat.getRunnerInfo());
            }
        }
        stat.setCreatedAt(new Date());
        em.persist(stat);
        return stat;
    }

    public ImageStats[] getAll() {
        return em.createNamedQuery("ImageStats.findAll", ImageStats.class).getResultList().toArray(new ImageStats[0]);
    }

    public ImageStats[] getAllByTag(String tag) {
        return em.createNamedQuery("ImageStats.findByTag", ImageStats.class).setParameter("tag_name", tag)
                .getResultList().toArray(new ImageStats[0]);
    }

    public ImageStats[] getAllByImageName(String imageName) {
        return em.createNamedQuery("ImageStats.findByImageName", ImageStats.class).setParameter("image_name", imageName)
                .getResultList().toArray(new ImageStats[0]);
    }

    public String[] getDistinctTags() {
        return em.createNamedQuery("ImageStats.distinctTags", String.class).getResultList().toArray(new String[0]);
    }

    public Map<Long, String> getDistinctImageNames() {
        return em.createNamedQuery("ImageStats.distinctImageNames", Tuple.class).getResultList().stream()
                .collect(HashMap::new, (m, t) -> m.put(t.get(1, Long.class), t.get(0, String.class)), HashMap::putAll);
    }

    public Map<Long, String> getDistinctImageNames(String keyword) {
        return em.createNamedQuery("ImageStats.distinctImageNamesByKeyword", Tuple.class).setParameter("keyword", keyword)
                .getResultList().stream()
                .collect(HashMap::new, (m, t) -> m.put(t.get(1, Long.class), t.get(0, String.class)), HashMap::putAll);
    }

    public ImageStats[] lookup(String what, ImageStats.SearchableRunnerInfo where, boolean wildcard) {
        final String q = "SELECT s FROM ImageStats s WHERE s.runnerInfo." + where.column + " " +
                (wildcard ? "LIKE CONCAT('%',:" + where.column + ",'%')" : "= :" + where.column) + " ORDER BY s.imageName";
        return em.createQuery(q, ImageStats.class)
                .setParameter(where.column, what)
                .getResultList().toArray(new ImageStats[0]);
    }

    public ImageStats getSingle(long id) {
        return em.find(ImageStats.class, id);
    }

    @Transactional
    public ImageStats deleteOne(long id) {
        final ImageStats stat = getSingle(id);
        if (stat == null) {
            throw new WebApplicationException("ImageStats with ID " + id + " not found.",
                    Response.Status.NOT_FOUND);
        }
        em.remove(stat);
        return stat;
    }

    @Transactional
    public ImageStats[] deleteMany(Long[] ids) {
        final List<ImageStats> removed = new ArrayList<>();
        for (long id : ids) {
            final ImageStats stat = getSingle(id);
            // If we get a request to delete an ID which is already gone, there is nothing to do.
            if (stat == null) {
                continue;
            }
            em.remove(stat);
            removed.add(stat);
        }
        return removed.toArray(new ImageStats[0]);
    }

    @Transactional
    public int deleteManyByImageNameAndDate(String imageName, Date dateOldest, Date dateNewest) {
        return em.createNamedQuery("ImageStats.deleteByImageNameAndDate")
                .setParameter("image_name", imageName)
                .setParameter("date_created_oldest", dateOldest)
                .setParameter("date_created_newest", dateNewest)
                .executeUpdate();
    }

    @Transactional
    public ImageStats updateBuildTime(long id, long buildTimeMilis) {
        final ImageStats stat = getSingle(id);
        if (stat == null) {
            return null;
        }
        if (buildTimeMilis != 0) {
            final BuildPerformanceStats perfStats = stat.getResourceStats();
            perfStats.setTotalTimeSeconds(((double) buildTimeMilis) / 1000);
        }
        return stat;
    }

    @Transactional
    public ImageStats updateRunnerInfo(long id, RunnerInfo info) {
        final ImageStats stat = getSingle(id);
        if (stat == null) {
            return null;
        }
        if (info != null) {
            info.setCreatedAt(new Date());
            em.persist(info);
            stat.setRunnerInfo(info);
        }
        return stat;
    }

    // RunnerInfo

    public RunnerInfo getSingleRunnerInfo(long id) {
        return em.find(RunnerInfo.class, id);
    }

    @Transactional
    public RunnerInfo add(RunnerInfo runnerInfo) {
        runnerInfo.setCreatedAt(new Date());
        em.persist(runnerInfo);
        return runnerInfo;
    }

    /**
     * RunnerInfo is optional, so we don't delete the attached stats alongside with it.
     */
    @Transactional
    public RunnerInfo deleteRunnerInfo(long id) {
        final RunnerInfo r = getSingleRunnerInfo(id);
        if (r == null) {
            throw new WebApplicationException("RunnerInfo with ID " + id + " not found.",
                    Response.Status.NOT_FOUND);
        }
        em.remove(r);
        return r;
    }
}
