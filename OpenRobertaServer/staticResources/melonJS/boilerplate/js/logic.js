/**
 * Created by bbagci on 07.07.2015.
 */
Math.radians = function(degrees) {
    return degrees * Math.PI / 180;
};

// Converts from radians to degrees.
Math.degrees = function(radians) {
    return radians * 180 / Math.PI;
};

Math.Sinus = function(degrees){
    return Math.sin(degrees * Math.PI / 180);
};

Math.Cosinus = function(degrees){
    return Math.cos(degrees * Math.PI / 180);
};

var zahl = 0;
var richtungswinkel = -1;

var bewegungswinkel = -1;

var linkerRad = 0.75;
var rechterRad = 1.0;

var l = 20,
    Vl = 0.5,
    Vr = 1,
    R = 0,
    za = 0,
    w = 0,
    ICCx = 0,
    ICCy = 0;


var calcICCx = function (x, R, za){
    var tmp = x - R * Math.Sinus(za);
    return tmp;
}

var calcICCy = function (y, R, za){
    var tmp = y + R * Math.Cosinus(za);
    return tmp;
}

var calcR = function (lang, velL, velR){
    var tmp = (lang/2) * ((velL + velR) / (velR - velL));
    return tmp;
}

var calcW = function (lang, velL, velR) {
    var tmp = (velR - velL) / lang;
    return tmp;
}

var logic = {
    "sleep": function (delay) {
        var start = new Date().getTime();
        while (new Date().getTime() < start + delay);
    },

    "beweg_example" : function() {
        if(mainRoboter.angleTo(mainStone) >= Number.prototype.degToRad(-89)) {
            _this.body.vel.x += 1;
            //console.log(mainRoboter.angleTo(mainStone));
        } else if (_this.renderable.angle >= (-89).degToRad())
        {
            _this.renderable.angle += (-1).degToRad();

        } else if (mainRoboter.distanceTo(mainStone) > 0)
        {

            _this.body.vel.y -= 1;
            //console.log(mainRoboter.distanceTo(mainStone));
            console.log(mainRoboter.distanceTo(mainStone));
        }

    },

    "dreh_x" : function(x, y, winkel) {
        var temp = x * Math.Cosinus(winkel) - y *Math.Sinus(winkel);
        //console.log(temp);
        return temp;
    },

    "dreh_y" : function(x, y, winkel) {
        var temp = x * Math.Sinus(winkel) + y * Math.Cosinus(winkel);
        //console.log(temp);
        return temp;
    },

    "beweg_example2" : function() {
        if(zahl < 200) {
            zahl++;
            _this.body.vel.x = 1.5;
            //console.log(mainRoboter.angleTo(mainStone));
        } else if (_this.renderable.angle >= (richtungswinkel).degToRad())
        {
           _this.pos.x += 1;

        }

    }
};