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

import de.fhg.iais.roberta.javaServer.provider.OraSessionState;
import de.fhg.iais.roberta.persistence.ConfigurationProcessor;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Util;

@Path("/conf")
public class RestConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RestConfiguration.class);
    private static final String OPEN_ROBERTA_STATE = "openRobertaState";
    private static final boolean SHORT_LOG = true;

    private final SessionFactoryWrapper sessionFactoryWrapper;

    @Inject
    public RestConfiguration(SessionFactoryWrapper sessionFactoryWrapper) {
        this.sessionFactoryWrapper = sessionFactoryWrapper;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraSessionState OpenRobertaSessionState httpSessionState, JSONObject fullRequest) throws Exception {
        int logLength = new ClientLogger().log(fullRequest);
        if ( LOG.isDebugEnabled() ) {
            if ( SHORT_LOG ) {
                LOG.debug("/conf got: " + fullRequest.toString().substring(0, 120));
            } else {
                LOG.debug("/conf got: " + fullRequest);
            }
        }
        final int userId = httpSessionState.getUserId();
        JSONObject response = new JSONObject();
        SessionWrapper dbSession = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            ConfigurationProcessor cp = new ConfigurationProcessor(dbSession, httpSessionState);
            if ( cmd.equals("saveC") ) {
                String configurationName = request.getString("configurationName");
                String configurationText = request.getString("configuration");
                cp.updateConfiguration(configurationName, userId, configurationText);
                Util.addResultInfo(response, cp);

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