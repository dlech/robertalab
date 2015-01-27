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

import com.google.inject.Inject;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.Util;

@Path("/robot")
public class RestRobot {
    private static final Logger LOG = LoggerFactory.getLogger(RestRobot.class);

    private final BrickCommunicator brickCommunicator;

    @Inject
    public RestRobot(BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraData HttpSessionState httpSessionState, JSONObject fullRequest) throws Exception {
        new ClientLogger().log(LOG, fullRequest);
        final int userId = httpSessionState.getUserId();
        JSONObject response = new JSONObject();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);

            if ( cmd.equals("updateFirmware") ) {
                String token = httpSessionState.getToken();
                if ( token != null ) {
                    // everything is fine
                    boolean isPossible = this.brickCommunicator.firmwareUpdateRequested(token);
                    if ( isPossible ) {
                        Util.addSuccessInfo(response, Key.ROBOT_FIRMWAREUPDATE_POSSIBLE);
                    } else {
                        Util.addErrorInfo(response, Key.ROBOT_FIRMWAREUPDATE_IMPOSSIBLE);
                    }
                } else {
                    Util.addErrorInfo(response, Key.ROBOT_NOT_CONNECTED);
                }
            } else {
                LOG.error("Invalid command: " + cmd);
                Util.addErrorInfo(response, Key.COMMAND_INVALID);
            }
        } catch ( Exception e ) {
            String errorTicketId = Util.getErrorTicketId();
            LOG.error("Exception. Error ticket: " + errorTicketId, e);
            Util.addErrorInfo(response, Key.SERVER_ERROR).append("parameters", errorTicketId);
        }
        Util.addFrontendInfo(response, httpSessionState, this.brickCommunicator);
        return Response.ok(response).build();
    }
}