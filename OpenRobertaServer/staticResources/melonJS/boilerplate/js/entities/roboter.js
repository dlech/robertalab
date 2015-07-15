/**
 * Player Entity
 */
var _this;

var started = false, phase2 = false, phase3 = false;
var _counter = 0;
var b = function (){

    _this.renderable.angle += (-1).degToRad();
};

game.RoboterEntity = me.Entity.extend({
    /**
     * constructor
     */
    init:function (x, y, settings) {
        // call the constructor
        this._super(me.Entity, 'init', [x, y , settings]);

        //this.body.gravity = 0;
        this.alwaysUpdate = true;
        this.body.gravity = 0;

        this.body.setVelocity(2,2);
        this.body.setFriction(1,1);
        //this.renderable.angle = 0.78;

        _this = this;

        me.input.bindKey(me.input.KEY.S, "start2");
        me.input.bindPointer(me.input.KEY.S); // wg touch

        halfte_des_bildes_w = me.game.viewport.getWidth() / 2;
        halfte_des_bildes_h = me.game.viewport.getHeight() / 2;

        var er = logic.dreh_x(2, 1, 30);
        var err = logic.dreh_y(2, 1, 30);

        R = calcR(l, Vl, Vr);
        console.log("R: " + R);
        w = calcW(l, Vl, Vr);
        console.log("w: " + w);
        ICCx = calcICCx(this.pos.x, R, za);
        console.log("ICCx: " + ICCx);
        ICCy = calcICCy(this.pos.y, R, za);
        console.log("ICCy: " + ICCy);

        var ary1, ary2, ary3;

        //w *= 450; // 450 = 90 / 0.2

        ary1 = [
            [Math.Cosinus(w), -Math.Sinus(w), 0],
            [Math.Sinus(w), Math.Cosinus(w), 0],
            [0, 0, 1]
        ];

        ary2 = [
            [(this.pos.x - ICCx)],
            [(this.pos.y - ICCy)],
            [za]
        ];

        ary3 = [
            [ICCx],
            [ICCy],
            [w]
        ];

        var mm = math.multiply(ary1,ary2);
        var admm = math.add(mm, ary3);

        console.log(admm);


    },

    /**
     * update the entity
     */
    update : function (dt) {

        if (me.input.isKeyPressed('start2') && started == false) {
            //started = true;
            //console.log("isk drin");
            //this.pos.x -= 100;

            if(_counter < 100)
            {
                _counter += 0.1;

                w = 0.2 * _counter;

                ary1 = [
                    [Math.Cosinus(w), -Math.Sinus(w), 0],
                    [Math.Sinus(w), Math.Cosinus(w), 0],
                    [0, 0, 1]
                ];

                ary2 = [
                    [(this.pos.x - ICCx)],
                    [(this.pos.y - ICCy)],
                    [za]
                ];

                ary3 = [
                    [ICCx],
                    [ICCy],
                    [w]
                ];

                var mm = math.multiply(ary1,ary2);
                var admm = math.add(mm, ary3);

                this.pos.x =  admm[0][0];
                console.log(this.pos.x);
                this.pos.y = admm[1][0];
                za = admm[2][0];


            }

           // window.setTimeout(logic.beweg_example2, 0);

        }

        //this.body.gravity = b;
        /* START
        if (me.input.isKeyPressed('start1') && started == false) {
            started = true;

            //window.setTimeout()

        }

        // fahren auf x
        if(_counter < 275 && started == true){
            _counter += 1;

            this.body.vel.x += 1;
            //console.log(this.pos.x); // ########

            if(_counter == 275){
                started = false;
                phase2 = true;
                _counter = 0;
            }
        }

        // drehung
        if(_counter > (-90).degToRad() && phase2 == true) {

            _counter += (-1).degToRad();

            setTimeout('b()', 100);

           // console.log (_counter + "." + this.renderable.angle);
           // if(_counter == (-90).degToRad()){
             //   phase2 = false;
           // }

        }

        if(_counter < (-90).degToRad())
        {
            phase2 = false;
            _counter = 0;
            phase3 = true;
        }

        if(_counter < 300 && phase3 == true)
        {
            _counter += 1;

            this.body.vel.y -= 1;

            if(_counter == 275){
                phase3 = false;
                _counter = 0;
            }

        }
        END */

        // apply physics to the body (this moves the entity)
        this.body.update(dt);

        // handle collisions against other shapes
        me.collision.check(this);



        // return true if we moved or if the renderable was updated
    // return (this._super(me.Entity, 'update', [dt]) || this.body.vel.x !== 0 || this.body.vel.y !== 0 || this.renderable.angle != 0);
        return true;
    },

   /**
     * colision handler
     * (called when colliding with other objects)
     */
    onCollision : function (response, other) {
       _counter = 9999;
       console.log("Roboter: bin mit etwas kollidiert");
        // Make all other objects solid
        return true;
    }
});

