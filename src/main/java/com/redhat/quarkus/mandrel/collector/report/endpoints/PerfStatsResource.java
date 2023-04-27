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
package com.redhat.quarkus.mandrel.collector.report.endpoints;

import com.redhat.quarkus.mandrel.collector.report.model.perf.PerfStatStats;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@ApplicationScoped
@Path("api/v1/perfstats")
public class PerfStatsResource {

    @POST
    @RolesAllowed("token_write")
    @Path("/perf")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Response timeAndSize(List<PerfStatStats> perfStatStats) {
        // TODO: Is this idiomatic Panache?
        perfStatStats.forEach(stat -> stat.persist());
        // We might not want to spit it all right back, ...or?
        return Response.status(Response.Status.CREATED).entity(perfStatStats).build();
    }

    @GET
    @RolesAllowed("token_read")
    @Path("/perf")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timeAndSize() {
        // TODO: Paging, lookup...
        return Response.status(Response.Status.OK).entity(PerfStatStats.findAll().list()).build();
    }
}
