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

import com.google.inject.Inject;

import de.fhg.iais.roberta.brick.BrickCommunicator;
import de.fhg.iais.roberta.brick.CompilerWorkflow;
import de.fhg.iais.roberta.javaServer.provider.OraData;
import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.connector.DbSession;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
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

            } else if ( cmd.equals("deleteP") && httpSessionState.isUserLoggedIn() ) {
                String programName = request.getString("name");
                pp.deleteByName(programName, userId);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("loadPN") && httpSessionState.isUserLoggedIn() ) {
                JSONArray programInfo = pp.getProgramInfo(userId);
                response.put("programNames", programInfo);
                Util.addResultInfo(response, pp);

            } else if ( cmd.equals("runP") ) {
                // TODO: refactor to a Processor (?)
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
                String message = this.compilerWorkflow.execute(dbSession, token, programName, programText, configurationText);
                if ( message == null ) {
                    // everything is fine
                    message = this.brickCommunicator.theRunButtonWasPressed(token, programName, configurationName);
                }
                response.put("rc", "ok");
                response.put("data", message);
                LOG.info("running program: " + message);

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