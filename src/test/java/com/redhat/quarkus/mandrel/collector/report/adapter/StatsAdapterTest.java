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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.redhat.quarkus.mandrel.collector.report.model.BuildPerformanceStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageSizeStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.JNIAccessStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReachableImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ReflectionRegistrationStats;
import com.redhat.quarkus.mandrel.collector.report.model.TotalClassesStats;
import com.redhat.quarkus.mandrel.collector.report.model.graal.AnalysisResults;
import com.redhat.quarkus.mandrel.collector.report.model.graal.CPUInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.CodeArea;
import com.redhat.quarkus.mandrel.collector.report.model.graal.DebugInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.ExecutableStats;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GCInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GeneralInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;
import com.redhat.quarkus.mandrel.collector.report.model.graal.ImageDetails;
import com.redhat.quarkus.mandrel.collector.report.model.graal.ImageHeap;
import com.redhat.quarkus.mandrel.collector.report.model.graal.MemoryInfo;
import com.redhat.quarkus.mandrel.collector.report.model.graal.ResourceUsage;
import com.redhat.quarkus.mandrel.collector.report.model.graal.ResourcesInfo;

public class StatsAdapterTest {

    private enum SchemaVersion {
        VER_0_9_0,
        VER_0_9_1
    }

    private static class StatResult {
        private final ImageStats im;
        private final GraalStats gs;

        private StatResult(ImageStats im, GraalStats gs) {
            this.im = im;
            this.gs = gs;
        }
    }

    private static final StatsAdapter ADAPTER = new StatsAdapter();

    @Test
    public void canAdaptGraalToImageStats() {
        doVersionTest(SchemaVersion.VER_0_9_0);
        doVersionTest(SchemaVersion.VER_0_9_1);
    }

    private void doVersionTest(SchemaVersion version) {
        StatResult s = doTest(true, version);
        versionSpecificAssertions(s, version);
        s = doTest(false, version);
        versionSpecificAssertions(s, version);
    }

    private void versionSpecificAssertions(StatResult s, SchemaVersion version) {
        switch (version) {
            case VER_0_9_0:
                version090Assertions(s);
                break;
            case VER_0_9_1:
                version091Assertions(s);
                break;
            default:
                throw new IllegalArgumentException("Unknown schema version " + version);
        }
    }

    private void version091Assertions(StatResult s) {
        ImageStats im = s.im;
        GraalStats gs = s.gs;
        // 0.9.1 must have timing stats
        assertTrue(im.getResourceStats().getTotalTimeSeconds() > 0);
        // 0.9.1 must have typeStats
        assertNotNull(gs.getAnalysisResults().getTypeStats());
    }

    private void version090Assertions(StatResult s) {
        ImageStats im = s.im;
        GraalStats gs = s.gs;
        // 0.9.0 doesn't have timing stats
        assertEquals(-1, im.getResourceStats().getTotalTimeSeconds());
        // 0.9.0 doesn't have typeStats
        assertNull(gs.getAnalysisResults().getTypeStats());
        assertNotNull(gs.getAnalysisResults().getClassStats());
    }

    private StatResult doTest(boolean withDebugInfo, SchemaVersion version) {
        GraalStats stat = produceGraalStat(withDebugInfo, version);

        ImageStats imStat = ADAPTER.adapt(stat);

        assertNotNull(imStat);
        assertEquals(stat.getGenInfo().getGraalVersion(), imStat.getGraalVersion());
        assertEquals(stat.getGenInfo().getName(), imStat.getImageName());

        JNIAccessStats jniStat = imStat.getJniStats();
        assertNotNull(jniStat);
        assertEquals(stat.getAnalysisResults().getClassStats().getJni(), jniStat.getNumClasses());
        assertEquals(stat.getAnalysisResults().getMethodStats().getJni(), jniStat.getNumMethods());
        assertEquals(stat.getAnalysisResults().getFieldStats().getJni(), jniStat.getNumFields());

        TotalClassesStats totalStat = imStat.getTotalStats();
        assertNotNull(totalStat);
        assertEquals(stat.getAnalysisResults().getClassStats().getTotal(), totalStat.getNumClasses());
        assertEquals(stat.getAnalysisResults().getMethodStats().getTotal(), totalStat.getNumMethods());
        assertEquals(stat.getAnalysisResults().getFieldStats().getTotal(), totalStat.getNumFields());

        ReflectionRegistrationStats reflStats = imStat.getReflectionStats();
        assertNotNull(reflStats);
        assertEquals(stat.getAnalysisResults().getClassStats().getReflection(), reflStats.getNumClasses());
        assertEquals(stat.getAnalysisResults().getMethodStats().getReflection(), reflStats.getNumMethods());
        assertEquals(stat.getAnalysisResults().getFieldStats().getReflection(), reflStats.getNumFields());

        ReachableImageStats reachSt = imStat.getReachableStats();
        assertNotNull(reachSt);
        assertEquals(stat.getAnalysisResults().getMethodStats().getReachable(), reachSt.getNumMethods());
        assertEquals(stat.getAnalysisResults().getFieldStats().getReachable(), reachSt.getNumFields());
        assertEquals(stat.getAnalysisResults().getClassStats().getReachable(), reachSt.getNumClasses());

        ImageSizeStats sizeStat = imStat.getSizeStats();
        assertNotNull(sizeStat);
        assertEquals(stat.getImageDetails().getCodeArea().getBytes(), sizeStat.getCodeCacheSize());
        if (withDebugInfo) {
            assertEquals(stat.getImageDetails().getDebugInfo().getBytes(), sizeStat.getDebuginfoSize());
        } else {
            assertNull(stat.getImageDetails().getDebugInfo());
            assertEquals(0, sizeStat.getDebuginfoSize());
        }
        assertEquals(stat.getImageDetails().getImageHeap().getBytes(), sizeStat.getHeapSize());
        assertEquals(calculateOtherBytes(stat, withDebugInfo), sizeStat.getOtherSize());
        assertEquals(stat.getImageDetails().getSizeBytes(), sizeStat.getTotalSize());

        BuildPerformanceStats perfStats = imStat.getResourceStats();
        assertNotNull(sizeStat);
        assertEquals(stat.getResourceUsage().getCpu().getCpuLoad(), perfStats.getBuilderCpuLoad());
        assertEquals(stat.getResourceUsage().getCpu().getCoresTotal(), perfStats.getNumCpuCores());
        assertEquals(stat.getResourceUsage().getMemory().getPeakRSS(), perfStats.getPeakRSSBytes());
        assertEquals(stat.getResourceUsage().getMemory().getMachineTotal(), perfStats.getBuilderMachineMemTotal());
        return new StatResult(imStat, stat);
    }

    private Long calculateOtherBytes(GraalStats stat, boolean withDebugInfo) {
        long debugInfoBytes = 0;
        if (withDebugInfo) {
            debugInfoBytes = stat.getImageDetails().getDebugInfo().getBytes();
        }
        long total = stat.getImageDetails().getSizeBytes();
        return total - (stat.getImageDetails().getCodeArea().getBytes() + debugInfoBytes
                + stat.getImageDetails().getImageHeap().getBytes());
    }

    private GraalStats produceGraalStat(boolean withDebugInfo, SchemaVersion version) {
        switch (version) {
            case VER_0_9_0:
                return produceGraalStatVers090(withDebugInfo);
            case VER_0_9_1:
                return produceGraalStatVers091(withDebugInfo);
            default:
                throw new IllegalArgumentException("Unknown schema version " + version);

        }
    }

    private GraalStats produceGraalStatVers091(boolean withDebugInfo) {
        GraalStats s = produceGenericBaseStat(withDebugInfo);
        // Schema 0.9.1 uses typeStats and sets build time
        ExecutableStats typeStats = new ExecutableStats();
        typeStats.setJni(10);
        typeStats.setReflection(1222);
        typeStats.setTotal(2000);
        typeStats.setReachable(1800);
        s.getAnalysisResults().setTypeStats(typeStats);
        ResourceUsage rs = s.getResourceUsage();
        rs.setTotalTimeSecs(30.232);
        return s;
    }

    private GraalStats produceGraalStatVers090(boolean withDebugInfo) {
        GraalStats s = produceGenericBaseStat(withDebugInfo);
        // Schema 0.9.0 used classStats over typeStats
        ExecutableStats classStats = new ExecutableStats();
        classStats.setJni(10);
        classStats.setReflection(1222);
        classStats.setTotal(2000);
        classStats.setReachable(1800);
        s.getAnalysisResults().setClassStats(classStats);
        return s;
    }

    private GraalStats produceGenericBaseStat(boolean withDebugInfo) {
        GraalStats s = new GraalStats();
        GeneralInfo general = new GeneralInfo();
        general.setCCompiler("GCC");
        general.setGarbageCollector("Serial GC");
        general.setName("helloworld");
        general.setGraalVersion("Mandrel 22.3-dev, Java 11");
        CPUInfo cpuInfo = new CPUInfo();
        cpuInfo.setCoresTotal(8);
        cpuInfo.setCpuLoad(6.8);
        MemoryInfo memInfo = new MemoryInfo();
        memInfo.setMachineTotal(1000_000);
        memInfo.setPeakRSS(500_000);
        GCInfo gcInfo = new GCInfo();
        gcInfo.setCount(17);
        gcInfo.setTotalSecs(0.20);
        ResourceUsage usage = new ResourceUsage();
        usage.setCpu(cpuInfo);
        usage.setMemory(memInfo);
        usage.setGc(gcInfo);

        AnalysisResults analysis = new AnalysisResults();
        ExecutableStats methodStats = new ExecutableStats();
        methodStats.setJni(2);
        methodStats.setReflection(121);
        methodStats.setTotal(330);
        methodStats.setReachable(300);
        analysis.setMethodStats(methodStats);
        ExecutableStats fieldStats = new ExecutableStats();
        fieldStats.setJni(3);
        fieldStats.setReachable(30);
        fieldStats.setTotal(500);
        fieldStats.setReachable(302);
        analysis.setFieldStats(fieldStats);

        ImageDetails imageDetails = new ImageDetails();
        imageDetails.setSizeBytes(300_000);
        CodeArea codeArea = new CodeArea();
        codeArea.setBytes(100_000);
        codeArea.setCompUnits(1234);
        ImageHeap heap = new ImageHeap();
        heap.setBytes(120_000);
        ResourcesInfo resInfo = new ResourcesInfo();
        resInfo.setBytes(20_000);
        resInfo.setCount(10);
        heap.setResources(resInfo);
        if (withDebugInfo) {
            DebugInfo info = new DebugInfo();
            info.setBytes(0);
            imageDetails.setDebugInfo(info);
        }

        imageDetails.setImageHeap(heap);
        imageDetails.setCodeArea(codeArea);

        s.setGenInfo(general);
        s.setResourceUsage(usage);
        s.setAnalysisResults(analysis);
        s.setImageDetails(imageDetails);
        return s;
    }
}
