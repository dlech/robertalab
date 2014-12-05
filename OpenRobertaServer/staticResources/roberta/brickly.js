$(document).ready(WRAP.fn3(init, 'brickly init EV3'));

function init() {
    COMM.json("/blocks", {
        "cmd" : "loadT",
        "name" : "brickEV3"
    }, injectBrickly);
};

/**
 * Inject Brickly with initial toolbox
 * 
 * @param {response}
 *            toolbox
 */
function injectBrickly(toolbox) {
    response(toolbox);
    if (toolbox.rc === 'ok') {
        Blockly.inject(document.body, {
            path : '/blockly/',
            toolbox : toolbox.data,
            trashcan : true,
            save : true,
            back : true,
            check : true,
            scrollbars : true
        });

        // should this come from the server?
        var text = "<block_set xmlns='http://de.fhg.iais.roberta.blockly'>" +  "<instance x='75' y='75'>" + "<block type='robBrick_EV3-Brick'>"
                + "<value name='S1'>" + "<block type='robBrick_touch'></block>" + "</value>" + "<value name='S4'>"
                + "<block type='robBrick_ultrasonic'></block>" + "</value>" + "<value name='MB'>" + "<block type='robBrick_motor_big'>"
                + "<field name='MOTOR_REGULATION'>TRUE</field>" + "<field name='MOTOR_REVERSE'>OFF</field>" + "<field name='MOTOR_DRIVE'>RIGHT</field>"
                + "</block>" + "</value>" + "<value name='MC'>" + "<block type='robBrick_motor_big'>" + "<field name='MOTOR_REGULATION'>TRUE</field>"
                + "<field name='MOTOR_REVERSE'>OFF</field>" + "<field name='MOTOR_DRIVE'>LEFT</field>" + "</block>" + "</value>" + "</block>" + "</instance>"
                + "</block_set>";
        var xml = Blockly.Xml.textToDom(text);
        Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, xml);
    }
}

function response(result) {
    LOG.info('result from server: ' + JSON.stringify(result));
};

function saveToServer(name) {
    var xml = Blockly.Xml.workspaceToDom(Blockly.mainWorkspace);
    var xml_text = Blockly.Xml.domToText(xml);
    LOG.info('save brick configuration ' + name);
    COMM.json("/conf", {
        "cmd" : "saveC",
        "name" : name,
        "configuration" : xml_text
    });
}

function loadToolbox(toolbox) {
    COMM.json("/blocks", {
        "cmd" : "loadT",
        "name" : toolbox
    }, showToolbox);
}

function checkProgram() {
    // TODO do we need this here?
}

function back() {
    parent.switchToBlockly();
}
