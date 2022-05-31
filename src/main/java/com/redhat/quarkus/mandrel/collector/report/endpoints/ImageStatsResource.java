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

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStatsCollection;

@ApplicationScoped
@Path("api/v1/image-stats")
@Produces("application/json")
@Consumes("application/json")
public class ImageStatsResource {

    private static final Logger LOGGER = Logger.getLogger(ImageStatsResource.class.getName());
    private final ImageStatsCollection collection;

    public ImageStatsResource(ImageStatsCollection collection) {
        this.collection = collection;
    }

    @RolesAllowed("token_read")
    @GET
    public ImageStats[] list() {
        return collection.getAll();
    }

    @RolesAllowed("token_read")
    @GET
    @Path("{statId:\\d+}")
    public ImageStats listOne(@PathParam("statId") Long statId) {
        return collection.getSingle(statId);
    }

    @RolesAllowed("token_read")
    @GET
    @Path("tag/{tag}")
    public ImageStats[] getByTag(@PathParam("tag") String tag) {
        return collection.getAllByTag(tag);
    }

    @RolesAllowed("token_read")
    @GET
    @Path("tags/distinct")
    public String[] getDistinctTags() {
        return collection.getDistinctTags();
    }

    @RolesAllowed("token_write")
    @POST
    public ImageStats add(ImageStats stat, @QueryParam("t") String tag) {
        if (stat.getId() > 0) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        if (tag != null) {
            stat.setTag(tag);
        }
        return collection.add(stat);
    }

    @RolesAllowed("token_write")
    @DELETE
    @Path("{statId}")
    public ImageStats delete(@PathParam("statId") Long statId) {
        return collection.deleteOne(statId);
    }

    @RolesAllowed("token_write")
    @DELETE
    public ImageStats[] deleteMany(Long[] ids) {
        return collection.deleteMany(ids);
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Inject
        ObjectMapper objectMapper;

        @Override
        public Response toResponse(Exception exception) {
            LOGGER.error("Failed to handle request", exception);

            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }

            ObjectNode exceptionJson = objectMapper.createObjectNode();
            exceptionJson.put("exceptionType", exception.getClass().getName());
            exceptionJson.put("code", code);

            if (exception.getMessage() != null) {
                exceptionJson.put("error", exception.getMessage());
            }

            return Response.status(code)
                    .entity(exceptionJson)
                    .build();
        }

    }
}
