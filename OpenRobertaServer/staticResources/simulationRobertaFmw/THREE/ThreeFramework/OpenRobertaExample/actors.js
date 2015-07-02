var ACTORS = (function () {
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
	
	Motor.prototype.getPower = function(){
	    return this.power;
	};
	
	Motor.prototype.setPower = function(value){
	    this.power = value;
	};
	
	Motor.prototype.isStopped = function(){
	    return this.stopped;
	};
	
	Motor.prototype.setStopped = function(value){
	    this.stopped = value;
	};
	
	function toString(){
		return JSON.stringify([distanceCovered, distanceToCover, leftMotor, rightMotor]);
	}
	
	return {"getLeftMotor":getLeftMotor, "getRightMotor":getRightMotor, "setSpeed":setSpeed,
		"getDistanceCovered":getDistanceCovered, "setDistanceCovered":setDistanceCovered,
		"getDistanceToCover":getDistanceToCover, "setDistanceToCover":setDistanceToCover,
		"toString":toString
	};
}) ();