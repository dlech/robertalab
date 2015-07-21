/**
 *
 */
Math.ToRad = function (deg) {
    return deg * Math.PI / 180;
};
Math.ToDeg = function (rad) {
    return rad * 180 / Math.PI;
};
Math.Sinus = function (deg) {
    return Math.sin(Math.ToRad(deg));
};
Math.Cosinus = function (deg) {
    return Math.cos(Math.ToRad(deg));
};

// TODO: global variables are implemented in logic
// TODO: reasonable variable names
// TODO: capture distance traveled

var l = 20,
    Vl = 300,
    Vr = 400,
    R = 0,
    agl = 0,
    w = 0,
    ICCx = 0,
    ICCy = 0;

var tp_x = 0, tp_y = 0;

var calcICCx = function (x, R, za) {
    var tmp = x - R * Math.Sinus(za);
    return tmp;
};

var calcICCy = function (y, R, za) {
    var tmp = y + R * Math.Cosinus(za);
    return tmp;
};

var calcR = function (l, Vl, Vr) {
    var tmp = (l / 2) * ((Vl + Vr) / (Vr - Vl));
    return tmp;
};

var calcW = function (l, Vl, Vr) {
    var tmp = (Vr - Vl) / l;
    return tmp;
};

//--

var beta = true; // for determine one loop cycle

//--

var logic = {

    "sleep_by_date": function (delay) {
        var start = new Date().getTime();
        while (new Date().getTime() < start + delay);
    },

    "kurviere": function () {

        if (Vl == Vr) {
            Vr -= 0.01;
        }

        if (beta) {
            agl = _this.renderable.angle * (180 / Math.PI);
            R = calcR(l, Vl, Vr);
            w = calcW(l, Vl, Vr);
            ICCx = calcICCx(_this.pos.x, R, agl);
            ICCy = calcICCy(_this.pos.y, R, agl);

            tp_x = _this.pos.x;
            tp_y = _this.pos.y;

            beta = false;
        }

        matrix1 = [
            [Math.Cosinus(w), -(Math.Sinus(w)), 0],
            [Math.Sinus(w), Math.Cosinus(w), 0],
            [0, 0, 1]
        ];

        matrix2 = [
            [(tp_x - ICCx)],
            [(tp_y - ICCy)],
            [agl]
        ];

        matrix3 = [
            [ICCx],
            [ICCy],
            [w]
        ];

        var res = math.add(math.multiply(matrix1, matrix2), matrix3);

        tp_x = res[0][0];
        tp_y = res[1][0];
        agl = res[2][0];

        _this.body.vel.x = tp_x - _this.pos.x;
        _this.body.vel.y = tp_y - _this.pos.y;
        _this.renderable.angle = Math.ToRad(agl);

    }
};