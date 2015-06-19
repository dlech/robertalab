var rightMotorSpeed ;
var leftMotorSpeed ;
var WHEEL_RATIO  = 2.8/(14.8);// 15 cm scale is equal to 1 unit on the canvas approximation 
var theta = 0 ;
var DISTANCE_BTW_WHEELS = 12/(14.8); // 15 cm scale is equal to  1 unit on the canvas approximation
var CURRENT_MEASURE = "Degree" ;
var VOLTAGE_LEVEL = 7.7; // even the hardware description says 10 v, the maximum shown value on the EV3  is 8 v.
var deltaX  ;
var deltaY   ;
var rightSpdPerFrame ;
var leftSpdPerFrame ;
var AVERAGE_FPS = 1/60;
var DEG_BY_VOLT_SECOND = 70*(Math.PI/180);// Data taken from LEJOS  Documentation
var robotMotionValues = [] ;
var deltaFpsSpeed  ;
var checkSeed ;
var rotationRatio ;


function calculateTheta(){
	
	//if(CURRENT_MEASURE == "Degree"){
		rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND*WHEEL_RATIO*deltaFpsSpeed;
		//AVERAGE_FPS  ;// Warning  the sin and cos should be for 
																							//Degree format otherwise it should be translated to radians
		leftSpdPerFrame = leftMotorSpeed*VOLTAGE_LEVEL*DEG_BY_VOLT_SECOND*WHEEL_RATIO*deltaFpsSpeed;
		//AVERAGE_FPS	;
		theta += (rightSpdPerFrame-leftSpdPerFrame)/(DISTANCE_BTW_WHEELS) ; // the WHEEL_RATIO is taken out because it was used wrongly now represent angle and not speed
	//}else {
		//rotationRatio = DISTANCE_BTW_WHEELS*( rightSpdPerFrame + leftSpdPerFrame)/(2*( rightSpdPerFrame - leftSpdPerFrame)) ; // calculate the ICC ration // just for debugging reasons
		//checkSeed = theta*(rotationRatio+(.5*DISTANCE_BTW_WHEELS));
		//if ()
		//console.log("R" + rotationRatio ) ;  // just for debugging reasons
		//rightSpdPerFrame = rightMotorSpeed*VOLTAGE_LEVEL*1000/avgFPS
	//}

}



function calculateDeltaX(){
	
	deltaX = (.5)*(rightSpdPerFrame+leftSpdPerFrame)*Math.cos(theta ) ;  // + instead - 
	//if((rotationRatio>=0) && (rotationRatio<16))
	//{	deltaX = theta*rotationRatio*Math.cos(theta ); 
	
	//}else{
		//deltaX = (.5)*(rightSpdPerFrame+leftSpdPerFrame)*Math.cos(theta ) ; 
	
	//}
	
	
}


function calculateDeltaY(){
	
	
	
	deltaY = (.5)*(rightSpdPerFrame+leftSpdPerFrame)*Math.sin(theta) ;  // + instead - 
	//if((rotationRatio>=0) && (rotationRatio<16))
	//{	deltaY = theta*rotationRatio*Math.sin(theta ); 
	
	//}else{
		//deltaY = (.5)*(rightSpdPerFrame+leftSpdPerFrame)*Math.sin(theta ) ; 
	
	//}
	
}


function getRobotMotion( robertaOutPut) {
	
	rightMotorSpeed =  robertaOutPut[RIGHT_MOTOR_SPD_INDEX];
	leftMotorSpeed =  robertaOutPut[LEFT_MOTOR_SPD_INDEX];
	deltaFpsSpeed =   clock.getDelta() ;		
	//console.log(""+deltaFpsSpeed)
	calculateTheta() ;
	//console.log("theta"+ theta ) ;
	calculateDeltaX() ;
	calculateDeltaY();
	
	robotMotionValues[THETA_INDEX] = theta ;
	robotMotionValues[DELTA_X_INDEX] = deltaX ;
	robotMotionValues[DELTA_Y_INDEX] = deltaY;
	
	//var rotationRatio = DISTANCE_BTW_WHEELS*(leftSpdPerFrame +rightSpdPerFrame)/(2*(leftSpdPerFrame -rightSpdPerFrame)) ;
	//console.log(""+rotationRatio)
	
	return robotMotionValues ;
	
}