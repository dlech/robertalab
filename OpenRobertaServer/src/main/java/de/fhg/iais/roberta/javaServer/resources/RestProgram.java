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
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;
import de.fhg.iais.roberta.util.Util;

@Path("/program")
public class RestProgram {
    private static final Logger LOG = LoggerFactory.getLogger(RestProgram.class);
    private static final String OPEN_ROBERTA_STATE = "openRobertaState";
    private static final boolean SHORT_LOG = true;

    private final SessionFactoryWrapper sessionFactoryWrapper;
    private final Templates templates;
    private final BrickCommunicator brickCommunicator;

    @Inject
    public RestProgram(
        @InjectParam SessionFactoryWrapper sessionFactoryWrapper,
        @InjectParam Templates templates,
        @InjectParam BrickCommunicator brickCommunicator) {
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
                LOG.debug("/program got: " + fullRequest.toString().substring(0, 120));
            } else {
                LOG.debug("/program got: " + fullRequest);
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
                String message = CompilerWorkflow.execute(dbSession, token, programName, programText, configurationText);
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