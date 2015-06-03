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

import de.fhg.iais.roberta.ast.hardwarecheck.UsedPortsCheckVisitor;
import de.fhg.iais.roberta.ast.transformer.JaxbBlocklyProgramTransformer;
import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.codegen.lejos.Helper;
import de.fhg.iais.roberta.ev3.EV3BrickConfiguration;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.persistence.AccessRightProcessor;
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.util.DbSession;
import de.fhg.iais.roberta.persistence.util.HttpSessionState;
import de.fhg.iais.roberta.persistence.util.SessionFactoryWrapper;
import de.fhg.iais.roberta.util.AliveData;
import de.fhg.iais.roberta.util.ClientLogger;
import de.fhg.iais.roberta.util.Key;
import de.fhg.iais.roberta.util.Util;

@Path("/program")
public class ClientProgram {
    private static final Logger LOG = LoggerFactory.getLogger(ClientProgram.class);

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final BrickCommunicator brickCommunicator;
    private final CompilerWorkflow compilerWorkflow;

    @Inject
    public ClientProgram(SessionFactoryWrapper sessionFactoryWrapper, BrickCommunicator brickCommunicator, CompilerWorkflow compilerWorkflow) {
        this.sessionFactoryWrapper = sessionFactoryWrapper;
        this.brickCommunicator = brickCommunicator;
        this.compilerWorkflow = compilerWorkflow;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response command(@OraData HttpSessionState httpSessionState, JSONObject fullRequest) throws Exception {
        AliveData.rememberClientCall();
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
            AccessRightProcessor upp = new AccessRightProcessor(dbSession, httpSessionState);

            if ( cmd.equals("saveP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                boolean isShared = request.optBoolean("shared", false);
                pp.updateProgram(programName, userId, programText, true, !isShared);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("saveAsP") ) {
                String programName = request.getString("name");
                String programText = request.getString("program");
                pp.updateProgram(programName, userId, programText, true, true);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("loadP") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                Program program = pp.getProgram(programName, userId);
                if ( program != null ) {
                    response.put("data", program.getProgramText());
                }
                Util.addResultInfo(response, pp);
            } else if ( cmd.equals("checkP") ) {
                String programText = request.optString("programText");
                String configurationText = request.optString("configurationText");

                JaxbBlocklyProgramTransformer<Void> programTransformer = null;
                try {
                    programTransformer = Helper.generateProgramTransformer(programText);
                } catch ( Exception e ) {
                    LOG.error("Transformer failed", e);
                    //return Key.COMPILERWORKFLOW_ERROR_PROGRAM_TRANSFORM_FAILED;
                }
                EV3BrickConfiguration brickConfiguration = null;
                try {
                    brickConfiguration = (EV3BrickConfiguration) Helper.generateConfiguration(configurationText);
                } catch ( Exception e ) {
                    LOG.error("Generation of the configuration failed", e);
                    //return Key.COMPILERWORKFLOW_ERROR_CONFIGURATION_TRANSFORM_FAILED;
                }

                UsedPortsCheckVisitor programChecker = new UsedPortsCheckVisitor(brickConfiguration);
                int errorCounter = programChecker.check(programTransformer.getTree());
                response.put("data", Helper.jaxbToXml(Helper.astToJaxb(programChecker.getCheckedProgram())));
                response.put("errorCounter", errorCounter);
                Util.addSuccessInfo(response, Key.ROBOT_PUSH_RUN);

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

            } else if ( cmd.equals("loadPR") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                JSONArray relations = pp.getProgramRelations(programName, userId);
                response.put("relations", relations);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("runP") ) {
                String token = httpSessionState.getToken();
                String programName = request.getString("name");
                String programText = request.optString("programText");
                String configurationText = request.optString("configurationText");
                LOG.info("compiler workflow started for program {}", programName);
                Key messageKey = this.compilerWorkflow.execute(dbSession, token, programName, programText, configurationText);
                if ( messageKey == null ) {
                    // everything is fine
                    if ( token == null ) {
                        Util.addErrorInfo(response, Key.ROBOT_NOT_CONNECTED);
                    } else {
                        boolean wasRobotWaiting = this.brickCommunicator.theRunButtonWasPressed(token, programName);
                        if ( wasRobotWaiting ) {
                            Util.addSuccessInfo(response, Key.ROBOT_PUSH_RUN);
                        } else {
                            Util.addErrorInfo(response, Key.ROBOT_NOT_WAITING);
                        }
                    }
                } else {
                    response.put("rc", "error").put("message", messageKey);
                }
            } else {
                LOG.error("Invalid command: " + cmd);
                Util.addErrorInfo(response, Key.COMMAND_INVALID);
            }
            dbSession.commit();
        } catch ( Exception e ) {
            dbSession.rollback();
            String errorTicketId = Util.getErrorTicketId();
            LOG.error("Exception. Error ticket: " + errorTicketId, e);
            Util.addErrorInfo(response, Key.SERVER_ERROR).append("parameters", errorTicketId);
        } finally {
            if ( dbSession != null ) {
                dbSession.close();
            }
        }
        Util.addFrontendInfo(response, httpSessionState, this.brickCommunicator);
        return Response.ok(response).build();
    }
}