package de.fhg.iais.roberta.javaServer.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/token")
public class TokenReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(TokenReceiver.class);

    @POST
    //@Consumes(MediaType.TEXT_PLAIN)
    //@Produces(MediaType.TEXT_PLAIN)
    public Response handle(String token) {
        LOG.info("/token, " + token);
        return Response.ok("OK").build();
    }

}
