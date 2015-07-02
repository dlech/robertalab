var PROGRAM = (function () {
	var program = [];
	var wait = false;
	
	function set(newProgram) {
		program = newProgram;
	}
	
	function isTerminated() {
		return program.length == 0;
	}
	
	function get() {
		if (program.length == 0) {
			throw "Program is empty!";
		}
		return program[0];
	}
	
	
	function getRemove() {
		if (program.length == 0) {
			throw "Program is empty!";
		}
		var statement = program[0];
		program = program.slice(1, program.length);
		return statement;
	}
	
	function prepend(programPrefix) {
		if (programPrefix != undefined) {
			program = programPrefix.concat(program);
		}		
	}
	
	function isWait(){
		return wait;
	}
	
	function setWait(value) {
		wait = value;
	}
	
	function toString() {
		return program;
	}
	
	return {"set":set, "isTerminated":isTerminated, "get":get,
		"getRemove":getRemove, "prepend":prepend,
		"isWait":isWait, "setWait":setWait, "toString":toString};		
} ) ();