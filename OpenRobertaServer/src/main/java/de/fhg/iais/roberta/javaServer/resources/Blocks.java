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
        OpenRobertaState openRobertaState = (OpenRobertaState) httpSession.getAttribute(OPEN_ROBERTA_STATE);
        if ( openRobertaState == null ) {
            openRobertaState = OpenRobertaState.init();
            httpSession.setAttribute(OPEN_ROBERTA_STATE, openRobertaState);
        }
        final int userId = openRobertaState.getUserId();
        JSONObject response = new JSONObject();
        SessionWrapper session = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            if ( cmd.equals("saveP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                if ( openRobertaState.isUserLoggedIn() ) {
                    Program program = new ProgramProcessor().updateProgram(session, programName, userId, programText);
                    String rc = program == null ? "ERROR" : "ok";
                    response.put("rc", rc);
                    LOG.info("saving program to db: " + rc);
                } else {
                    openRobertaState.setProgramNameAndProgramText(programName, programText);
                    response.put("rc", "ok");
                    LOG.info("saving program to session: ok");
                }

            } else if ( cmd.equals("saveC") ) {
                String configurationName = request.getString("configurationName");
                String configurationText = request.getString("configuration");
                if ( openRobertaState.isUserLoggedIn() ) {
                    Configuration program = new ConfigurationProcessor().updateConfiguration(session, configurationName, userId, configurationText);
                    String rc = program == null ? "ERROR" : "ok";
                    response.put("rc", rc);
                    LOG.info("saving configuration to db: " + rc);
                } else {
                    openRobertaState.setConfigurationNameAndConfiguration(configurationName, configurationText);
                    response.put("rc", "ok");
                    LOG.info("saving configuration to session: ok");
                }

            } else if ( cmd.equals("loadP") && openRobertaState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                Program program = new ProgramProcessor().getProgram(session, programName, userId);
                String rc = program == null ? "ERROR" : "ok";
                response.put("rc", rc);
                if ( program == null ) {
                    response.put("cause", "program not found");
                } else {
                    response.put("data", program.getProgramText());
                }
                LOG.info("loading program: " + rc);

            } else if ( cmd.equals("runP") ) {
                String token = "1Q2W3E4R"; // TODO: change frontend to supply us with the token
                if ( request.has("token") ) {
                    token = request.getString("token");
                }
                String programName = request.getString("name");
                String programText = ""; // TODO: change frontend to supply us with the program xml
                if ( request.has("program") ) {
                    programText = request.getString("program");
                }
                String configurationName = "default"; // TODO: change frontend to supply us with the configuration name
                if ( request.has("configurationName") ) {
                    configurationName = request.getString("configurationName");
                }
                String configurationText = ""; // TODO: change frontend to supply us with the configuration xml
                if ( request.has("configurationText") ) {
                    configurationText = request.getString("configurationText");
                }
                if ( openRobertaState.isUserLoggedIn() ) {
                    Program program = new ProgramProcessor().getProgram(session, programName, userId);
                    programText = program.getProgramText();
                    // TODO: change frontend
                    // Configuration configuration = new ConfigurationProcessor().getConfiguration(session, configurationName, userId);
                    // configurationText = configuration.getConfigurationText();
                }
                LOG.info("compiler workflow started for program {} and configuration {}", programName, configurationName);
                String message = CompilerWorkflow.execute(session, token, programName, programText, configurationText);
                if ( message == null ) {
                    // everything is fine
                    message = this.brickCommunicator.theRunButtonWasPressed(token, programName, configurationName);
                }
                response.put("rc", "ok");
                response.put("data", message);
                LOG.info("running program: " + message);

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

            } else if ( cmd.equals("loadPN") && openRobertaState.isUserLoggedIn() ) {
                JSONArray programInfo = new ProgramProcessor().getProgramInfo(session, userId);
                response.put("rc", "ok");
                response.put("programNames", programInfo);
                LOG.info("program info about " + programInfo.length() + " program(s)");

            } else if ( cmd.equals("deletePN") && openRobertaState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                int numberOfDeletedPrograms = new ProgramProcessor().deleteByName(session, programName, userId);
                response.put("rc", "ok");
                response.put("deleted", numberOfDeletedPrograms);
                LOG.info("deleted " + numberOfDeletedPrograms + " program(s)");

            } else if ( cmd.equals("saveUser") ) {
                String account = request.getString("accountName");
                String password = request.getString("password");
                String email = request.getString("userEmail");
                String role = request.getString("role");

                User user = new UserProcessor().saveUser(session, account, password, role, email, null);
                String rc = user == null ? "ERROR" : "ok";
                LOG.info("user created: " + rc);
                response.put("rc", rc);
                if ( user != null ) {
                    openRobertaState.setUserId(user.getId());
                    response.put("userId", user.getId());
                    response.put("created", "True");
                }

            } else if ( cmd.equals("signInUser") ) {
                String userAccountName = request.getString("accountName");
                String pass = request.getString("password");
                User user = new UserProcessor().getUser(session, userAccountName, pass);

                if ( user == null ) {
                    response.put("exists", "False");
                    LOG.info("login: ERROR");
                } else {
                    response.put("exists", "True");
                    int id = user.getId();
                    String account = user.getAccount();
                    openRobertaState.setUserId(id);
                    user.setLastLogin();
                    response.put("userId", id);
                    response.put("userRole", user.getRole());
                    response.put("userAccountName", account);
                    LOG.info("logon: user {} with id {} logged in", account, id);
                }

            } else if ( cmd.equals("deleteUser") ) {
                String account = request.getString("accountName");
                int deletedUsers = new UserProcessor().deleteUserByAccount(session, userId, account);
                String rc = deletedUsers == 1 ? "ok" : "ERROR";
                response.put("rc", rc);
                LOG.info("deleted " + deletedUsers + " user(s)");

            } else {
                LOG.error("Invalid command: " + cmd);
                response.put("rc", "error");
                response.put("cause", "invalid command");

            }
            session.commit();
        } catch ( Exception e ) {
            session.rollback();
            LOG.error("exception", e);
            response.put("rc", "error");
            String msg = e.getMessage();
            response.put("cause", msg == null ? "no message" : msg);
        } finally {
            if ( session != null ) {
                session.close();
            }
        }
        response.put("serverTime", new Date());
        return Response.ok(response).build();
    }
}