package de.fhg.iais.roberta.javaServer.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import de.fhg.iais.roberta.brick.BrickCommunicationData;
import de.fhg.iais.roberta.brick.BrickCommunicator;

@Path("/pushcmd")
public class BrickCommand {
    private static final Logger LOG = LoggerFactory.getLogger(BrickCommand.class);

    // brickdata + cmds send to server
    private static final String CMD = "cmd";
    private static final String CMD_REGISTER = "register";
    private static final String CMD_PUSH = "push";

    // cmds brick receives from server
    private static final String CMD_REPEAT = "repeat";
    private static final String CMD_ABORT = "abort";
    private static final String CMD_UPDATE = "update";
    private static final String CMD_DOWNLOAD = "download";
    private static final String CMD_CONFIGURATION = "configuration";

    private final BrickCommunicator brickCommunicator;

    @Inject
    public BrickCommand(BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject requestEntity) throws JSONException, InterruptedException {
        // - {"macaddr":"00-9A-90-00-2B-5B","cmd":"register","token":"4ESSWLRH","brickname":"Roberta01","battery":"7.2","version":"1.0.1"}
        String cmd = requestEntity.getString(CMD);
        LOG.info("/pushcmd - " + cmd);
        String macaddr = null;
        String token = null;
        String brickname = null;
        String battery = null;
        String version = null;
        try {
            macaddr = requestEntity.getString("macaddr");
            token = requestEntity.getString("token");
            brickname = requestEntity.getString("brickname");
            battery = requestEntity.getString("battery");
            version = requestEntity.getString("version");
        } catch ( Exception e ) {
            LOG.error("Robot request aborted. Robot uses a wrong JSON: " + requestEntity);
            return Response.serverError().build();
        }
        // todo: validate version serverside
        JSONObject response;
        switch ( cmd ) {
            case CMD_REGISTER:
                LOG.info("/pushcmd - brick sends token " + token + " for registration");
                BrickCommunicationData state = new BrickCommunicationData(token, macaddr, brickname, version);
                boolean result = this.brickCommunicator.brickWantsTokenToBeApproved(state);
                response = new JSONObject().put("response", result ? "ok" : "error").put("cmd", CMD_REPEAT);
                return Response.ok(response).build();
            case CMD_PUSH:
                LOG.info("/pushcmd - push request for token " + token);
                String command = this.brickCommunicator.brickWaitsForAServerPush(token);
                if ( command == null ) {
                    LOG.error("No valid command issued by the server as response to a oush command request for token " + token);
                    return Response.serverError().build();
                } else {
                    LOG.info("the command " + command + " is pushed to the robot");
                    response = new JSONObject().put("cmd", command);
                    return Response.ok(response).build();
                }
            default:
                LOG.error("Robot request aborted. Robot uses a wrong JSON: " + requestEntity);
                return Response.serverError().build();
        }
    }
}
