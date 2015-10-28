/**
 * @fileoverview Blocks for Thymio.
 * @author fabian@hahn.graphics (Fabian Hahn)
 */
'use strict';

goog.provide('Blockly.Blocks.thymio');

goog.require('Blockly.Blocks');

/**
 * Common HSV hue for all blocks in this category.
 */
Blockly.Blocks.thymio.ACTUATORS_HUE = 60;
Blockly.Blocks.thymio.LEDS_HUE = 160;
Blockly.Blocks.thymio.SENSORS_HUE = 90;
Blockly.Blocks.thymio.EVENTS_HUE = 10;

/*
Blockly.Blocks.colour.HUE = 20;
Blockly.Blocks.loops.HUE = 120;
Blockly.Blocks.texts.HUE = 160;
Blockly.Blocks.logic.HUE = 210;
Blockly.Blocks.math.HUE = 230;
Blockly.Blocks.lists.HUE = 260;
Blockly.Blocks.procedures.HUE = 290;
Blockly.Blocks.variables.HUE = 330;
*/

Blockly.Blocks['thymio_when'] = {
	/**
	 * Block for Thymio when conditions.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_LOGIC_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_WHEN_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_WHEN_TOOLTIP);
		this.setPreviousStatement(true);
	    this.setNextStatement(true);
		
		this.appendValueInput('WHEN').setCheck('Boolean').appendField('when');
		this.appendStatementInput('DO').appendField('do');
	}
};

Blockly.Blocks['thymio_for'] = {
	/**
	 * Block for Thymio count loops.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_LOOPS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_FOR_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_FOR_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var variableField = new Blockly.FieldTextInput('i');
		var fromField = new Blockly.FieldTextInput('1', Blockly.FieldTextInput.numberValidator);
		var toField = new Blockly.FieldTextInput('10', Blockly.FieldTextInput.numberValidator);

		this.appendDummyInput().appendField('for').appendField(variableField, 'ITER').appendField('from').appendField(fromField, 'FROM').appendField('to').appendField(toField, 'TO');
		this.appendStatementInput('DO').appendField('do');
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('ITER')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string} oldName Previous name of variable.
	 * @param {string} newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('ITER'))) {
			this.setFieldValue(newName, 'ITER');
		}
	}
};

Blockly.Blocks['thymio_subroutine_define'] = {
	/**
	 * Block to define Thymio subroutines
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.procedures.HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SUBROUTINE_DEFINE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SUBROUTINE_DEFINE_TOOLTIP);

		var nameField = new Blockly.FieldTextInput('name', Blockly.Procedures.rename);

		this.appendDummyInput().appendField('subroutine').appendField(nameField, 'NAME');
		this.appendStatementInput('STACK');
	},
	/**
	 * Dispose of any callers.
	 * 
	 * @this Blockly.Block
	 */
	dispose : function()
	{
		var name = this.getFieldValue('NAME');
		Blockly.Procedures.disposeCallers(name, this.workspace);
		// Call parent's destructor.
		this.constructor.prototype.dispose.apply(this, arguments);
	},

	/**
	 * Return the signature of this procedure definition.
	 * 
	 * @return {!Array} Tuple containing three elements: - the name of the
	 *         defined procedure, - a list of all its arguments, - that it DOES
	 *         NOT have a return value.
	 * @this Blockly.Block
	 */
	getProcedureDef : function()
	{
		return [this.getFieldValue('NAME'), [], false];
	},
	callType_ : 'procedures_callnoreturn'
};

Blockly.Blocks['thymio_event_button'] = {
	/**
	 * Block for Thymio button event.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_BUTTON_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_BUTTON_TOOLTIP);

		var buttonDropdown = new Blockly.FieldDropdown([['center', 'button.center'], ['forward', 'button.forward'], ['backward', 'button.backward'], ['left', 'button.left'], ['right', 'button.right']]);
		var modeDropdown = new Blockly.FieldDropdown([['touched', 'PRESS'], ['released', 'RELEASE']]);
		
		this.appendDummyInput().appendField('on').appendField(buttonDropdown, 'BUTTON').appendField('button').appendField(modeDropdown, 'MODE');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_prox'] = {
	/**
	 * Block for Thymio proximity event.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_PROX_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_PROX_TOOLTIP);

		var sensorList = [];
		sensorList.push(['front left', 'prox.horizontal[0]']);
		sensorList.push(['front left/middle', 'prox.horizontal[1]']);
		sensorList.push(['front middle', 'prox.horizontal[2]']);
		sensorList.push(['front right/middle', 'prox.horizontal[3]']);
		sensorList.push(['front right', 'prox.horizontal[4]']);
		sensorList.push(['rear left', 'prox.horizontal[5]']);
		sensorList.push(['rear right', 'prox.horizontal[6]']);
		
		var sensorDropdown = new Blockly.FieldDropdown(sensorList);
		var modeDropdown = new Blockly.FieldDropdown([['proximity', 'BLOCK'], ['no proximity', 'CLEAR']]);
		
		this.appendDummyInput().appendField('on').appendField(sensorDropdown, 'SENSOR').appendField('sensor detecting').appendField(modeDropdown, 'MODE');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_prox_ground'] = {
	/**
	 * Block for Thymio ground proximity event.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_PROX_GROUND_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_PROX_GROUND_TOOLTIP);

		var sensorList = [];
		sensorList.push(['left', 'prox.ground.delta[0]']);
		sensorList.push(['right', 'prox.ground.delta[1]']);
		
		var sensorDropdown = new Blockly.FieldDropdown(sensorList);
		var modeDropdown = new Blockly.FieldDropdown([['black', 'BLACK'], ['white', 'WHITE'], ['proximity', 'PROX'], ['no proximity', 'NOPROX']]);
		
		this.appendDummyInput().appendField('on').appendField(sensorDropdown, 'SENSOR').appendField('ground sensor detecting').appendField(modeDropdown, 'MODE');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_shock'] = {
	/**
	 * Block for Thymio shock events.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_SHOCK_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_SHOCK_TOOLTIP);
		
		this.appendDummyInput().appendField('on shock detected');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_timer'] = {
	/**
	 * Block for Thymio events.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_TIMER_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_TIMER_TOOLTIP);
		
		var eventList = [];		
		eventList.push(['first', 'timer0']);
		eventList.push(['second', 'timer1']);

		var dropdown = new Blockly.FieldDropdown(eventList);
		
		this.appendDummyInput().appendField('on').appendField(dropdown, 'EVENT').appendField('timer expired');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_sound'] = {
	/**
	 * Block for Thymio sound events.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_SOUND_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_SOUND_TOOLTIP);
		
		var eventList = [];		
		eventList.push(['intensity above threshold', 'mic']);
		eventList.push(['finished playing', 'sound.finished']);

		var dropdown = new Blockly.FieldDropdown(eventList);
		
		this.appendDummyInput().appendField('on sound').appendField(dropdown, 'EVENT');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_receive'] = {
	/**
	 * Block for Thymio signal events.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_RECEIVE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_RECEIVE_TOOLTIP);
		
		var eventList = [];		
		eventList.push(['IR communication', 'prox.comm']);
		eventList.push(['remote control signal', 'rc5']);

		var dropdown = new Blockly.FieldDropdown(eventList);
		
		this.appendDummyInput().appendField('on').appendField(dropdown, 'EVENT').appendField('received');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_event_update'] = {
	/**
	 * Block for Thymio update events.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.EVENTS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_EVENT_UPDATED_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_EVENT_UPDATED_TOOLTIP);
		
		var eventList = [];		
		eventList.push(['button values', 'buttons']);
		eventList.push(['proximity sensors', 'prox']);
		eventList.push(['temperature', 'temperature']);
		eventList.push(['accelerometer', 'acc']);
		eventList.push(['motor', 'motor']);

		var dropdown = new Blockly.FieldDropdown(eventList);
		
		this.appendDummyInput().appendField('on').appendField(dropdown, 'EVENT').appendField('updated');
		this.appendStatementInput('HANDLER');
	}
};

Blockly.Blocks['thymio_led'] = {
	/**
	 * Block to set Thymio LEDs.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var dropdown = new Blockly.FieldDropdown([['top', 'leds.top'], ['bottom left', 'leds.bottom.left'], ['bottom right', 'leds.bottom.right']]);
		this.appendDummyInput().appendField('set').appendField(dropdown, 'LED').appendField('led to').appendField(new Blockly.FieldColour('#ff0000'), 'COLOR');
	}
};

Blockly.Blocks['thymio_led_rgb'] = {
	/**
	 * Block to set Thymio LEDs by RGB values.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_RGB_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_RGB_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var dropdown = new Blockly.FieldDropdown([['top', 'leds.top'], ['bottom left', 'leds.bottom.left'], ['bottom right', 'leds.bottom.right']]);
		this.appendValueInput('RED').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set').appendField(dropdown, 'LED').appendField('led to red');
		this.appendValueInput('GREEN').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('green');
		this.appendValueInput('BLUE').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('blue');
	}
};

Blockly.Blocks['thymio_led_circle'] = {
	/**
	 * Block to set Thymio circle leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_CIRCLE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_CIRCLE_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		this.appendValueInput('CIRCLE0').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set circle leds to forward');
		this.appendValueInput('CIRCLE1').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('forward right');
		this.appendValueInput('CIRCLE2').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('right');
		this.appendValueInput('CIRCLE3').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('backward right');
		this.appendValueInput('CIRCLE4').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('backward');
		this.appendValueInput('CIRCLE5').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('backward left');
		this.appendValueInput('CIRCLE6').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('left');
		this.appendValueInput('CIRCLE7').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('forward left');
	}
};

Blockly.Blocks['thymio_led_prox'] = {
	/**
	 * Block to set Thymio proximity leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_PROX_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_PROX_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendValueInput('PROX0').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set horizontal proximity leds to front left');
		this.appendValueInput('PROX1').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('front left/middle');
		this.appendValueInput('PROX2').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('right');
		this.appendValueInput('PROX3').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('front middle');
		this.appendValueInput('PROX4').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('front right/middle');
		this.appendValueInput('PROX5').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('front right');
		this.appendValueInput('PROX6').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('rear left');
		this.appendValueInput('PROX7').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('rear right');
	}
};


Blockly.Blocks['thymio_led_prox_ground'] = {
	/**
	 * Block to set Thymio proximity leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_PROX_GROUND_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_PROX_GROUND_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendValueInput('PROX0').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set ground proximity leds to left');
		this.appendValueInput('PROX1').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('right');
	}
};

Blockly.Blocks['thymio_led_button'] = {
	/**
	 * Block to set Thymio button leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_BUTTON_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_BUTTON_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendValueInput('FORWARD').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set button leds to forward');
		this.appendValueInput('RIGHT').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('right');
		this.appendValueInput('BACKWARD').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('backward');
		this.appendValueInput('LEFT').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('left');
	}
};

Blockly.Blocks['thymio_led_temperature'] = {
	/**
	 * Block to set Thymio temperature leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_TEMPERATURE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_TEMPERATURE_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendValueInput('RED').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set temperature leds to red');
		this.appendValueInput('BLUE').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('blue');
	}
};

Blockly.Blocks['thymio_led_rc_sound'] = {
	/**
	 * Block to set Thymio rc and sound leds
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_RC_SOUND_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_RC_SOUND_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		var dropdown = new Blockly.FieldDropdown([['remote control', 'leds.rc'], ['microphone', 'leds.sound']]);
		
		this.appendValueInput('INTENSITY').setCheck('Number').setAlign(Blockly.ALIGN_RIGHT).appendField('set').appendField(dropdown, 'LED').appendField('led to');
	}
};

Blockly.Blocks['thymio_led_off'] = {
	/**
	 * Block to turn off Thymio LEDs.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColour(Blockly.Blocks.thymio.LEDS_HUE);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_LED_OFF_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_LED_OFF_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		var leds = [];
		leds.push(['top led', 'leds.top']);
		leds.push(['bottom left led', 'leds.bottom.left']);
		leds.push(['bottom right led', 'leds.bottom.right']);
		leds.push(['circle leds', 'leds.circle']);
		leds.push(['horizontal proximity leds', 'leds.prox.h']);
		leds.push(['ground proximity leds', 'leds.prox.v']);
		leds.push(['remote control led', 'leds.rc']);
		leds.push(['button leds', 'leds.buttons']);
		leds.push(['temperature led', 'leds.temperature']);
		leds.push(['microphone led', 'leds.sound']);

		var dropdown = new Blockly.FieldDropdown(leds);
		this.appendDummyInput().appendField('turn off').appendField(dropdown, 'LED');
	}
};

Blockly.Blocks['thymio_sound_system'] = {
	/**
	 * Block to play Thymio system sounds.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SOUND_SYSTEM_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SOUND_SYSTEM_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		var soundList = [];
		soundList.push(['startup', '0']);
		soundList.push(['shutdown', '1']);
		soundList.push(['arrow', '2']);
		soundList.push(['central', '3']);
		soundList.push(['scary', '4']);
		soundList.push(['collision', '5']);
		soundList.push(['target friendly', '6']);
		soundList.push(['target detected', '7']);
		var dropdown = new Blockly.FieldDropdown(soundList);
		
		this.appendDummyInput().appendField('play').appendField(dropdown, 'SOUND').appendField('sound');
	}
};

Blockly.Blocks['thymio_sound_note'] = {
	/**
	 * Block to play Thymio sound notes.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SOUND_NOTE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SOUND_NOTE_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendValueInput('FREQ').setCheck('Number').appendField('play');
		this.appendValueInput('DURATION').setCheck('Number').appendField('Hz note for');
		this.appendDummyInput().appendField('/ 60 seconds')
		this.setInputsInline(true);
	}
};

Blockly.Blocks['thymio_sound_stop'] = {
	/**
	 * Block to cause Thymio to stop playing sound.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SOUND_STOP_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SOUND_STOP_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		this.appendDummyInput().appendField('stop playing sound');
	}
};

Blockly.Blocks['thymio_button_pressed'] = {
	/**
	 * Block for checking whether a button is pressed
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_BUTTON_PRESSED_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_BUTTON_PRESSED_TOOLTIP);

		var buttonDropdown = new Blockly.FieldDropdown([['center', 'button.center'], ['forward', 'button.forward'], ['backward', 'button.backward'], ['left', 'button.left'], ['right', 'button.right']]);
		this.setOutput(true, 'Boolean');
		this.appendDummyInput().appendField(buttonDropdown, 'BUTTON').appendField('button touched');
	}
};

Blockly.Blocks['thymio_prox_check'] = {
	/**
	 * Block for checking whether a proximity sensor is blocked or cleared
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_PROX_CHECK_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_PROX_CHECK_TOOLTIP);
		
		var sensorList = [];
		sensorList.push(['front left', 'prox.horizontal[0]']);
		sensorList.push(['front left/middle', 'prox.horizontal[1]']);
		sensorList.push(['front middle', 'prox.horizontal[2]']);
		sensorList.push(['front right/middle', 'prox.horizontal[3]']);
		sensorList.push(['front right', 'prox.horizontal[4]']);
		sensorList.push(['rear left', 'prox.horizontal[5]']);
		sensorList.push(['rear right', 'prox.horizontal[6]']);

		var sensorDropdown = new Blockly.FieldDropdown(sensorList);
		var modeDropdown = new Blockly.FieldDropdown([['proximity', 'BLOCK'], ['no proximity', 'CLEAR']]);
		
		this.setOutput(true, 'Boolean');
		this.appendDummyInput().appendField(sensorDropdown, 'SENSOR').appendField('sensor detecting').appendField(modeDropdown, 'MODE');
	}
};

Blockly.Blocks['thymio_prox_ground_check'] = {
	/**
	 * Block for checking whether a ground proximity sensor is white or black
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_PROX_GROUND_CHECK_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_PROX_GROUND_CHECK_TOOLTIP);
		
		var sensorList = [];
		sensorList.push(['left', 'prox.ground.delta[0]']);
		sensorList.push(['right', 'prox.ground.delta[1]']);

		var sensorDropdown = new Blockly.FieldDropdown(sensorList);
		var modeDropdown = new Blockly.FieldDropdown([['black', 'BLACK'], ['white', 'WHITE'], ['proximity', 'PROX'], ['no proximity', 'NOPROX']]);
		
		this.setOutput(true, 'Boolean');
		this.appendDummyInput().appendField(sensorDropdown, 'SENSOR').appendField('ground sensor detecting').appendField(modeDropdown, 'MODE');
	}
};

Blockly.Blocks['thymio_sensor_temperature'] = {
	/**
	 * Block for retrieving the temperature sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_TEMPERATURE_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_TEMPERATURE_TOOLTIP);

		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField('temperature sensor value');
	}
};

Blockly.Blocks['thymio_sensor_mic'] = {
	/**
	 * Block for retrieving the microphone intensity sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_MIC_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_MIC_TOOLTIP);

		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField('microphone intensity value');
	}
};

Blockly.Blocks['thymio_sensor_comm'] = {
	/**
	 * Block for retrieving the IR communication value
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_COMM_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_COMM_TOOLTIP);

		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField('received IR communication value');
	}
};

Blockly.Blocks['thymio_sensor_prox'] = {
	/**
	 * Block for retrieving a proximity sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_PROX_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_PROX_TOOLTIP);

		var sensorList = [];
		sensorList.push(['front left', 'prox.horizontal[0]']);
		sensorList.push(['front left/middle', 'prox.horizontal[1]']);
		sensorList.push(['front middle', 'prox.horizontal[2]']);
		sensorList.push(['front right/middle', 'prox.horizontal[3]']);
		sensorList.push(['front right', 'prox.horizontal[4]']);
		sensorList.push(['rear left', 'prox.horizontal[5]']);
		sensorList.push(['rear right', 'prox.horizontal[6]']);
		sensorList.push(['left ground', 'prox.ground.delta[0]']);
		sensorList.push(['right ground', 'prox.ground.delta[1]']);

		var dropdown = new Blockly.FieldDropdown(sensorList);
		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField(dropdown, 'SENSOR').appendField('proximity sensor closeness');
	}
};

Blockly.Blocks['thymio_sensor_motor'] = {
	/**
	 * Block for retrieving a motor sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_MOTOR_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_MOTOR_TOOLTIP);

		var sensorList = [];
		sensorList.push(['left', 'motor.left.speed']);
		sensorList.push(['right', 'motor.right.speed']);

		var dropdown = new Blockly.FieldDropdown(sensorList);
		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField(dropdown, 'SENSOR').appendField('motor speed');
	}
};

Blockly.Blocks['thymio_sensor_acc'] = {
	/**
	 * Block for retrieving a accelerometer sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_ACC_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_ACC_TOOLTIP);

		var sensorList = [];
		sensorList.push(['x', 'acc[0]']);
		sensorList.push(['y', 'acc[1]']);
		sensorList.push(['z', 'acc[2]']);

		var dropdown = new Blockly.FieldDropdown(sensorList);
		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField('accelerometer').appendField(dropdown, 'SENSOR').appendField('sensor value');
	}
};

Blockly.Blocks['thymio_sensor_rc'] = {
	/**
	 * Block for retrieving a remote control sensor state
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_SENSOR_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SENSOR_RC_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SENSOR_RC_TOOLTIP);

		var sensorList = [];
		sensorList.push(['address', 'rc5.address']);
		sensorList.push(['command', 'rc5.command']);

		var dropdown = new Blockly.FieldDropdown(sensorList);
		this.setOutput(true, 'Number');
		this.appendDummyInput().appendField('received remote control').appendField(dropdown, 'SENSOR').appendField('value');
	}
};

Blockly.Blocks['thymio_motors_start'] = {
	/**
	 * Block for starting Thymio's motors
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_MOTORS_START_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_MOTORS_START_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		var commands = [];
		commands.push(['driving forward', 'FORWARD']);
		commands.push(['driving backward', 'BACKWARD']);
		commands.push(['turning left', 'TURNLEFT']);
		commands.push(['turning right', 'TURNRIGHT']);
		commands.push(['turning backward left', 'TURNBACKWARDLEFT']);
		commands.push(['turning backward right', 'TURNBACKWARDRIGHT']);
		commands.push(['spinning counterclockwise', 'SPINCCW']);
		commands.push(['spinning clockwise', 'SPINCW']);		

		var dropdown = new Blockly.FieldDropdown(commands);
		this.appendValueInput('SPEED').setCheck('Number').appendField('start').appendField(dropdown, 'COMMAND').appendField('with speed');
	}
};

Blockly.Blocks['thymio_motors_stop'] = {
	/**
	 * Block for stopping Thymio's motors
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_MOTORS_STOP_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_MOTORS_STOP_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		this.appendDummyInput().appendField('stop motors');
	}
};

Blockly.Blocks['thymio_actuator_mic'] = {
	/**
	 * Block for setting a Thymio microphone threshold
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_ACTUATOR_MIC_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_ACTUATOR_MIC_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
	
		this.appendValueInput('VALUE').setCheck('Number').appendField('set microphone threshold to');
	}
};

Blockly.Blocks['thymio_actuator_comm'] = {
	/**
	 * Block for setting an IR communication to send
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_ACTUATOR_COMM_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_ACTUATOR_COMM_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
	
		this.appendValueInput('VALUE').setCheck('Number').appendField('set IR communication to transmit to');
	}
};

Blockly.Blocks['thymio_actuator_timer'] = {
	/**
	 * Block for setting a Thymio timer period
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_ACTUATOR_TIMER_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_ACTUATOR_TIMER_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var variables = [];
		variables.push(['first','timer.period[0]']);
		variables.push(['second','timer.period[1]']);

		var dropdown = new Blockly.FieldDropdown(variables);
		this.appendValueInput('VALUE').setCheck('Number').appendField('set').appendField(dropdown, 'VARIABLE').appendField('timer period to');
		this.appendDummyInput().appendField('milliseconds');
		this.setInputsInline(true);
	}
};

Blockly.Blocks['thymio_actuator_motor'] = {
	/**
	 * Block for setting a Thymio motor actuator
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_ACTUATOR_MOTOR_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_ACTUATOR_MOTOR_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var variables = [];
		variables.push(['left', 'motor.left.target']);
		variables.push(['right', 'motor.right.target']);

		var dropdown = new Blockly.FieldDropdown(variables);
		this.appendValueInput('VALUE').setCheck('Number').appendField('set').appendField(dropdown, 'VARIABLE').appendField('motor speed to');
	}
};

Blockly.Blocks['thymio_variable_get'] = {
	/**
	 * Block for variable getter. This is a copy paste of blockly's native variables_get block, except that this one returns only Numbers
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setHelpUrl(Blockly.Msg.VARIABLES_GET_HELPURL);
		this.setColour(Blockly.Blocks.variables.HUE);
		this.appendDummyInput().appendField(new Blockly.FieldVariable(Blockly.Msg.VARIABLES_DEFAULT_NAME), 'VAR');
		this.setOutput(true, 'Number');
		this.setTooltip(Blockly.Msg.VARIABLES_GET_TOOLTIP);
		this.contextMenuMsg_ = Blockly.Msg.VARIABLES_GET_CREATE_SET;
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('VAR')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string}
	 *            oldName Previous name of variable.
	 * @param {string}
	 *            newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
			this.setFieldValue(newName, 'VAR');
		}
	},
	contextMenuType_ : 'thymio_variable_set',
	/**
	 * Add menu option to create getter/setter block for this setter/getter.
	 * 
	 * @param {!Array}
	 *            options List of menu options to add to.
	 * @this Blockly.Block
	 */
	customContextMenu : function(options)
	{
		var option = {
			enabled : true
		};
		var name = this.getFieldValue('VAR');
		option.text = this.contextMenuMsg_.replace('%1', name);
		var xmlField = goog.dom.createDom('field', null, name);
		xmlField.setAttribute('name', 'VAR');
		var xmlBlock = goog.dom.createDom('block', null, xmlField);
		xmlBlock.setAttribute('type', this.contextMenuType_);
		option.callback = Blockly.ContextMenu.callbackFactory(this, xmlBlock);
		options.push(option);
	}
};

Blockly.Blocks['thymio_variable_set'] = {
	/**
	 * Block for variable setter. This is a copy paste of blockly's native variables_set block, except that this one accepts only Numbers
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.jsonInit({
			"message0" : Blockly.Msg.VARIABLES_SET,
			"args0" : [{
				"type" : "field_variable",
				"name" : "VAR",
				"variable" : Blockly.Msg.VARIABLES_DEFAULT_NAME
			}, {
				"type" : "input_value",
				"name" : "VALUE",
				"check": "Number"
			}],
			"previousStatement" : null,
			"nextStatement" : null,
			"colour" : Blockly.Blocks.variables.HUE,
			"tooltip" : Blockly.Msg.VARIABLES_SET_TOOLTIP,
			"helpUrl" : Blockly.Msg.VARIABLES_SET_HELPURL
		});
		this.contextMenuMsg_ = Blockly.Msg.VARIABLES_SET_CREATE_GET;
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('VAR')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string}
	 *            oldName Previous name of variable.
	 * @param {string}
	 *            newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
			this.setFieldValue(newName, 'VAR');
		}
	},
	contextMenuType_ : 'thymio_variable_get',
	customContextMenu : Blockly.Blocks['thymio_variable_get'].customContextMenu
};


Blockly.Blocks['thymio_declare_array'] = {
	/**
	 * Block to declare Thymio arrays.
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ARRAYS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_DECLARE_ARRAY_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_DECLARE_ARRAY_TOOLTIP);

		var variableField = new Blockly.FieldTextInput('a');
		var sizeField = new Blockly.FieldTextInput('3', Blockly.FieldTextInput.numberValidator);

		this.appendDummyInput().appendField('declare').appendField(variableField, 'VAR').appendField('as array of size').appendField(sizeField, 'SIZE');
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('VAR')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string} oldName Previous name of variable.
	 * @param {string} newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
			this.setFieldValue(newName, 'VAR');
		}
	}
};

Blockly.Blocks['thymio_set_array'] = {
	/**
	 * Block for setting a array element
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ARRAYS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_SET_ARRAY_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_SET_ARRAY_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);

		var variableField = new Blockly.FieldTextInput('a');

	    this.appendValueInput('INDEX').setCheck('Number').appendField('set array').appendField(variableField, 'VAR').appendField('element');
	    this.appendValueInput('VALUE').setCheck('Number').appendField('to');
	    this.setInputsInline(true);
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('VAR')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string} oldName Previous name of variable.
	 * @param {string} newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
			this.setFieldValue(newName, 'VAR');
		}
	}
};

Blockly.Blocks['thymio_get_array'] = {
	/**
	 * Block for getting a array element
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ARRAYS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_GET_ARRAY_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_GET_ARRAY_TOOLTIP);

		var variableField = new Blockly.FieldTextInput('a');
		
		this.setOutput(true, 'Number');
		this.appendValueInput('INDEX').setCheck('Number').appendField('get array').appendField(variableField, 'VAR').appendField('element');
	    this.setInputsInline(true);
	},
	/**
	 * Return all variables referenced by this block.
	 * 
	 * @return {!Array.<string>} List of variable names.
	 * @this Blockly.Block
	 */
	getVars : function()
	{
		return [this.getFieldValue('VAR')];
	},
	/**
	 * Notification that a variable is renaming. If the name matches one of this
	 * block's variables, rename it.
	 * 
	 * @param {string} oldName Previous name of variable.
	 * @param {string} newName Renamed variable.
	 * @this Blockly.Block
	 */
	renameVar : function(oldName, newName)
	{
		if(Blockly.Names.equals(oldName, this.getFieldValue('VAR'))) {
			this.setFieldValue(newName, 'VAR');
		}
	}
};

Blockly.Blocks['thymio_compare'] = {
	/**
	 * Block for comparison operator. This is a copy paste of blockly's native logic_compare block, except that this one accepts only Numbers
	 * 
	 * @this Blockly.Block
	 */
	init : function()
	{
		var OPERATORS = this.RTL ? [['=', 'EQ'], ['\u2260', 'NEQ'], ['>', 'LT'], ['\u2265', 'LTE'], ['<', 'GT'], ['\u2264', 'GTE']] : [['=', 'EQ'], ['\u2260', 'NEQ'], ['<', 'LT'], ['\u2264', 'LTE'], ['>', 'GT'], ['\u2265', 'GTE']];
		this.setHelpUrl(Blockly.Msg.LOGIC_COMPARE_HELPURL);
		this.setColourRGB(Blockly.CAT_LOGIC_RGB);
		this.setOutput(true, 'Boolean');
		this.appendValueInput('A').setCheck('Number');
		this.appendValueInput('B').setCheck('Number').appendField(new Blockly.FieldDropdown(OPERATORS), 'OP');
		this.setInputsInline(true);
		// Assign 'this' to a variable for use in the tooltip closure below.
		var thisBlock = this;
		this.setTooltip(function()
		{
			var op = thisBlock.getFieldValue('OP');
			var TOOLTIPS = {
				'EQ' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_EQ,
				'NEQ' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_NEQ,
				'LT' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_LT,
				'LTE' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_LTE,
				'GT' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_GT,
				'GTE' : Blockly.Msg.LOGIC_COMPARE_TOOLTIP_GTE
			};
			return TOOLTIPS[op];
		});
	}
};

Blockly.Blocks['thymio_arithmetic'] = {
	/**
	 * Block for Thymio arithmetic operators.
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_MATH_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_ARITHMETIC_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_ARITHMETIC_TOOLTIP);
		
		var operators = [];
		
		operators.push(['+', '+']);
		operators.push(['-', '-']);
		operators.push(['*', '*']);
		operators.push(['÷', '/']);
		operators.push(['mod', '%']);
		
		this.setOutput(true, 'Number');
		this.appendValueInput('A').setCheck('Number');
		this.appendValueInput('B').setCheck('Number').appendField(new Blockly.FieldDropdown(operators), 'OP');
		this.setInputsInline(true);
	}
};

Blockly.Blocks['thymio_binary'] = {
	/**
	 * Block for Thymio binary operators.
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_MATH_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_BINARY_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_BINARY_TOOLTIP);
		
		var operators = [];
		
		operators.push(['left shift', '<<']);
		operators.push(['right shift', '>>']);
		operators.push(['and', '&']);
		operators.push(['or', '|']);
		operators.push(['xor', '^']);
		
		this.setOutput(true, 'Number');
		this.appendValueInput('A').setCheck('Number').appendField('binary');
		this.appendValueInput('B').setCheck('Number').appendField(new Blockly.FieldDropdown(operators), 'OP');
		this.setInputsInline(true);
	}
};

Blockly.Blocks['thymio_unary'] = {
	/**
	 * Block for Thymio unary operators.
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_MATH_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_UNARY_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_UNARY_TOOLTIP);
		
		var operators = [];
		
		operators.push(['negative', '-']);
		operators.push(['absolute', 'abs']);
		operators.push(['binary not', '~']);
		
		this.setOutput(true, 'Number');
		this.appendValueInput('VALUE').setCheck('Number').appendField(new Blockly.FieldDropdown(operators), 'OP');
	}
};

Blockly.Blocks['thymio_communication'] = {
	/**
	 * Block for controlling Thymio's communication feature
	 * @this Blockly.Block
	 */
	init : function()
	{
		this.setColourRGB(Blockly.CAT_ACTUATORS_RGB);
		this.setHelpUrl(Blockly.Msg.TEXT_THYMIO_COMMUNICATION_HELPURL);
		this.setTooltip(Blockly.Msg.TEXT_THYMIO_COMMUNICATION_TOOLTIP);
		this.setPreviousStatement(true);
		this.setNextStatement(true);
		
		var modeDropdown = new Blockly.FieldDropdown([['enable', 'ENABLE'], ['disable', 'DISABLE']]);
		
		this.appendDummyInput().appendField(modeDropdown, 'MODE').appendField('IR communication');
	}
};
