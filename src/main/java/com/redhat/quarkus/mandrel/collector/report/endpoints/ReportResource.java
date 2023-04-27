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

import com.redhat.quarkus.mandrel.collector.report.model.Report;
import com.redhat.quarkus.mandrel.collector.report.model.SimpleTimeAndSize;

import javax.annotation.security.RolesAllowed;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * DEMO for testing...
 */
@Path("/api/report")
public class ReportResource {

    /**
     * Expect JSON, e.g.
     *
     * <p>
     * { "arch": "amd64", "buildTimeS": 180, "classes": 100000, "classesForJNIAccess": 50000, "classesForReflection":
     * 50000, "classesReachable": 80000, "executableSizeMB": 55, "fields": 100000, "fieldsForJNIAccess": 50000,
     * "fieldsForReflection": 50000, "fieldsReachable": 80000, "jdkVersion": "11.0.15+10", "mandrelVersion":
     * "22.1.0.0-Final", "methods": 100000, "methodsForJNIAccess": 50000, "methodsForReflection": 50000,
     * "methodsReachable": 80000, "nativeImageXmXMB": 8000, "numberOfGC": 123, "os": "Linux", "peakRSSMB": 6860,
     * "quarkusVersion": "2.8.3.Final", "ramAvailableMB": 14336, "timeInGCS": 23, "testApp": "...some URL including
     * commit hash?" }
     *
     * @param timeAndSize
     *
     * @return
     */
    @POST
    @RolesAllowed("token_write")
    @Path("/time-and-size")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Response timeAndSize(SimpleTimeAndSize timeAndSize) {
        timeAndSize.persist();
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @RolesAllowed("token_read")
    @Path("/time-and-size")
    @Produces(MediaType.APPLICATION_JSON)
    public Response timeAndSize() {
        // TODO: pagination
        return Response.status(Response.Status.OK).entity(SimpleTimeAndSize.findAll().list()).build();
    }

    @POST
    @RolesAllowed("token_write")
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    // @Transactional - when it does some writing...
    public Response testReport(Report report) {
        return Response.status(Response.Status.CREATED).entity(report).build();
    }

    @GET
    @RolesAllowed("token_read")
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testReport() {
        return Response.status(Response.Status.OK).entity(new Report("test", 666)).build();
    }
}
