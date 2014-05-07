package de.fhg.iais.roberta.javaServer.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/error")
public class ErrorMessageReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(ErrorMessageReceiver.class);

    @POST
    //@Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public Response handle(String errorMessage) {
        LOG.info("/error, " + errorMessage);
        return Response.ok().build();
    }

}
