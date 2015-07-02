var SENSORS = (function() {
    var touchSensor = false;
    var ultrasonicSensor = 0;
    var colorSensor = 0;
    var lightSensor = 0;

    function isPressed() {
        return touchSensor;
    }

    function setTouchSensor(value) {
        touchSensor = value;
    }

    return {
        "isPressed" : isPressed,
        "setTouchSensor" : setTouchSensor
    };
})();

var ACTORS = (function() {
    var distanceCovered = 0;
    var distanceToCover = 0;

    var leftMotor = new Motor();
    var rightMotor = new Motor();

    function getLeftMotor() {
        return leftMotor;
    }

    function getRightMotor() {
        return rightMotor;
    }

    function setSpeed(speed) {
        leftMotor.setPower(speed);
        rightMotor.setPower(speed);
    }

    function getDistanceCovered() {
        return distanceCovered;
    }

    function setDistanceCovered(distance) {
        distanceCovered = distance;
    }

    function getDistanceToCover() {
        return distanceToCover;
    }

    function setDistanceToCover(distance) {
        distanceCovered = 0;
        distanceToCover = distance;
    }

    function Motor() {
        this.power = 0;
        this.stopped = false;
    }

    Motor.prototype.getPower = function() {
        return this.power;
    };

    Motor.prototype.setPower = function(value) {
        this.power = value;
    };

    Motor.prototype.isStopped = function() {
        return this.stopped;
    };

    Motor.prototype.setStopped = function(value) {
        this.stopped = value;
    };

    function toString() {
        return JSON.stringify([ distanceCovered, distanceToCover, leftMotor, rightMotor ]);
    }

    return {
        "getLeftMotor" : getLeftMotor,
        "getRightMotor" : getRightMotor,
        "setSpeed" : setSpeed,
        "getDistanceCovered" : getDistanceCovered,
        "setDistanceCovered" : setDistanceCovered,
        "getDistanceToCover" : getDistanceToCover,
        "setDistanceToCover" : setDistanceToCover,
        "toString" : toString
    };
})();

var MEM = (function() {
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

    function toString() {
        return JSON.stringify(memory);
    }

    return {
        "decl" : decl,
        "assign" : assign,
        "get" : get,
        "clear" : clear,
        "toString" : toString
    };
})();

var PROGRAM = (function() {
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

    function isWait() {
        return wait;
    }

    function setWait(value) {
        wait = value;
    }

    function toString() {
        return program;
    }

    return {
        "set" : set,
        "isTerminated" : isTerminated,
        "get" : get,
        "getRemove" : getRemove,
        "prepend" : prepend,
        "isWait" : isWait,
        "setWait" : setWait,
        "toString" : toString
    };
})();
