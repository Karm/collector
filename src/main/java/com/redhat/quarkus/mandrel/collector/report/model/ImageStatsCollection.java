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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApplicationScoped
public class ImageStatsCollection {

    private static final String TAG_PARAM = "tag_name";

    @Inject
    EntityManager em;

    @Transactional
    public ImageStats add(ImageStats stat) {
        stat.setCreatedAt(new Date());
        em.persist(stat);
        return stat;
    }

    public ImageStats[] getAll() {
        return em.createNamedQuery("ImageStats.findAll", ImageStats.class).getResultList().toArray(new ImageStats[0]);
    }

    public ImageStats[] getAllByTag(String tag) {
        return em.createNamedQuery("ImageStats.findByTag", ImageStats.class).setParameter(TAG_PARAM, tag)
                .getResultList().toArray(new ImageStats[0]);
    }

    public ImageStats[] getAllByImageName(String imageName) {
        return em.createNamedQuery("ImageStats.findByImageName", ImageStats.class).setParameter("image_name", imageName)
                .getResultList().toArray(new ImageStats[0]);
    }

    public String[] getDistinctTags() {
        return em.createNamedQuery("ImageStats.distinctTags", String.class).getResultList().toArray(new String[0]);
    }

    public ImageStats getSingle(long id) {
        return em.find(ImageStats.class, id);
    }

    @Transactional
    public ImageStats deleteOne(long id) {
        ImageStats stat = getSingle(id);
        em.remove(stat);
        return stat;
    }

    @Transactional
    public ImageStats[] deleteMany(Long[] ids) {
        List<ImageStats> removed = new ArrayList<>();
        for (long id : ids) {
            ImageStats stat = getSingle(id);
            em.remove(stat);
            removed.add(stat);
        }
        return removed.toArray(new ImageStats[0]);
    }

    @Transactional
    public ImageStats updateBuildTime(long id, long buildTimeMilis) {
        ImageStats stat = getSingle(id);
        if (stat == null) {
            return null;
        }
        if (buildTimeMilis != 0) {
            BuildPerformanceStats perfStats = stat.getResourceStats();
            double buildTimeSec = ((double) buildTimeMilis) / 1000;
            perfStats.setTotalTimeSeconds(buildTimeSec);
        }
        return stat;
    }
}
