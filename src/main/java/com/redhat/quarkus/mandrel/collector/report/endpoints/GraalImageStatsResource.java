package com.redhat.quarkus.mandrel.collector.report.endpoints;

import com.redhat.quarkus.mandrel.collector.report.adapter.StatsAdapter;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStatsCollection;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@ApplicationScoped
@Path("api/v1/image-stats")
@Produces("application/json")
@Consumes("application/json")
public class GraalImageStatsResource {

    private static final StatsAdapter ADAPTER = new StatsAdapter();
    private final ImageStatsCollection collection;

    public GraalImageStatsResource(ImageStatsCollection collection) {
        this.collection = collection;
    }

    @RolesAllowed("token_write")
    @Path("import")
    @POST
    public ImageStats importStat(GraalStats importStat, @QueryParam("t") String tag) {
        ImageStats stat = ADAPTER.adapt(importStat);
        if (tag != null) {
            stat.setTag(tag);
        }
        return collection.add(stat);
    }
}
