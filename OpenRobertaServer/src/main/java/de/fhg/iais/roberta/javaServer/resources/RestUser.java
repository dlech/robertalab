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
import de.fhg.iais.roberta.persistence.UserProcessor;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Util;

@Path("/user")
public class RestUser {
    private static final Logger LOG = LoggerFactory.getLogger(RestUser.class);

    private final BrickCommunicator brickCommunicator;

    @Inject
    public RestUser(BrickCommunicator brickCommunicator) {
        this.brickCommunicator = brickCommunicator;
    }

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
            UserProcessor up = new UserProcessor(dbSession, httpSessionState);
            if ( cmd.equals("login") ) {
                String userAccountName = request.getString("accountName");
                String password = request.getString("password");
                User user = up.getUser(userAccountName, password);
                Util.addResultInfo(response, up);
                if ( user != null ) {
                    int id = user.getId();
                    String account = user.getAccount();
                    httpSessionState.rememberLogin(id);
                    user.setLastLogin();
                    response.put("userId", id);
                    response.put("userRole", user.getRole());
                    response.put("userAccountName", account);
                    LOG.info("logon: user {} with id {} logged in", account, id);
                }

            } else if ( cmd.equals("logout") && httpSessionState.isUserLoggedIn() ) {
                httpSessionState.rememberLogout();
                response.put("rc", "ok");
                LOG.info("logout of user " + userId);

            } else if ( cmd.equals("createUser") ) {
                String account = request.getString("accountName");
                String password = request.getString("password");
                String email = request.getString("userEmail");
                String role = request.getString("role");
                up.saveUser(account, password, role, email, null);
                Util.addResultInfo(response, up);

            } else if ( cmd.equals("deleteUser") ) {
                String account = request.getString("accountName");
                String password = request.getString("password");
                up.deleteUserByAccount(account, password);
                Util.addResultInfo(response, up);

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
        Util.addFrontendInfo(response, httpSessionState, this.brickCommunicator);
        return Response.ok(response).build();
    }
}