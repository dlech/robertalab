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

@Path("/pushcmd")
public class BrickCommand {
    private static final Logger LOG = LoggerFactory.getLogger(BrickCommand.class);

    // brickdata + cmds send to server
    private static final String CMD_REGISTER = "register";
    private static final String CMD_PUSH = "push";

    // cmds brick receives from server
    private static final String CMD_REPEAT = "repeat";
    private static final String CMD_ABORT = "abort";
    private static final String CMD_UPDATE = "update";
    private static final String CMD_DOWNLOAD = "download";
    private static final String CMD_CONFIGURATION = "configuration";

    public BrickCommand() {
        //
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject requestEntity) throws JSONException, InterruptedException {
        LOG.info("/pushcmd - " + requestEntity);
        Thread.sleep(5000);
        JSONObject response = new JSONObject().put("cmd", CMD_REPEAT);
        return Response.ok(response).build();
    }
}
