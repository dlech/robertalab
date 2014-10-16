package de.fhg.iais.roberta.javaServer.resources;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.InjectParam;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.Templates;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;

@Path("/blocks")
public class RestBlocks {
    private static final Logger LOG = LoggerFactory.getLogger(RestBlocks.class);
    private static final String OPEN_ROBERTA_STATE = "openRobertaState";
    private static final boolean SHORT_LOG = true;

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final Templates templates;
    private final BrickCommunicator brickCommunicator;

    @Inject
    public RestBlocks(
        @InjectParam SessionFactoryWrapper sessionFactoryWrapper,
        @InjectParam Templates templates,
        @InjectParam BrickCommunicator brickCommunicator) {
        this.sessionFactoryWrapper = sessionFactoryWrapper;
        this.templates = templates;
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response workWithBlocks(@Context HttpServletRequest req, JSONObject fullRequest) throws Exception {
        if ( LOG.isDebugEnabled() ) {
            if ( SHORT_LOG ) {
                LOG.debug("/blocks got: " + fullRequest.toString().substring(0, 120));
            } else {
                LOG.debug("/blocks got: " + fullRequest);
            }
        }
        HttpSession httpSession = req.getSession(true);
        OpenRobertaSessionState httpSessionState = (OpenRobertaSessionState) httpSession.getAttribute(OPEN_ROBERTA_STATE);
        if ( httpSessionState == null ) {
            httpSessionState = OpenRobertaSessionState.init();
            httpSession.setAttribute(OPEN_ROBERTA_STATE, httpSessionState);
        }
        final int userId = httpSessionState.getUserId();
        JSONObject response = new JSONObject();
        SessionWrapper dbSession = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            if ( cmd.equals("setToken") ) {
                String token = request.getString("token");
                httpSessionState.setToken(token);
                response.put("rc", "ok");
                LOG.info("set token: ok");

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