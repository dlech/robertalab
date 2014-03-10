package de.fhg.iais.roberta.javaServer.resources;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/blocks")
public class Blocks {
    private static final Logger LOG = LoggerFactory.getLogger(Blocks.class);
    private static final Map<String, String> programs = new ConcurrentHashMap<>();
    private static final Map<String, String> templates = new ConcurrentHashMap<>();

    static {
        String eins = "" //
            + "<xml>" //
            + "  <block type='controls_if'></block>" //
            + "  <block type='controls_repeat_ext'></block>" //
            + "  <block type='logic_compare'></block>" //
            + "  <block type='math_number'></block>" //
            + "  <block type='math_arithmetic'></block>" //
            + "  <block type='text'></block>" //
            + "  <block type='text_print'></block>" //
            + "  </xml>";
        String zwei = "" //
            + "<xml>" //
            + "  <block type='controls_if'></block>" //
            + "  <block type='logic_compare'></block>" //
            + "  <block type='math_number'></block>" //
            + "  <block type='math_arithmetic'></block>" //
            + "  <block type='text'></block>" //
            + "  </xml>";
        templates.put("1", eins);
        templates.put("2", zwei);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response workWithBlocks(JSONObject fullRequest) throws Exception {
        LOG.info("/blocks got: " + fullRequest);
        JSONObject response = new JSONObject();
        try {
            JSONObject request = fullRequest.getJSONObject("data");
            String cmd = request.getString("cmd");
            response.put("cmd", cmd);
            if ( cmd.equals("saveP") ) {
                String name = request.getString("name");
                String program = request.getString("program");
                programs.put(name, program);
                response.put("rc", "ok");
            } else if ( cmd.equals("loadP") ) {
                String name = request.getString("name");
                String program = programs.get(name);
                if ( program == null ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", program);
                }
            } else if ( cmd.equals("loadT") ) {
                String name = request.getString("name");
                String template = templates.get(name);
                if ( template == null ) {
                    response.put("rc", "error");
                    response.put("cause", "program not found");
                } else {
                    response.put("rc", "ok");
                    response.put("data", template);
                }
            } else {
                response.put("rc", "error");
                response.put("cause", "invalid command");
            }
        } catch ( Exception e ) {
            LOG.error("/blocks exception", e);
            response.put("rc", "error");
            String msg = e.getMessage();
            response.put("cause", msg == null ? "no message" : msg);
        }
        response.put("serverTime", new Date());
        return Response.ok(response).build();
    }

}