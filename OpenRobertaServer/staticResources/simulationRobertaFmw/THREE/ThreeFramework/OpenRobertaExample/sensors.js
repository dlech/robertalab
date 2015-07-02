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
	
	return {"isPressed":isPressed, "setTouchSensor":setTouchSensor};
}) ();