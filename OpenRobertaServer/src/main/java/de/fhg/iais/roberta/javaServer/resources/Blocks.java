package de.fhg.iais.roberta.javaServer.resources;

import java.util.Date;
import java.util.List;

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
import com.sun.jersey.api.core.InjectParam;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.brick.Templates;
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.UserProcessor;
import de.fhg.iais.roberta.persistence.UserProgramProcessor;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.bo.User;
import de.fhg.iais.roberta.persistence.bo.UserProgram;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;

@Path("/blocks")
public class Blocks {
    private static final Logger LOG = LoggerFactory.getLogger(Blocks.class);

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final Templates templates;
    private final BrickCommunicator brickCommunicator;

    public static int signedIn = 0;
    public static int userId;

    @Inject
    public Blocks(@InjectParam SessionFactoryWrapper sessionFactoryWrapper, @InjectParam Templates templates, @InjectParam BrickCommunicator brickCommunicator) {
        this.sessionFactoryWrapper = sessionFactoryWrapper;
        this.templates = templates;
        this.brickCommunicator = brickCommunicator;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response workWithBlocks(JSONObject fullRequest) throws Exception {
        LOG.info("/blocks got: " + fullRequest);
        JSONObject response = new JSONObject();
        SessionWrapper session = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            response.put("cmd", cmd);
            if ( cmd.equals("saveP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                Program program = new ProgramProcessor().updateProgram(session, "RobertaLabTest", programName, programText);
                String rc = program != null ? "sucessful" : "ERROR - nothing persisted";
                LOG.info(rc);
                response.put("rc", rc);
            } else if ( cmd.equals("loadP") ) {
                String projectName = "RobertaLabTest";
                String programName = request.getString("name");
                Program program = new ProgramProcessor().getProgram(session, projectName, programName);
                if ( program == null ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", program.getProgramText());
                }
            } else if ( cmd.equals("runP") ) {
                String token = "1Q2W3E4R";
                String projectName = "RobertaLabTest";
                String programName = request.getString("name");
                String configurationName = ""; // TODO change frontend to supply us with the configuration name
                String message = CompilerWorkflow.execute(session, token, projectName, programName, configurationName);
                if ( message == null ) {
                    // everything is fine
                    message = this.brickCommunicator.theRunButtonWasPressed(token, programName, configurationName);
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
                List<String> programNames = new ProgramProcessor().getProgramNames(session);
                response.put("rc", "ok");
                response.put("programNames", programNames);
            } else if ( cmd.equals("deletePN") ) {
                String projectName = "RobertaLabTest";
                String programName = request.getString("name");
                int numberOfDeletedPrograms = new ProgramProcessor().deleteByName(session, projectName, programName);
                response.put("rc", "ok");
                response.put("deleted", numberOfDeletedPrograms);
            } else if ( cmd.equals("saveUser") ) {

                String userAccountName = request.getString("accountName");
                String userName = request.getString("userName");
                String userEmail = request.getString("userEmail");
                String pass = request.getString("password");
                String role = request.getString("role");

                User user = new UserProcessor().saveUser(session, userAccountName, userName, userEmail, pass, role);

                String rc = user != null ? "sucessful" : "ERROR - nothing persisted";
                LOG.info(rc);
                response.put("rc", rc);

                if ( user == null ) {
                    response.put("created", "False");
                } else {
                    response.put("userId", user.getId());
                    response.put("created", "True");
                }

            } else if ( cmd.equals("signInUser") ) {

                String userAccountName = request.getString("accountName");
                String pass = request.getString("password");
                User user = new UserProcessor().getUser(session, userAccountName, pass);
                String rc = user != null ? "sucessful" : "ERROR - nothing persisted";
                LOG.info(rc);
                response.put("rc", rc);

                if ( user == null ) {
                    response.put("exists", "False");
                } else {
                    response.put("exists", "True");
                    response.put("userId", user.getId());
                    response.put("userRole", user.getRole());
                    response.put("userAccountName", user.getAccountName());

                    userId = user.getId();
                    signedIn = 1;

                    System.out.println("Signed in!");
                }

            } else if ( cmd.equals("deleteUser") ) {

                String userAccountName = request.getString("accountName");
                int deleteValue = new UserProcessor().deleteUserProgramByName(session, userAccountName);
                String rc = deleteValue != 0 ? "sucessful" : "Nothing to delete";
                LOG.info(rc);
                response.put("rc", rc);

            } else if ( cmd.equals("saveUserP") ) {

                String programName = request.getString("name");
                String programText = request.getString("program");
                String rc;

                if ( signedIn == 1 ) {

                    UserProgram userProgram = new UserProgramProcessor().updateUserProgram(session, userId, programName, programText);
                    rc = userProgram != null ? "sucessful" : "ERROR - nothing persisted";

                } else {
                    rc = "ERROR - nothing persisted";
                }
                LOG.info(rc);
                response.put("rc", rc);

            } else if ( cmd.equals("loadUserP") ) {

                String programName = request.getString("name");
                UserProgram program = new UserProgramProcessor().getUserProgram(session, userId, programName);
                if ( program == null || signedIn == 0 ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", program.getProgramText());
                }
            } else if ( cmd.equals("deleteUserPN") ) {

                String programName = request.getString("name");
                int numberOfDeletedPrograms = new UserProgramProcessor().deleteByName(session, userId, programName);
                response.put("rc", "ok");
                response.put("deleted", numberOfDeletedPrograms);
            } else if ( cmd.equals("loadPN") ) {
                List<String> programNames = new ProgramProcessor().getProgramNames(session);
                response.put("rc", "ok");
                response.put("programNames", programNames);
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