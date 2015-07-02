var MEM = (function () {
	var memory = {};
	
	function decl(name, init) {
		if (memory[name] != undefined) {
			throw "Variable " + name + " is defined!";
		}
		if (init == undefined) {
			throw "Variable " + name + " not initialized!";
		}
		memory[name] = init;
	}
	
	function assign(name, value) {
		if (memory[name] == undefined) {
			throw "Variable " + name + " is undefined!";
		}
		if (value == undefined) {
			throw "Variable " + name + " not assigned!";
		}
		memory[name] = value;
	}
	
	function get(name) {
		if (memory[name] == undefined) {
			throw "Variable " + name + " is undefined!";
		}
		
		return memory[name];
	}
	
	function clear() {
		memory = {};
	}
	
	function toString(){
		return JSON.stringify(memory);
	}
	
	return {"decl":decl, "assign":assign, "get":get, "clear":clear, "toString":toString};
}) ();