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

import de.fhg.iais.roberta.persistence.ProgramProcessor;
import de.fhg.iais.roberta.persistence.bo.Program;
import de.fhg.iais.roberta.persistence.connector.SessionFactoryWrapper;
import de.fhg.iais.roberta.persistence.connector.SessionWrapper;

@Path("/blocks")
public class Blocks {
    private static final Logger LOG = LoggerFactory.getLogger(Blocks.class);
    private static final Map<String, String> templates = new ConcurrentHashMap<>();

    static {
        String eins = "" //
            + "<xml id='toolbox' style='display: none'>" //
            + "  <category name='Aktion'>" //
            + "    <block type='robActions_motorDiff_on'>" //
            + "      <value name='POWER'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>30</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_motorDiff_on_for'>" //
            + "      <value name='POWER'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>30</field>" //
            + "        </block>" //
            + "      </value>" //
            + "      <value name='DISTANCE'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>10</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_motorDiff_stop'>" //
            + "    </block>" //
            + "    <block type='robActions_motorDiff_turn'>" //
            + "      <value name='POWER'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>30</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_motorDiff_turn_for'>" //
            + "      <value name='POWER'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>30</field>" //
            + "        </block>" //
            + "      </value>" //
            + "      <value name='DISTANCE'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>10</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_display'>" //
            + "      <value name='OUT'>" //
            + "        <block type='text'>" //
            + "          <field name='TEXT'>Hallo</field>" //
            + "        </block>" //
            + "      </value>" //
            + "      <value name='COL'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>0</field>" //
            + "        </block>" //
            + "      </value>" //
            + "      <value name='ROW'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>0</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_playTone'>" //
            + "      <value name='FREQUENZ'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>300</field>" //
            + "        </block>" //
            + "      </value>" //
            + "      <value name='DURATION'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>100</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robActions_brickLight'></block>" //
            + "  </category>" //
            + "  <category name='Kontrolle'>" //
            + "    <block type='robControls_wait'>" //
            + "      <value name='VALUE'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>25</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robControls_loopUntil'>" //
            + "      <value name='VALUE'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>30</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "    <block type='robControls_loopForever'>" //
            + "    </block>" //
            + "    <block type='robControls_ifElse'>" //
            + "      <value name='VALUE'>" //
            + "        <block type='math_number'>" //
            + "          <field name='NUM'>25</field>" //
            + "        </block>" //
            + "      </value>" //
            + "    </block>" //
            + "  </category>" //
            + "</xml>";
        String zwei =
            ""
                + " <xml id='toolbox' style='display: none'> "
                + "  <category name='Aktion'> "
                + "  <category name='Bewegung'> "
                + "  </category> "
                + "  <category name='Anzeige'> "
                + "  </category> "
                + "  <category name='Klang'> "
                + "  </category> "
                + "  <category name='Statusleuchte'> "
                + "  </category> "
                + "  </category> "
                + "  <category name='Sensoren'> "
                + "    <category name='Berührungssensor'> "
                + "      <block type='robSensors_touch_isPressed'>"
                + "      </block>"
                + "    </category> "
                + "    <category name='Ultraschallsensor'> "
                + "      <block type='robSensors_ultrasonic_setMode'> "
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "      <block type='robSensors_ultrasonic_getMode'>"
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "      <block type='robSensors_ultrasonic_getSample'>"
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "    </category> "
                + "    <category name='Farbsensor'> "
                + "      <block type='robSensors_colour_setMode'>"
                + "        <field name='SENSORPORT'>3</field> "
                + "      </block>"
                + "      <block type='robSensors_colour_getMode'>"
                + "        <field name='SENSORPORT'>3</field> "
                + "      </block>"
                + "      <block type='robSensors_colour_getSample'>"
                + "        <field name='SENSORPORT'>3</field> "
                + "      </block>"
                + "    </category> "
                + "    <category name='Infrarotsensor'> "
                + "      <block type='robSensors_infrared_setMode'>"
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "      <block type='robSensors_infrared_getMode'>"
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "      <block type='robSensors_infrared_getSample'>"
                + "        <field name='SENSORPORT'>4</field> "
                + "      </block>"
                + "    </category> "
                + "    <category name='Drehsensor'> "
                + "      <block type='robSensors_encoder_setMode'>"
                + "      </block>"
                + "      <block type='robSensors_encoder_getMode'>"
                + "      </block>"
                + "      <block type='robSensors_encoder_reset'>"
                + "      </block>"
                + "      <block type='robSensors_encoder_getSample'>"
                + "      </block>"
                + "    </category> "
                + "    <category name='Steintasten'> "
                + "      <block type='robSensors_key_isPressed'>"
                + "      </block>"
                + "      <block type='robSensors_key_waitForPress'>"
                + "      </block>"
                + "      <block type='robSensors_key_waitForPressAndRelease'>"
                + "      </block>"
                + "    </category> "
                + "    <category name='Kreiselsensor'> "
                + "      <block type='robSensors_gyro_setMode'>"
                + "        <field name='SENSORPORT'>2</field> "
                + "      </block>"
                + "      <block type='robSensors_gyro_getMode'>"
                + "        <field name='SENSORPORT'>2</field> "
                + "      </block>"
                + "      <block type='robSensors_gyro_reset'>"
                + "        <field name='SENSORPORT'>2</field> "
                + "      </block>"
                + "      <block type='robSensors_gyro_getSample'>"
                + "        <field name='SENSORPORT'>2</field> "
                + "      </block>"
                + "    </category> "
                + "  </category> "
                + "  <category name='Kontrolle'> "
                + "    <category name='Entscheidung'> "
                + "      <block type='controls_if'/> "
                + "    </category> "
                + "    <category name='Schleifen'> "
                + "      <block type='controls_repeat_ext'> "
                + "        <value name='TIMES'> "
                + "          <block type='math_number'> "
                + "            <field name='NUM'>10</field> "
                + "          </block> "
                + "        </value> "
                + "      </block> "
                + "      <block type='controls_whileUntil'/> "
                + "      <block type='controls_for'> "
                + "        <value name='FROM'> "
                + "          <block type='math_number'> "
                + "            <field name='NUM'>1</field> "
                + "          </block> "
                + "        </value> "
                + "        <value name='TO'> "
                + "          <block type='math_number'> "
                + "            <field name='NUM'>10</field> "
                + "          </block> "
                + "        </value> "
                + "        <value name='BY'> "
                + "          <block type='math_number'> "
                + "            <field name='NUM'>1</field> "
                + "          </block> "
                + "        </value> "
                + "      </block> "
                + "      <block type='controls_forEach'/> "
                + "      <block type='controls_flow_statements'/> "
                + "    </category> "
                + "    <category name='Tasks'> "
                + "      <block type='robControls_activity'> "
                + "        <value name='ACTIVITY'> "
                + "          <block type='variables_get'> "
                + "            <field name='VAR'>zwei</field> "
                + "          </block> "
                + "        </value> "
                + "      </block> "
                + "      <block type='robControls_start_activity'> "
                + "        <value name='ACTIVITY'> "
                + "          <block type='variables_get'> "
                + "            <field name='VAR'>zwei</field> "
                + "          </block> "
                + "        </value> "
                + "      </block> "
                + "    </category> "
                + "  </category> "
                + "  <category name='Logik'> "
                + "    <block type='logic_compare'/> "
                + "    <block type='logic_operation'/> "
                + "    <block type='logic_negate'/> "
                + "    <block type='logic_boolean'/> "
                + "    <block type='logic_null'/> "
                + "    <block type='logic_ternary'/> "
                + "  </category> "
                + "  <category name='Mathematik'> "
                + "    <block type='math_number'/> "
                + "    <block type='math_arithmetic'/> "
                + "    <block type='math_single'/> "
                + "    <block type='math_trig'/> "
                + "    <block type='math_constant'/> "
                + "    <block type='math_number_property'/> "
                + "    <block type='math_change'> "
                + "      <value name='DELTA'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>1</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='math_round'/> "
                + "    <block type='math_on_list'/> "
                + "    <block type='math_modulo'/> "
                + "    <block type='math_constrain'> "
                + "      <value name='LOW'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>1</field> "
                + "        </block> "
                + "      </value> "
                + "      <value name='HIGH'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>100</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='math_random_int'> "
                + "      <value name='FROM'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>1</field> "
                + "        </block> "
                + "      </value> "
                + "      <value name='TO'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>100</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='math_random_float'/> "
                + "  </category> "
                + "  <category name='Text'> "
                + "    <block type='text'/> "
                + "    <block type='text_join'/> "
                + "    <block type='text_append'> "
                + "      <value name='TEXT'> "
                + "        <block type='text'/> "
                + "      </value> "
                + "    </block> "
                + "    <block type='text_length'/> "
                + "    <block type='text_isEmpty'/> "
                + "    <block type='text_indexOf'> "
                + "      <value name='VALUE'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>text</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='text_charAt'> "
                + "      <value name='VALUE'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>text</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='text_getSubstring'> "
                + "      <value name='STRING'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>text</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='text_changeCase'/> "
                + "    <block type='text_trim'/> "
                // + "    <block type='text_print'/> " TODO Ausgabe Display
                + "    <block type='text_prompt'/> "
                + "  </category> "
                + "  <category name='Listen'> "
                + "    <block type='lists_create_empty'/> "
                + "    <block type='lists_create_with'/> "
                + "    <block type='lists_repeat'> "
                + "      <value name='NUM'> "
                + "        <block type='math_number'> "
                + "          <field name='NUM'>5</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='lists_length'/> "
                + "    <block type='lists_isEmpty'/> "
                + "    <block type='lists_indexOf'> "
                + "      <value name='VALUE'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>liste</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='lists_getIndex'> "
                + "      <value name='VALUE'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>liste</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='lists_setIndex'> "
                + "      <value name='LIST'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>liste</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "    <block type='lists_getSublist'> "
                + "      <value name='LIST'> "
                + "        <block type='variables_get'> "
                + "          <field name='VAR'>liste</field> "
                + "        </block> "
                + "      </value> "
                + "    </block> "
                + "  </category> "
                + "  <category name='Variablen' custom='VARIABLE'/> "
                + "  <category name='Funktionen' custom='PROCEDURE'/> "
                + "</xml> ";
        templates.put("1", eins);
        templates.put("2", zwei);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response workWithBlocks(JSONObject fullRequest) throws Exception {
        LOG.info("/blocks got: " + fullRequest);
        JSONObject response = new JSONObject();
        SessionWrapper session = SessionFactoryWrapper.getSession();
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