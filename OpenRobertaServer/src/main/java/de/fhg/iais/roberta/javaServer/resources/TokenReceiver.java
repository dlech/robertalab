package de.fhg.iais.roberta.javaServer.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/token")
public class TokenReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(TokenReceiver.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject request) {
        LOG.info("/token - " + request);
        return Response.ok("OK").build();
    }

}
