package de.fhg.iais.roberta.javaServer.resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/saveTheBlocks")
public class SaveTheBlocks {
    private static final Logger LOG = LoggerFactory.getLogger(SaveTheBlocks.class);

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response rechnen(String request) throws Exception {
        LOG.info("/saveTheBlocks got: " + request);
        JSONObject response = new JSONObject();
        response.put("result", "save successful at server time: " + new Date());
        return Response.ok(response).build();
    }

}