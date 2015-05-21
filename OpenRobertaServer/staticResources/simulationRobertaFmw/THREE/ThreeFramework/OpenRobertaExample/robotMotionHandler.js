var rightMotorSpeed ;
var leftMotorSpeed ;
var WHEEL_RATIO  = 2.8;
var theta ;
var DISTANCE_BTW_WHEELS = 13.5;
var CURRENT_MEASURE = "Degree" ;
var VOLTAGE_LEVEL = 10;
var deltaX  ;
var deltaY   ;
var rightSpdPerFrame ;
var leftSpdPerFrame ;
var AVERAGE_FPS = 35;
var DEG_BY_VOLT_SECOND = 1000 ;
var robotMotionValues = [] ;


function calculateTheta(){
	
	//if(CURRENT_MEASURE == "Degree"){
		rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND/AVERAGE_FPS  ;// Warning  the sin and cos should be for 
																							//Degree format otherwise it should be translated to radians
		leftSpdPerFrame = leftMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND/AVERAGE_FPS	;
		theta = WHEEL_RATIO/DISTANCE_BTW_WHEELS*(rightSpdPerFrame-leftSpdPerFrame) ;
	//}else {
		
		//rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*1000/avgFPS
	//}

}



function calculateDeltaX(){
	
	deltaX = WHEEL_RATIO/2*(rightSpdPerFrame+leftSpdPerFrame)*Math.cos(theta) ;  // + instead - 
	
	
	
	
}


function calculateDeltaY(){
	
	
	
	deltaY = WHEEL_RATIO/2*(rightSpdPerFrame+leftSpdPerFrame)*Math.sin(theta) ;  // + instead - 
	
	
}


function getRobotMotion( robertaOutPut) {
	
	rightMotorSpeed =  robertaOutPut[0];
	leftMotorSpeed =  robertaOutPut[1];
	
	calculateTheta() ;
	calculateDeltaX() ;
	calculateDeltaY();
	
	robotMotionValues[0] = theta ;
	robotMotionValues[1] = deltaX ;
	robotMotionValues[2] = deltaY;
	
	
	return robotMotionValues ;
	
}