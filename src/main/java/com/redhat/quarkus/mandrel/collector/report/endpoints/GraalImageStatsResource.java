package com.redhat.quarkus.mandrel.collector.report.endpoints;

import com.redhat.quarkus.mandrel.collector.report.adapter.StatsAdapter;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStatsCollection;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalStats;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

@ApplicationScoped
@Path("api/v1/image-stats")
@Produces("application/json")
@Consumes("application/json")
public class GraalImageStatsResource {

    private static final StatsAdapter ADAPTER = new StatsAdapter();

    @Inject
    ImageStatsCollection collection;

    @RolesAllowed("token_write")
    @Path("import")
    @POST
    public ImageStats importStat(GraalStats importStat, @QueryParam("t") String tag, @QueryParam("rid") Long runnerInfoId)
            throws WebApplicationException {
        if (importStat == null) {
            throw new WebApplicationException("GraalStats must not be null", Status.INTERNAL_SERVER_ERROR);
        }
        final ImageStats stat = ADAPTER.adapt(importStat);
        if (tag != null) {
            stat.setTag(tag);
        }
        return collection.add(stat, runnerInfoId);
    }
}
