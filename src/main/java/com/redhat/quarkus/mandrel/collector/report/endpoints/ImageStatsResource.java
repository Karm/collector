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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStats;
import com.redhat.quarkus.mandrel.collector.report.model.ImageStatsCollection;
import com.redhat.quarkus.mandrel.collector.report.model.graal.GraalBuildInfo;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.TreeMap;

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
    @Path("experiment/{image_name}")
    public Response getExperiment(@PathParam("image_name") String imageName) {
        final ImageStats[] s = collection.getAllByImageName(imageName);
        final Map<String, JsonArrayBuilder> nums = new TreeMap<>();
        nums.put("tag", Json.createArrayBuilder());
        nums.put("created_at", Json.createArrayBuilder());
        nums.put("peak_rss_bytes", Json.createArrayBuilder());
        nums.put("gc_total_ms", Json.createArrayBuilder());
        nums.put("total_build_time_ms", Json.createArrayBuilder());
        nums.put("code_area_bytes", Json.createArrayBuilder());
        nums.put("image_total_bytes", Json.createArrayBuilder());
        nums.put("image_heap_bytes", Json.createArrayBuilder());
        nums.put("resources_bytes", Json.createArrayBuilder());
        nums.put("resources_count", Json.createArrayBuilder());
        nums.put("methods_total", Json.createArrayBuilder());
        nums.put("methods_reflection", Json.createArrayBuilder());
        nums.put("methods_jni", Json.createArrayBuilder());
        nums.put("methods_reachable", Json.createArrayBuilder());
        nums.put("classes_total", Json.createArrayBuilder());
        nums.put("classes_reflection", Json.createArrayBuilder());
        nums.put("classes_jni", Json.createArrayBuilder());
        nums.put("classes_reachable", Json.createArrayBuilder());
        nums.put("fields_total", Json.createArrayBuilder());
        nums.put("fields_reflection", Json.createArrayBuilder());
        nums.put("fields_jni", Json.createArrayBuilder());
        nums.put("fields_reachable", Json.createArrayBuilder());
        for (ImageStats i : s) {
            nums.get("tag").add(i.getTag() == null ? "" : i.getTag());
            nums.get("created_at").add(i.getCreatedAt().toString());
            nums.get("peak_rss_bytes").add(i.getResourceStats().getPeakRSSBytes());
            nums.get("gc_total_ms").add((long) (i.getResourceStats().getGcTimeSeconds() * 1000));
            nums.get("total_build_time_ms").add((long) (i.getResourceStats().getTotalTimeSeconds() * 1000));
            nums.get("code_area_bytes").add(i.getSizeStats().getCodeCacheSize());
            nums.get("image_total_bytes").add(i.getSizeStats().getTotalSize());
            nums.get("image_heap_bytes").add(i.getSizeStats().getHeapSize());
            nums.get("resources_bytes").add(i.getSizeStats().getResourcesSize());
            nums.get("resources_count").add(i.getSizeStats().getResourcesCount());
            nums.get("methods_total").add(i.getTotalStats().getNumMethods());
            nums.get("methods_reflection").add(i.getReflectionStats().getNumMethods());
            nums.get("methods_jni").add(i.getJniStats().getNumMethods());
            nums.get("methods_reachable").add(i.getReachableStats().getNumMethods());
            nums.get("classes_total").add(i.getTotalStats().getNumClasses());
            nums.get("classes_reflection").add(i.getReflectionStats().getNumClasses());
            nums.get("classes_jni").add(i.getJniStats().getNumClasses());
            nums.get("classes_reachable").add(i.getReachableStats().getNumClasses());
            nums.get("fields_total").add(i.getTotalStats().getNumFields());
            nums.get("fields_reflection").add(i.getReflectionStats().getNumFields());
            nums.get("fields_jni").add(i.getJniStats().getNumFields());
            nums.get("fields_reachable").add(i.getReachableStats().getNumFields());
        }
        final JsonObjectBuilder rb = Json.createObjectBuilder();
        for (Map.Entry<String, JsonArrayBuilder> kv : nums.entrySet()) {
            rb.add(kv.getKey(), kv.getValue().build());
        }
        return Response.status(Response.Status.OK).entity(rb.build().toString()).build();
    }

    @RolesAllowed("token_read")
    @GET
    @Path("tags/distinct")
    public String[] getDistinctTags() {
        return collection.getDistinctTags();
    }

    @RolesAllowed("token_read")
    @GET
    @Path("image-names/distinct")
    public Map<Long, String> getDistinctImageNames() {
        return collection.getDistinctImageNames();
    }

    @RolesAllowed("token_read")
    @GET
    @Path("image-names/distinct/{keyword}")
    public Map<Long, String> getDistinctImageNames(@PathParam("keyword") String keyword) {
        return collection.getDistinctImageNames(keyword);
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
    @PUT
    @Path("{statId:\\d+}")
    public ImageStats updateBuildTime(@PathParam("statId") Long statId, GraalBuildInfo info) {
        if (info == null) {
            throw new WebApplicationException("GraalBuildInfo for statId " + statId + " must not be null",
                    Status.INTERNAL_SERVER_ERROR);
        }
        final ImageStats stat = collection.updateBuildTime(statId, info.getTotalBuildTimeMilis());
        if (stat == null) {
            throw new WebApplicationException("Stat with id " + statId + " not found", Status.NOT_FOUND);
        }
        return stat;
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

            return Response.status(code).entity(exceptionJson).build();
        }

    }
}
