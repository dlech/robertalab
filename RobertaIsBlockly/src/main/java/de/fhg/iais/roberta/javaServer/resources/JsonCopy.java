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

import de.fhg.iais.roberta.javaServer.util.ClientLogger;

@Path("/copy")
public class JsonCopy
{
    private static final Logger LOG = LoggerFactory.getLogger(JsonCopy.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleGreeting(JSONObject request) throws Exception {
        int logLength = new ClientLogger().log(request);
        LOG.info("/copy");
        JSONObject copy = request.getJSONObject("data");
        copy.put("remark", "this is a copy");
        copy.put("logged", logLength);
        return Response.ok(copy).build();
    }
}