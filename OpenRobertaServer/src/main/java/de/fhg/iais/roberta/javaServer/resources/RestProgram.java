package de.fhg.iais.roberta.javaServer.resources;

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

import com.google.inject.Inject;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.persistence.ConfigurationProcessor;
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.UserProgramProcessor;
import de.fhg.iais.roberta.persistence.bo.Configuration;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.persistence.util.SessionFactoryWrapper;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Util;

@Path("/program")
public class RestProgram {
    private static final Logger LOG = LoggerFactory.getLogger(RestProgram.class);

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final BrickCommunicator brickCommunicator;
    private final CompilerWorkflow compilerWorkflow;

    @Inject
    public RestProgram(SessionFactoryWrapper sessionFactoryWrapper, BrickCommunicator brickCommunicator, CompilerWorkflow compilerWorkflow) {
        this.sessionFactoryWrapper = sessionFactoryWrapper;
        this.brickCommunicator = brickCommunicator;
        this.compilerWorkflow = compilerWorkflow;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraData HttpSessionState httpSessionState, JSONObject fullRequest) throws Exception {
        new ClientLogger().log(LOG, fullRequest);
        final int userId = httpSessionState.getUserId();
        JSONObject response = new JSONObject();
        DbSession dbSession = this.sessionFactoryWrapper.getSession();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            LOG.info("command is: " + cmd);
            response.put("cmd", cmd);
            ProgramProcessor pp = new ProgramProcessor(dbSession, httpSessionState);
            UserProgramProcessor upp = new UserProgramProcessor(dbSession, httpSessionState);

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
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("shareP") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("programName");
                String userToShareName = request.getString("userToShare");
                String right = request.getString("right");
                upp.shareToUser(userId, userToShareName, programName, right);
                Util.addResultInfo(response, upp);

            } else if ( cmd.equals("deleteP") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                pp.deleteByName(programName, userId);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("loadPN") && httpSessionState.isUserLoggedIn() ) {
                JSONArray programInfo = pp.getProgramInfo(userId);
                response.put("programNames", programInfo);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("runP") ) {
                String token = httpSessionState.getToken();
                String programName = request.getString("name");
                String configurationName = request.getString("configuration");
                String programText = httpSessionState.getProgram();
                String configurationText = httpSessionState.getConfiguration();
                if ( httpSessionState.isUserLoggedIn() ) {
                    Program program = pp.getProgram(programName, userId);
                    programText = program.getProgramText();
                }
                if ( httpSessionState.isUserLoggedIn() || configurationName.equals("Standardkonfiguration") ) {
                    Configuration configuration = new ConfigurationProcessor(dbSession, httpSessionState).getConfiguration(configurationName, userId);
                    configurationText = configuration.getConfigurationText();
                }
                LOG.info("compiler workflow started for program {} and configuration {}", programName, configurationName);
                String messageKey = this.compilerWorkflow.execute(dbSession, token, programName, programText, configurationText);
                if ( messageKey == null ) {
                    // everything is fine
                    boolean wasRobotWaiting = this.brickCommunicator.theRunButtonWasPressed(token, programName, configurationName);
                    if ( wasRobotWaiting ) {
                        response.put("rc", "ok").put("message", "robot.push.run");
                    } else {
                        response.put("rc", "error").put("message", "robot.not_waiting");
                    }
                } else {
                    response.put("rc", "error").put("data", messageKey);
                }
            } else {
                LOG.error("Invalid command: " + cmd);
                response.put("rc", "error").put("message", "command.invalid");
            }
            dbSession.commit();
        } catch ( Exception e ) {
            dbSession.rollback();
            String errorTicketId = Util.getErrorTicketId();
            LOG.error("Exception. Error ticket: " + errorTicketId, e);
            response.put("rc", "error").put("message", Util.SERVER_ERROR).append("parameters", errorTicketId);
        } finally {
            if ( dbSession != null ) {
                dbSession.close();
            }
        }
        Util.addFrontendInfo(response, httpSessionState, this.brickCommunicator);
        return Response.ok(response).build();
    }
}