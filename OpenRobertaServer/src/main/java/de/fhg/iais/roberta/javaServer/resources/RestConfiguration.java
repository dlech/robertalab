package de.fhg.iais.roberta.javaServer.resources;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.persistence.ConfigurationProcessor;
import de.fhg.iais.roberta.persistence.bo.Configuration;
import de.fhg.iais.roberta.persistence.connector.DbSession;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Util;

@Path("/conf")
public class RestConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(RestConfiguration.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraData HttpSessionState httpSessionState, @OraData DbSession dbSession, JSONObject fullRequest) throws Exception {
        new ClientLogger().log(LOG, fullRequest);
        final int userId = httpSessionState.getUserId();
        JSONObject response = new JSONObject();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            ConfigurationProcessor cp = new ConfigurationProcessor(dbSession, httpSessionState);
            if ( cmd.equals("saveC") ) {
                String configurationName = request.getString("name");
                String configurationText = request.getString("configuration");
                cp.updateConfiguration(configurationName, userId, configurationText);
                Util.addResultInfo(response, cp);

            } else if ( cmd.equals("loadC") && httpSessionState.isUserLoggedIn() ) {
                String configurationName = request.getString("name");
                Configuration configuration = cp.getConfiguration(configurationName, userId);
                if ( configuration != null ) {
                    response.put("data", configuration.getConfigurationText());
                }
                Util.addResultInfo(response, cp);

            } else if ( cmd.equals("deleteC") && httpSessionState.isUserLoggedIn() ) {
                String configurationName = request.getString("name");
                cp.deleteByName(configurationName, userId);
                Util.addResultInfo(response, cp);

            } else if ( cmd.equals("loadCN") && httpSessionState.isUserLoggedIn() ) {
                JSONArray configurationInfo = cp.getConfigurationInfo(userId);
                response.put("configurationNames", configurationInfo);
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