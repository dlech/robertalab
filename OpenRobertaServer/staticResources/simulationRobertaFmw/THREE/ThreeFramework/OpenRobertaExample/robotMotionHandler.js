var rightMotorSpeed ;
var leftMotorSpeed ;
var WHEEL_RATIO  = 2.8/(15);// 15 cm scale is equal to 1 unit on the canvas approximation 
var theta ;
var DISTANCE_BTW_WHEELS = 13.5/(15); // 15 cm scale is equal to  1 unit on the canvas approximation
var CURRENT_MEASURE = "Degree" ;
var VOLTAGE_LEVEL = 8; // even the hardware description says 10 v, the maximum shown value on the EV3  is 8 v.
var deltaX  ;
var deltaY   ;
var rightSpdPerFrame ;
var leftSpdPerFrame ;
var AVERAGE_FPS = 60;
var DEG_BY_VOLT_SECOND = 62;// Data taken from LEJOS  Documentation
var robotMotionValues = [] ;


function calculateTheta(){
	
	//if(CURRENT_MEASURE == "Degree"){
		rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND ;
		//AVERAGE_FPS  ;// Warning  the sin and cos should be for 
																							//Degree format otherwise it should be translated to radians
		leftSpdPerFrame = leftMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND ;
		//AVERAGE_FPS	;
		theta = WHEEL_RATIO/DISTANCE_BTW_WHEELS*(rightSpdPerFrame-leftSpdPerFrame) ;
	//}else {
		
		//rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*1000/avgFPS
	//}

}



function calculateDeltaX(){
	
	deltaX = WHEEL_RATIO/2*(rightSpdPerFrame+leftSpdPerFrame)*Math.cos(theta*(Math.PI/180)) ;  // + instead - 
	
	
	
	
}


function calculateDeltaY(){
	
	
	
	deltaY = WHEEL_RATIO/2*(rightSpdPerFrame+leftSpdPerFrame)*Math.sin(theta*(Math.PI/180)) ;  // + instead - 
	
	
}


function getRobotMotion( robertaOutPut) {
	
	rightMotorSpeed =  robertaOutPut[RIGHT_MOTOR_SPD_INDEX];
	leftMotorSpeed =  robertaOutPut[LEFT_MOTOR_SPD_INDEX];
	
	calculateTheta() ;
	calculateDeltaX() ;
	calculateDeltaY();
	
	robotMotionValues[THETA_INDEX] = theta ;
	robotMotionValues[DELTA_X_INDEX] = deltaX ;
	robotMotionValues[DELTA_Y_INDEX] = deltaY;
	
	
	return robotMotionValues ;
	
}