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
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.UserProcessor;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;

@Path("/blocks")
public class Blocks {
    private static final Logger LOG = LoggerFactory.getLogger(Blocks.class);
    private static final String OPEN_ROBERTA_STATE = "openRobertaState";

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
        LOG.info("/blocks got: " + fullRequest);
        HttpSession httpSession = req.getSession(true);
        OpenRobertaState openRobertaState = (OpenRobertaState) httpSession.getAttribute(OPEN_ROBERTA_STATE);
        if ( openRobertaState == null ) {
            openRobertaState = OpenRobertaState.init();
            httpSession.setAttribute(OPEN_ROBERTA_STATE, openRobertaState);
        }
        final int userId = openRobertaState.getUserId();
        System.out.println("userId: " + userId);
        JSONObject response = new JSONObject();
        SessionWrapper session = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            response.put("cmd", cmd);
            if ( cmd.equals("saveP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                Program program = new ProgramProcessor().updateProgram(session, programName, userId, programText);
                String rc = program != null ? "sucessful" : "ERROR - nothing persisted";
                LOG.info(rc);
                response.put("rc", rc);

            } else if ( cmd.equals("loadP") ) {
                String programName = request.getString("name");
                Program program = new ProgramProcessor().getProgram(session, programName, userId);
                if ( program == null ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", program.getProgramText());
                }

            } else if ( cmd.equals("runP") ) {
                String token = "1Q2W3E4R";
                String programName = request.getString("name");
                String brickConfigurationName = "default"; // TODO: change frontend to supply us with the configuration name
                String brickConfigurationAsXmlString = ""; // TODO: change frontend to supply us with the configuration name
                Program program = new ProgramProcessor().getProgram(session, programName, userId);
                LOG.info("compiler workflow started for program {} and brick configuration {}", programName, brickConfigurationName);
                String message = CompilerWorkflow.execute(session, token, programName, program.getProgramText(), brickConfigurationAsXmlString);
                if ( message == null ) {
                    // everything is fine
                    message = this.brickCommunicator.theRunButtonWasPressed(token, programName, brickConfigurationName);
                }
                response.put("rc", "ok");
                response.put("data", message);

            } else if ( cmd.equals("loadT") ) {
                String name = request.getString("name");
                String template = this.templates.get(name);
                if ( template == null ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", template);
                }

            } else if ( cmd.equals("loadPN") ) {
                JSONArray programInfo = new ProgramProcessor().getProgramInfo(session, userId);
                response.put("rc", "ok");
                response.put("programNames", programInfo);

            } else if ( cmd.equals("deletePN") ) {
                String programName = request.getString("name");
                int numberOfDeletedPrograms = new ProgramProcessor().deleteByName(session, programName, userId);
                response.put("rc", "ok");
                response.put("deleted", numberOfDeletedPrograms);

            } else if ( cmd.equals("saveUser") ) {
                String account = request.getString("accountName");
                String password = request.getString("password");
                String email = request.getString("userEmail");
                String role = request.getString("role");

                User user = new UserProcessor().saveUser(session, account, password, role, email, null);
                String rc = user == null ? "ERROR" : "sucessful";
                LOG.info("result of create a new user: " + rc);
                response.put("rc", rc);
                if ( user == null ) {
                    response.put("created", "False");
                } else {
                    openRobertaState.setUserId(user.getId());
                    response.put("userId", user.getId());
                    response.put("created", "True");
                }

            } else if ( cmd.equals("signInUser") ) {
                String userAccountName = request.getString("accountName");
                String pass = request.getString("password");
                User user = new UserProcessor().getUser(session, userAccountName, pass);
                LOG.info("result of login: " + (user == null ? "ERROR" : "sucessful"));

                if ( user == null ) {
                    response.put("exists", "False");
                } else {
                    response.put("exists", "True");
                    int id = user.getId();
                    String account = user.getAccount();
                    openRobertaState.setUserId(id);
                    user.setLastLogin();
                    response.put("userId", id);
                    response.put("userRole", user.getRole());
                    response.put("userAccountName", account);
                    LOG.info("user {} with id {} logged in", account, id);
                }

            } else if ( cmd.equals("deleteUser") ) {
                String account = request.getString("accountName");
                int deleteValue = new UserProcessor().deleteUserProgramByName(session, account);
                String rc = deleteValue != 0 ? "sucessful" : "Nothing to delete";
                LOG.info(rc);
                response.put("rc", rc);

            } else {
                LOG.error("Invalid /blocks command: " + cmd);
                response.put("rc", "error");
                response.put("cause", "invalid command");

            }
            session.commit();
        } catch ( Exception e ) {
            session.rollback();
            LOG.error("/blocks exception", e);
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