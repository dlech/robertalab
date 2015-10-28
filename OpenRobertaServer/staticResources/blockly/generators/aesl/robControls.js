/**
 * @fileoverview Generating AESL for rob controls.
 * @author fabian@hahn.graphics (Fabian Hahn)
 */
'use strict';

goog.provide('Blockly.AESL.robControls');

goog.require('Blockly.AESL');

Blockly.AESL['robControls_start'] = function(block)
{
	var code = '';
	
	if(block.declare_) {
		var declarations = Blockly.AESL.statementToCode(block, 'ST');
		code += '# variable initializations\n' + declarations + '\n';
	}
	
	code += '# program start\n';
	return code;
}