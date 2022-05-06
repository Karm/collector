package com.redhat.quarkus.mandrel.collector.report;

import com.redhat.quarkus.mandrel.collector.access.TokenRepository;
import com.redhat.quarkus.mandrel.collector.access.model.Token;
import com.redhat.quarkus.mandrel.collector.report.model.Report;
import io.quarkus.logging.Log;

import javax.annotation.security.PermitAll;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.security.NoSuchAlgorithmException;

import static com.redhat.quarkus.mandrel.collector.access.TokenRepository.hash;
import static com.redhat.quarkus.mandrel.collector.access.TokenResource.tokenPattern;

/**
 * DEMO DEMO DEMO
 */
@Path("/api/report")
public class ReportResource {

    @Inject
    TokenRepository tokenRepository;

    @POST
    @PermitAll
    @Path("/test")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //@Transactional - when it does some writing...
    // DEMO
    // Implement auth provider - this is unnecessarily late in the game to check the token
    public Response testReport(@HeaderParam("token") String token, Report report) throws NoSuchAlgorithmException {
        if (token == null || !tokenPattern.matcher(token).matches()) {
            return Response.serverError().status(Response.Status.UNAUTHORIZED).build();
        }
        final Token t = tokenRepository.find("tokenHash", hash(token)).firstResult();
        if (t == null) {
            return Response.serverError().status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.status(Response.Status.CREATED).entity(report).build();
    }
}
