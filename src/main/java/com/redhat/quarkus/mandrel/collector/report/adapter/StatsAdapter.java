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

package com.redhat.quarkus.mandrel.collector.report.adapter;

import com.redhat.quarkus.mandrel.collector.report.model.BuildPerformanceStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageSizeStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.JNIAccessStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReachableImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReflectionRegistrationStats;
import com.redhat.quarkus.mandrel.collector.report.model.TotalClassesStats;
import com.redhat.quarkus.mandrel.collector.report.model.graal.DebugInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;

public class StatsAdapter {

    public ImageStats adapt(GraalStats graalStat) {
        ImageStats stats = new ImageStats(graalStat.getGenInfo().getName());
        stats.setGraalVersion(graalStat.getGenInfo().getGraalVersion());
        JNIAccessStats jni = new JNIAccessStats();
        jni.setNumClasses(graalStat.getAnalysisResults().getClassStats().getJni());
        jni.setNumMethods(graalStat.getAnalysisResults().getMethodStats().getJni());
        jni.setNumFields(graalStat.getAnalysisResults().getFieldStats().getJni());
        stats.setJniStats(jni);
        TotalClassesStats total = new TotalClassesStats();
        total.setNumClasses(graalStat.getAnalysisResults().getClassStats().getTotal());
        total.setNumFields(graalStat.getAnalysisResults().getFieldStats().getTotal());
        total.setNumMethods(graalStat.getAnalysisResults().getMethodStats().getTotal());
        stats.setTotalStats(total);
        ReflectionRegistrationStats ref = new ReflectionRegistrationStats();
        ref.setNumClasses(graalStat.getAnalysisResults().getClassStats().getReflection());
        ref.setNumMethods(graalStat.getAnalysisResults().getMethodStats().getReflection());
        ref.setNumFields(graalStat.getAnalysisResults().getFieldStats().getReflection());
        stats.setReflectionStats(ref);
        ReachableImageStats reach = new ReachableImageStats();
        reach.setNumClasses(graalStat.getAnalysisResults().getClassStats().getReachable());
        reach.setNumMethods(graalStat.getAnalysisResults().getMethodStats().getReachable());
        reach.setNumFields(graalStat.getAnalysisResults().getFieldStats().getReachable());
        stats.setReachableStats(reach);

        ImageSizeStats size = new ImageSizeStats();
        size.setCodeCacheSize(graalStat.getImageDetails().getCodeArea().getBytes());
        DebugInfo debugInfo = graalStat.getImageDetails().getDebugInfo();
        if (debugInfo != null) {
            size.setDebuginfoSize(debugInfo.getBytes());
        }
        size.setHeapSize(graalStat.getImageDetails().getImageHeap().getBytes());
        size.setTotalSize(graalStat.getImageDetails().getSizeBytes());
        size.setOtherSize(calculateOtherSize(graalStat));
        size.setResourcesSize(graalStat.getImageDetails().getImageHeap().getResources().getBytes());
        size.setResourcesCount(graalStat.getImageDetails().getImageHeap().getResources().getCount());
        stats.setSizeStats(size);

        BuildPerformanceStats perfStats = new BuildPerformanceStats();
        perfStats.setBuilderCpuLoad(graalStat.getResourceUsage().getCpu().getCpuLoad());
        perfStats.setBuilderMachineMemTotal(graalStat.getResourceUsage().getMemory().getMachineTotal());
        perfStats.setNumCpuCores(graalStat.getResourceUsage().getCpu().getCoresTotal());
        perfStats.setPeakRSSBytes(graalStat.getResourceUsage().getMemory().getPeakRSS());
        // Schema 0.9.1 added total time to the graal stats
        double totalTimeSec = graalStat.getResourceUsage().getTotalTimeSecs();
        if (totalTimeSec > 0) {
            perfStats.setTotalTimeSeconds(totalTimeSec);
        } else {
            // Schema 0.9.0 (22.3):
            // Timing information needs to be updated using the PUT endpoint
            // of api/v1/image-stats/<id>
            perfStats.setTotalTimeSeconds(-1);
        }
        double gcTimeSec = graalStat.getResourceUsage().getGc().getTotalSecs();
        if (gcTimeSec > 0) {
            perfStats.setGcTimeSeconds(gcTimeSec);
        } else {
            // Schema 0.9.0 (22.3):
            // Timing information needs to be updated using the PUT endpoint
            // of api/v1/image-stats/<id>
            perfStats.setGcTimeSeconds(-1);
        }
        stats.setResourceStats(perfStats);
        return stats;
    }

    private long calculateOtherSize(GraalStats graalStat) {
        long total = graalStat.getImageDetails().getSizeBytes();
        long heap = graalStat.getImageDetails().getImageHeap().getBytes();
        long code = graalStat.getImageDetails().getCodeArea().getBytes();
        long debugInfo = 0;
        DebugInfo debugInfoObject = graalStat.getImageDetails().getDebugInfo();
        if (debugInfoObject != null) {
            debugInfo = debugInfoObject.getBytes();
        }
        return total - heap - code - debugInfo;
    }
}
