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
        String cmd = requestEntity.getString(CMD);
        LOG.info("/pushcmd - " + cmd);
        switch ( cmd ) {
            case CMD_REGISTER:
                String token = requestEntity.getString("token");
                LOG.info("/pushcmd - agreement request for token " + token);
                boolean result = this.brickCommunicator.iAmABrickAndWantATokenToBeAgreedUpon(token);
                JSONObject response = new JSONObject().put("response", result ? "ok" : "error").put("cmd", CMD_REPEAT);
                return Response.ok(response).build();
            default:
                Thread.sleep(5000);
                JSONObject response = new JSONObject().put("cmd", CMD_REPEAT);
                return Response.ok(response).build();
                break;
        }
    }
}
