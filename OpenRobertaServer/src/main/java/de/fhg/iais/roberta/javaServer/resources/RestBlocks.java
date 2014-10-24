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

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.Templates;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.persistence.connector.DbSession;
import de.fhg.iais.roberta.util.ClientLogger;

@Path("/blocks")
public class RestBlocks {
    private static final Logger LOG = LoggerFactory.getLogger(RestBlocks.class);

    private final Templates templates;
    private final BrickCommunicator brickCommunicator;

    @Inject
    public RestBlocks(Templates templates, BrickCommunicator brickCommunicator) {
        this.templates = templates;
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraData HttpSessionState httpSessionState, @OraData DbSession dbSession, JSONObject fullRequest) throws Exception {
        new ClientLogger().log(LOG, fullRequest);
        JSONObject response = new JSONObject();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            if ( cmd.equals("setToken") ) {
                String token = request.getString("token");
                String rc = null;
                if ( this.brickCommunicator.aTokenAgreementWasSent(token) ) {
                    rc = "ok";
                    httpSessionState.setToken(token);
                    LOG.info("token " + token + " is registered in the session");
                } else {
                    rc = "error";
                }
                response.put("rc", rc);
                LOG.info("set token: " + rc);

            } else if ( cmd.equals("loadT") ) {
                String name = request.getString("name");
                String template = this.templates.get(name);
                String rc = template == null ? "error" : "ok";
                response.put("rc", rc);
                if ( template == null ) {
                    response.put("cause", "toolbox not found");
                } else {
                    response.put("data", template);
                }
                LOG.info("loading toolbox: " + rc);

            } else {
                LOG.error("Invalid command: " + cmd);
                response.put("rc", "error");
                response.put("cause", "invalid command");

            }
            dbSession.commit();
        } catch ( Exception e ) {
            dbSession.rollback();
            LOG.error("exception", e);
            response.put("rc", "error");
            String msg = e.getMessage();
            response.put("cause", msg == null ? "no message" : msg);
        } finally {
            if ( dbSession != null ) {
                dbSession.close();
            }
        }
        response.put("serverTime", new Date());
        return Response.ok(response).build();
    }
}