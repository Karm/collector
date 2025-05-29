package com.redhat.quarkus.mandrel.collector.report.model.graal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MethodStats extends ExecutableStats {
    @JsonProperty("foreign_downcalls")
    private long foreignDowncalls;

    @JsonProperty("foreign_upcalls")
    private long foreignUpcalls;

    public long getForeignDowncalls() {
        return foreignDowncalls;
    }

    public void setForeignDowncalls(long foreignDowncalls) {
        this.foreignDowncalls = foreignDowncalls;
    }

    public long getForeignUpcalls() {
        return foreignUpcalls;
    }

    public void setForeignUpcalls(long foreignUpcalls) {
        this.foreignUpcalls = foreignUpcalls;
    }
}
