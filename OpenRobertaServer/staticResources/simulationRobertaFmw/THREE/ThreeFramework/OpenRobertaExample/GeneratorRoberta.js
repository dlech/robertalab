
var meterCounter = 0 ;
var specialSituationCounter = 0 ;
var specialDistance = .5 ; 
var GOAL_DISTANCE = 6 ;
var rotationRobot = 0 ;
var speedRotation = .001 ;
var motorRotation = 20 ;
var botValues = [];
var outPutBot =  [] ;
var inputBot = [];
var directionFlag = 1 ; // it means across x 

// Constant Indexes
var POSITION_X_INDEX = 0 ;
var POSITION_Y_INDEX = 1 ;
var COLLISION_FLAG_INDEX =  2;
var LIGHT_COLOR_FLAG_INDEX = 3 ;
var LIGHT_COLOR_INDEX = 4 ;
var ROTATION_Z_INDEX = 5 ;
var NO_COLOR = "NONE" ;



function applyWheelRotationX(novaPosition ){
 
 meterCounter += Math.abs(novaPosition - botValues[POSITION_X_INDEX]);// diff btw old and new x position  
	if(isNaN(meterCounter))
	{
	 
		meterCounter =  0 ;
	
	 
	}
 
	botValues[POSITION_X_INDEX] = novaPosition;
	if(meterCounter>= GOAL_DISTANCE){
	  motorRotation = 0 ;
	}
  
	outPutBot[POSITION_X_INDEX]= speedRotation*motorRotation; 

 
}


function setDistanceWeel(newDistance){
	
	GOAL_DISTANCE = newDistance ;
	
}

function resetMeterCounter(){
	meterCounter =  0 ;
}


function resetMotorRotation(){
	motorRotation = 20 ;
}

function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}


function setInputValuesRoboterta(arguments){
	inputBot = arguments;
	readColor();
	applyWheelRotationX(inputBot[POSITION_X_INDEX] );
	
	
	return outPutBot ;
	
	
}

function readColor(){
	
   if(inputBot[LIGHT_COLOR_FLAG_INDEX])
   {		
		switch(inputBot[LIGHT_COLOR_INDEX]){
			case "B30006" :
				outPutBot[ROTATION_Z_INDEX]= -.15 ; 
				outPutBot[LIGHT_COLOR_INDEX] = NO_COLOR ;
				outPutBot[LIGHT_COLOR_FLAG_INDEX] = false ;
			break ;
			case "00642E" :
			
			break ;
			case "0057A6" :
			
			break ;
			case "F70117" :
			
			break ;
			case "FFFFF" :
			
			break ;	 
			case "000000" :
				//speedRotation = -.000001
				outPutBot[ROTATION_Z_INDEX]= .15 ; 
				outPutBot[LIGHT_COLOR_INDEX] = NO_COLOR ;
				outPutBot[LIGHT_COLOR_FLAG_INDEX] = false ;
			
			break ;
			case "532115" :
			
			break ;
			default :
				//outPutBot[POSITION_X_INDEX]= -3 ; 
				outPutBot[LIGHT_COLOR_FLAG_INDEX] = false ;
				outPutBot[ROTATION_Z_INDEX]= 0 ; 
			break ;
			
			
			
		}
	}else{
		outPutBot[LIGHT_COLOR_FLAG_INDEX] = false ;
		outPutBot[ROTATION_Z_INDEX]= 0 ; 
	
		
	}
	
}

function dogingChrash()
{
	
	if(inputBot[COLLISION_FLAG_INDEX])
	{
		// setting the special distance to go back
		if(directionFlag == 1)
			{
						specialSituationCounter += Math.abs(inputBot[POSITION_X_INDEX] - botValues[POSITION_X_INDEX]);// diff btw old and new x position  
						if(isNaN(specialSituationCounter))
						{
							specialSituationCounter =  0 ;
						}
 
						
						if(specialSituationCounter>= specialDistance){
							//motorRotation = 0 ;
							outPutBot[ROTATION_Z_INDEX]= .90 ;
							outPutBot[COLLISION_FLAG_INDEX]= false ;
							directionFlag = -1(directionFlag);
							applyMovemment() ; 
						}
						
						outPutBot[POSITION_Y_INDEX]= -speedRotation*motorRotation;
			}else
			{
				
					   // Apply Y movement
					
						outPutBot[POSITION_Y_INDEX]= -speedRotation*motorRotation;
				
			}
		
		
		
		
		
		if(specialSituationCounter>= specialDistance){
			//motorRotation = 0 ;
			outPutBot[ROTATION_Z_INDEX]= .90 ;
			outPutBot[COLLISION_FLAG_INDEX]= false ;
			directionFlag = -1(directionFlag);
			applyMovemment() ; 
		}
		else{
				
			if(directionFlag == 1)
			{
				
						outPutBot[POSITION_Y_INDEX]= -speedRotation*motorRotation;
			}else
			{
				
					outPutBot[POSITION_X_INDEX]= -speedRotation*motorRotation;
				
			}
			 
			botValues[POSITION_X_INDEX] = inputBot[POSITION_X_INDEX];
		}
	
	}else
	{	
		applyMovemment() ;
		
	}	
}
	
	
	
	
	
	
	
	


function  applyMovemment(){
	
	if(directionFlag == 1)
			{
				applyWheelRotationX(inputBot[POSITION_X_INDEX]) ;
						
			}else
			{
							
			}
	
	
}




