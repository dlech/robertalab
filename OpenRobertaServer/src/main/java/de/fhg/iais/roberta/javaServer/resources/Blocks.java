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

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.sun.jersey.api.core.InjectParam;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.brick.Templates;
import de.fhg.iais.roberta.persistence.ConfigurationProcessor;
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.UserProcessor;
import de.fhg.iais.roberta.persistence.bo.Configuration;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;
import de.fhg.iais.roberta.util.Util;

@Path("/blocks")
public class Blocks {
    private static final Logger LOG = LoggerFactory.getLogger(Blocks.class);
    private static final String OPEN_ROBERTA_STATE = "openRobertaState";
    private static final boolean SHORT_LOG = true;

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final Templates templates;
    private final BrickCommunicator brickCommunicator;

    @Inject
    public Blocks(@InjectParam SessionFactoryWrapper sessionFactoryWrapper, @InjectParam Templates templates, @InjectParam BrickCommunicator brickCommunicator) {
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
            ProgramProcessor pp = new ProgramProcessor(dbSession, httpSessionState);
            if ( cmd.equals("saveP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                pp.updateProgram(programName, userId, programText);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("loadP") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                Program program = pp.getProgram(programName, userId);
                if ( program != null ) {
                    response.put("data", program.getProgramText());
                }

            } else if ( cmd.equals("deletePN") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                pp.deleteByName(programName, userId);
                response.put("rc", "ok");
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("runP") ) {
                String token = httpSessionState.getToken();
                String programName = request.getString("name");
                String programText = httpSessionState.getProgram();
                String configurationName = "default"; // TODO: change frontend to supply us with the configuration name
                if ( request.has("configurationName") ) {
                    configurationName = request.getString("configurationName");
                }
                String configurationText = ""; // TODO: change frontend to supply us with the configuration xml
                if ( request.has("configurationText") ) {
                    configurationText = request.getString("configurationText");
                }
                if ( httpSessionState.isUserLoggedIn() ) {
                    Program program = pp.getProgram(programName, userId);
                    programText = program.getProgramText();
                    // TODO: change frontend
                    // Configuration configuration = new ConfigurationProcessor().getConfiguration(session, configurationName, userId);
                    // configurationText = configuration.getConfigurationText();
                }
                LOG.info("compiler workflow started for program {} and configuration {}", programName, configurationName);
                String message = CompilerWorkflow.execute(dbSession, token, programName, programText, configurationText);
                if ( message == null ) {
                    // everything is fine
                    message = this.brickCommunicator.theRunButtonWasPressed(token, programName, configurationName);
                }
                response.put("rc", "ok");
                response.put("data", message);
                LOG.info("running program: " + message);

            } else if ( cmd.equals("saveC") ) {
                String configurationName = request.getString("configurationName");
                String configurationText = request.getString("configuration");
                if ( httpSessionState.isUserLoggedIn() ) {
                    Configuration program =
                        new ConfigurationProcessor(dbSession, httpSessionState).updateConfiguration(configurationName, userId, configurationText);
                    String rc = program == null ? "ERROR" : "ok";
                    response.put("rc", rc);
                    LOG.info("saving configuration " + configurationName + " to db: " + rc);
                } else {
                    httpSessionState.setConfigurationNameAndConfiguration(configurationName, configurationText);
                    response.put("rc", "ok");
                    LOG.info("saving configuration " + configurationName + " to session: ok");
                }

            } else if ( cmd.equals("setToken") ) {
                String token = request.getString("token");
                httpSessionState.setToken(token);
                response.put("rc", "ok");
                LOG.info("set token: ok");

            } else if ( cmd.equals("loadT") ) {
                String name = request.getString("name");
                String template = this.templates.get(name);
                String rc = template == null ? "ERROR" : "ok";
                response.put("rc", rc);
                if ( template == null ) {
                    response.put("cause", "toolbox not found");
                } else {
                    response.put("data", template);
                }
                LOG.info("loading toolbox: " + rc);

            } else if ( cmd.equals("loadPN") && httpSessionState.isUserLoggedIn() ) {
                JSONArray programInfo = pp.getProgramInfo(userId);
                response.put("rc", "ok");
                response.put("programNames", programInfo);
                LOG.info("program info about " + programInfo.length() + " program(s)");

            } else if ( cmd.equals("login") ) {
                String userAccountName = request.getString("accountName");
                String pass = request.getString("password");
                User user = new UserProcessor(dbSession, httpSessionState).getUser(userAccountName, pass);

                if ( user == null ) {
                    response.put("rc", "error");
                    response.put("cause", "invalid user or invalid password");
                    LOG.info("login failed for account: " + userAccountName);
                } else {
                    response.put("rc", "ok");
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

                User user = new UserProcessor(dbSession, httpSessionState).saveUser(account, password, role, email, null);
                String rc = user == null ? "ERROR" : "ok";
                LOG.info("user created: " + rc);
                response.put("rc", rc);
                if ( user != null ) {
                    httpSessionState.rememberLogin(user.getId());
                    response.put("userId", user.getId());
                }

            } else if ( cmd.equals("deleteUser") ) {
                String account = request.getString("accountName");
                int deletedUsers = new UserProcessor(dbSession, httpSessionState).deleteUserByAccount(userId, account);
                String rc = deletedUsers == 1 ? "ok" : "ERROR";
                response.put("rc", rc);
                LOG.info("deleted " + deletedUsers + " user(s)");

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