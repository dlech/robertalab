package de.fhg.iais.roberta.javaServer.resources;

import java.util.concurrent.atomic.AtomicInteger;

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

    private static final int EVERY_REQUEST = 100; // after EVERY_PING many ping requests have arrived, a log entry is written
    private static final AtomicInteger pushRequestCounterForLogging = new AtomicInteger(0);

    private static final String CMD = "cmd";
    private static final String CMD_REGISTER = "register";
    private static final String CMD_PUSH = "push";
    private static final String CMD_REPEAT = "repeat";

    private final BrickCommunicator brickCommunicator;

    @Inject
    public BrickCommand(BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject requestEntity) throws JSONException, InterruptedException {
        // - {"macaddr":"00-9A-90-00-2B-5B","cmd":"register","token":"4ESSWLRH","brickname":"Roberta01","battery":"7.2","lejosversion":"0.9.0-beta", "menuversion":"1.0.1"}
        String cmd = requestEntity.getString(CMD);
        String macaddr = null;
        String token = null;
        String brickname = null;
        String menuversion = null;
        String lejosversion = null;
        try {
            macaddr = requestEntity.getString("macaddr");
            token = requestEntity.getString("token");
            brickname = requestEntity.getString("brickname");
            menuversion = requestEntity.getString("menuversion");
            lejosversion = requestEntity.getString("lejosversion");
        } catch ( Exception e ) {
            LOG.error("Robot request aborted. Robot uses a wrong JSON: " + requestEntity);
            return Response.serverError().build();
        }
        // todo: validate version serverside
        JSONObject response;
        switch ( cmd ) {
            case CMD_REGISTER:
                LOG.info("/pushcmd - brick sends token " + token + " for registration");
                BrickCommunicationData state = new BrickCommunicationData(token, macaddr, brickname, menuversion, lejosversion);
                boolean result = this.brickCommunicator.brickWantsTokenToBeApproved(state);
                response = new JSONObject().put("response", result ? "ok" : "error").put("cmd", CMD_REPEAT);
                return Response.ok(response).build();
            case CMD_PUSH:
                int counter = pushRequestCounterForLogging.incrementAndGet();
                boolean logPush = counter % EVERY_REQUEST == 0;
                if ( logPush ) {
                    LOG.info("/pushcmd - push request for token " + token + " [count:" + counter + "]");
                }
                String command = this.brickCommunicator.brickWaitsForAServerPush(token);
                if ( command == null ) {
                    LOG.error("No valid command issued by the server as response to a push command request for token " + token);
                    return Response.serverError().build();
                } else {
                    if ( !command.equals(CMD_REPEAT) || logPush ) {
                        LOG.info("the command " + command + " is pushed to the robot [count:" + counter + "]");
                    }
                    response = new JSONObject().put(CMD, command);
                    return Response.ok(response).build();
                }
            default:
                LOG.error("Robot request aborted. Robot uses a wrong JSON: " + requestEntity);
                return Response.serverError().build();
        }
    }
}
