function continuation() {
    initProgram([ drive, x1, y1, w, finalX, waitStmt1 ]);
    while (!PROGRAM.isTerminated()) {
        step();
        sensors = moveRobot(ACTORS.a, ACTORS.b);
    }
    return MEM.toString() + " " + ACTORS.toString();
}

function initProgram(program) {
    MEM.clear();
    PROGRAM.set(program);
}

function step() {
    var stmt = PROGRAM.getRemove();
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
        break;

    case REPEAT_STMT:
        evalRepeat(stmt);
        break;

    case DRIVE_ACTION:
        ACTORS.setSpeed(stmt.speed);
        ACTORS.setDistanceToCover(stmt.distance);
        break;

    case WAIT_STMT:
        evalWaitStmt(stmt);
        break;
    default:
        throw "Invalid Statement " + stmt.stmt + "!";
    }
}

function evalRepeat(stmt) {
    var value = evalExpr(stmt.expr);
    if (value) {
        PROGRAM.prepend([ stmt ]);
        PROGRAM.prepend(stmt.stmtList);
    }
}

function evalIf(stmt) {
    var programPrefix;
    for (var i = 0; i < stmt.exprList.length; i++) {
        var value = evalExpr(stmt.exprList[i]);
        if (value) {
            programPrefix = stmt.thenList[i];
            if (PROGRAM.isWait()) {
                PROGRAM.getRemove();
                PROGRAM.setWait(false);
            }
            break;
        }
    }
    if (programPrefix == undefined) {
        programPrefix = stmt.elseStmts;
    }
    PROGRAM.prepend(programPrefix);

}

function evalWaitStmt(stmt) {
    PROGRAM.setWait(true);
    PROGRAM.prepend([ stmt ]);
    PROGRAM.prepend([ stmt.statements ]);
}

function evalExpr(expr) {
    switch (expr.expr) {
    case NUM_CONST:
    case BOOL_CONST:
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
    case MULT:
        val = valLeft * valRight;
        break;
    case LESS:
        val = valLeft < valRight;
        break;
    case EQ:
        val = valLeft == valRight;
        break;
    default:
        throw "Invalid Binary Operator";
    }
    return val;
}
