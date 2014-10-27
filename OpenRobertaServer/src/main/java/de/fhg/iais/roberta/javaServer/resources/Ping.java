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

import com.google.inject.Inject;
import com.google.inject.name.Named;

import de.fhg.iais.roberta.util.ClientLogger;

@Path("/ping")
public class Ping {
    private static final Logger LOG = LoggerFactory.getLogger(Ping.class);
    private final String version;

    @Inject
    public Ping(@Named("version") String version) {
        this.version = version;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject request) throws Exception {
        int logLen = new ClientLogger().log(LOG, request);
        LOG.info("/ping");
        Date date = new Date();
        JSONObject data = new JSONObject().put("version", this.version).put("date", date.getTime()).put("dateAsString", date.toString()).put("logged", logLen);
        return Response.ok(data).build();
    }
}