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

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Util;

@Path("/ping")
public class Ping {
    private static final Logger LOG = LoggerFactory.getLogger(Ping.class);
    private final String version;
    private final BrickCommunicator brickCommunicator;

    @Inject
    public Ping(@Named("version") String version, BrickCommunicator brickCommunicator) {
        this.version = version;
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(@OraData HttpSessionState httpSessionState, JSONObject fullRequest) throws Exception {
        int logLen = new ClientLogger().log(LOG, fullRequest);
        LOG.info("/ping");
        Date date = new Date();
        JSONObject response =
            new JSONObject().put("version", this.version).put("date", date.getTime()).put("dateAsString", date.toString()).put("logged", logLen);
        Util.addFrontendInfo(response, httpSessionState, this.brickCommunicator);
        return Response.ok(response).build();
    }
}