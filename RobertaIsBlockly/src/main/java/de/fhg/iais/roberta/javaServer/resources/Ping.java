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

import de.fhg.iais.roberta.javaServer.util.ClientLogger;

@Path("/ping")
public class Ping
{
    private static final Logger LOG = LoggerFactory.getLogger(Ping.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject request) throws Exception {
        int logLength = new ClientLogger().log(request);
        LOG.info("/ping");
        Date date = new Date();
        JSONObject data = new JSONObject().put("date", date.getTime()).put("dateAsString", date.toString()).put("logged", logLength);
        return Response.ok(data).build();
    }
}