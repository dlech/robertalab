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
import com.sun.jersey.api.core.InjectParam;

import de.fhg.iais.roberta.brick.BrickCommunicator;

@Path("/token")
public class TokenReceiver {
    private static final Logger LOG = LoggerFactory.getLogger(TokenReceiver.class);

    private final BrickCommunicator brickCommunicator;

    @Inject
    public TokenReceiver(@InjectParam BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handle(JSONObject requestEntity) throws JSONException {
        String token = requestEntity.getString("token");
        LOG.info("/token - agreement request for token " + token);
        boolean result = this.brickCommunicator.iAmABrickAndWantATokenToBeAgreedUpon(token);
        JSONObject response = new JSONObject().put("Response", result ? "OK" : "error");
        return Response.ok(response).build();
    }

}
