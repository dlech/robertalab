function initProgram(program) {
    MEM.clear();
    PROGRAM_SIMULATION.setNextStatement(true);
    PROGRAM_SIMULATION.setWait(false);
    PROGRAM_SIMULATION.set(program);
    LIGHT.setMode(OFF);
    ACTORS.resetMotorsSpeed();
}

function setSensorActorValues(simulationSensorData) {
    SENSORS.setTouchSensor(simulationSensorData.touch);
    SENSORS.setColor(simulationSensorData.color);
    SENSORS.setLight(simulationSensorData.light);
    SENSORS.setUltrasonicSensor(simulationSensorData.ultrasonic);
    ACTORS.getLeftMotor().setCurrentRotations(simulationSensorData.tacho[0]);
    ACTORS.getRightMotor().setCurrentRotations(simulationSensorData.tacho[1]);
    PROGRAM_SIMULATION.getTimer().setCurrentTime(simulationSensorData.time);
    PROGRAM_SIMULATION.setNextFrameTimeDuration(simulationSensorData.frameTime);
}

function handleSpeeds(speeds) {
    var values = {};
    values['powerLeft'] = ACTORS.getLeftMotor().getPower();
    values['powerRight'] = ACTORS.getRightMotor().getPower();
    if (speeds[0] != undefined) {
        values['powerLeft'] = speeds[0]
    }
    if (speeds[1] != undefined) {
        values['powerRight'] = speeds[1]
    }
    return values;
}

function step(simulationSensorData) {
    setSensorActorValues(simulationSensorData);
    if (PROGRAM_SIMULATION.isNextStatement()) {
        var stmt = PROGRAM_SIMULATION.getRemove();
        switch (stmt.stmt) {
        case ASSIGN_STMT:
            var value = evalExpr(stmt.expr);
            MEM.assign(stmt.name, value);
            break;

        case VAR_DECLARATION:
            var value = evalExpr(stmt.value);
            MEM.decl(stmt.name, value);
            break;

        case IF_STMT:
            evalIf(stmt);
            step(simulationSensorData);
            break;

        case REPEAT_STMT:
            evalRepeat(stmt);
            step(simulationSensorData);
            break;

        case DRIVE_ACTION:
            evalDriveAction(simulationSensorData, stmt);
            break;

        case TURN_ACTION:
            evalTurnAction(simulationSensorData, stmt);
            break;

        case MOTOR_ON_ACTION:
            evalMotorOnAction(simulationSensorData, stmt);
            break;

        case WAIT_STMT:
            evalWaitStmt(stmt);
            break;

        case WAIT_TIME_STMT:
            evalWaitTime(simulationSensorData, stmt);
            break;

        case TURN_LIGHT:
            LIGHT.setColor(stmt.color);
            LIGHT.setMode(stmt.mode);
            break;

        case STOP_DRIVE:
            ACTORS.setSpeed(0);
            break;

        case MOTOR_STOP:
            evalMotorStopAction(stmt);
            break;

        case RESET_LIGHT:
            LIGHT.setColor(GREEN);
            LIGHT.setMode(OFF);
            break;

        default:
            throw "Invalid Statement " + stmt.stmt + "!";
        }
    }
    newSpeeds = ACTORS.calculateCoveredDistance();
    PROGRAM_SIMULATION.handleWaitTimer();
    return handleSpeeds(newSpeeds);
}

function evalWaitTime(simulationSensorData, stmt) {
    PROGRAM_SIMULATION.setIsRunningTimer(true);
    PROGRAM_SIMULATION.resetTimer(simulationSensorData.time);
    PROGRAM_SIMULATION.setTimer(evalExpr(stmt.time));
}

function evalTurnAction(simulationSensorData, stmt) {
    ACTORS.resetTachoMotors(simulationSensorData.tacho[0], simulationSensorData.tacho[1]);
    ACTORS.setAngleSpeed(evalExpr(stmt.speed), stmt[TURN_DIRECTION]);
    setAngleToTurn(stmt);
}

function evalDriveAction(simulationSensorData, stmt) {
    ACTORS.resetTachoMotors(simulationSensorData.tacho[0], simulationSensorData.tacho[1]);
    ACTORS.setSpeed(evalExpr(stmt.speed), stmt[DRIVE_DIRECTION]);
    setDistanceToDrive(stmt);
}

function evalMotorOnAction(simulationSensorData, stmt) {
    if (stmt[MOTOR_SIDE] == MOTOR_LEFT) {
        ACTORS.resetLeftTachoMotor(simulationSensorData.tacho[0]);
        ACTORS.setLeftMotorSpeed(evalExpr(stmt.speed));
    } else {
        ACTORS.resetRightTachoMotor(simulationSensorData.tacho[1]);
        ACTORS.setRightMotorSpeed(evalExpr(stmt.speed));
    }
    setDurationToCover(stmt);
}

function evalMotorStopAction(stmt) {
    if (stmt[MOTOR_SIDE] == MOTOR_LEFT) {
        ACTORS.setLeftMotorSpeed(0);
    } else {
        ACTORS.setRightMotorSpeed(0);
    }
}

function setAngleToTurn(stmt) {
    if (stmt.angle != undefined) {
        ACTORS.clculateAngleToCover(evalExpr(stmt.angle));
    }
}

function setDistanceToDrive(stmt) {
    if (stmt.distance != undefined) {
        ACTORS.setDistanceToCover(evalExpr(stmt.distance));
    }
}

function setDurationToCover(stmt) {
    if (stmt[MOTOR_DURATION] != undefined) {
        ACTORS.setMotorDuration((stmt[MOTOR_DURATION]).motorMoveMode, evalExpr((stmt[MOTOR_DURATION]).motorDurationValue), stmt[MOTOR_SIDE]);
    }
}

function evalRepeat(stmt) {
    switch (stmt.mode) {
    case TIMES:
        for (var i = 0; i < evalExpr(stmt.expr); i++) {
            PROGRAM_SIMULATION.prepend(stmt.stmtList);
        }
        break;
    default:
        var value = evalExpr(stmt.expr);
        if (value) {
            PROGRAM_SIMULATION.prepend([ stmt ]);
            PROGRAM_SIMULATION.prepend(stmt.stmtList);
        }
    }
}

function evalIf(stmt) {
    var programPrefix;
    var value;
    for (var i = 0; i < stmt.exprList.length; i++) {
        value = evalExpr(stmt.exprList[i]);
        if (value) {
            programPrefix = stmt.thenList[i];
            if (PROGRAM_SIMULATION.isWait()) {
                PROGRAM_SIMULATION.getRemove();
                PROGRAM_SIMULATION.setWait(false);
            }
            break;
        }
    }

    if ((programPrefix == undefined || programPrefix == []) && !PROGRAM_SIMULATION.isWait()) {
        programPrefix = stmt.elseStmts;
    }

    PROGRAM_SIMULATION.prepend(programPrefix);
    return value;
}

function evalWaitStmt(stmt) {
    PROGRAM_SIMULATION.setWait(true);
    PROGRAM_SIMULATION.prepend([ stmt ]);
    for (var i = 0; i < stmt.statements.length; i++) {
        var value = evalIf(stmt.statements[i]);
        if (value) {
            break;
        }
    }

}

function evalExpr(expr) {
    switch (expr.expr) {
    case NUM_CONST:
    case BOOL_CONST:
    case COLOR_CONST:
        return expr.value;

    case VAR:
        return MEM.get(expr.name);

    case BINARY:
        return evalBinary(expr.op, expr.left, expr.right);

    case GET_SAMPLE:
        return evalSensor(expr[SENSOR_TYPE], expr[SENSOR_MODE]);
    default:
        throw "Invalid Expression Type!";
    }
}

function evalSensor(sensorType, sensorMode) {
    switch (sensorType) {
    case TOUCH:
        return SENSORS.isPressed();
    case ULTRASONIC:
        return SENSORS.getUltrasonicSensor();
    case RED:
        return SENSORS.getLight();
    case COLOUR:
        return SENSORS.getColor();
    default:
        throw "Invalid Sensor!";
    }
}

function evalBinary(op, left, right) {
    var valLeft = evalExpr(left);
    var valRight = evalExpr(right);
    var val;
    switch (op) {
    case ADD:
        val = valLeft + valRight;
        break;
    case MINUS:
        val = valLeft - valRight;
        break;
    case MULTIPLY:
        val = valLeft * valRight;
        break;
    case DIVIDE:
        val = valLeft / valRight;
        break;
    case POWER:
        val = Math.pow(valLeft, valRight);
        break;
    case LT:
        val = valLeft < valRight;
        break;
    case GT:
        val = valLeft > valRight;
        break;
    case EQ:
        val = valLeft == valRight;
        break;
    case NEQ:
        val = valLeft != valRight;
        break;
    case GTE:
        val = valLeft >= valRight;
        break;
    case LTE:
        val = valLeft <= valRight;
        break;
    case OR:
        val = valLeft || valRight;
        break;
    case AND:
        val = valLeft && valRight;
        break;
    default:
        throw "Invalid Binary Operator";
    }
    return val;
}
