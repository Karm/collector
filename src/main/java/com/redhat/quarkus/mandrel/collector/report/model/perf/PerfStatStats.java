package com.redhat.quarkus.mandrel.collector.report.model.perf;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * TODO: Break it into more entities, namely:
 *  - System stats
 *  - perf tool stats
 *  - GC stats
 *  - ...?
 */
@Entity(name = "perf_stat_stats")
@Table(name = "perf_stat_stats")
@Immutable
public class PerfStatStats extends PanacheEntity {
    // System
    public String quarkusVersion;
    public String mandrelVersion;
    public String jdkVersion;
    public String os;
    public String arch;
    public int ramAvailableMB = -1;
    public int coresAvailable = -1;
    public String runnerDescription;

    // Record
    @CreationTimestamp
    public LocalDateTime created;
    public String testApp;

    // Perf
    public String file;
    public double taskClock = -1;
    public long contextSwitches = -1;
    public long cpuMigrations = -1;
    public long pageFaults = -1;
    public long cycles = -1;
    public long instructions = -1;
    public long branches = -1;
    public long branchMisses = -1;
    public double secondsTimeElapsed = -1;

    //GC
    public int fullGCevents = -1;
    public int incrementalGCevents = -1;
    public double timeSpentInGCs = -1.0;

    // Aux
    public long rssKb = -1;
    public long executableSizeKb = -1;

    // Settings
    public long maxHeapSizeMB = -1;
    public Boolean parseOnce = false;
}
