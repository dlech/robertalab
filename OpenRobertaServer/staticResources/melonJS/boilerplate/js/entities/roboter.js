/**
 * Player Entity
 */

var _this;
var delaytime = 1000; // milliseconds
var once = true; // for determine one loop cycle

//--

var multicast = [];
function cast(tm) { // tm = time
    multicast.map(function (element, index) {
        index += 1; //dbg
        setTimeout(element, (tm * index));
    });
}

//--

game.RoboterEntity = me.Entity.extend({
    /**
     * constructor
     */
    init: function (x, y, settings) {
        // call the constructor
        this._super(me.Entity, 'init', [x, y, settings]);

        this.alwaysUpdate = true;
        this.body.gravity = 0;

        // this.body.setVelocity(1,1);
        // this.body.setMaxVelocity(10, 10);
        this.body.setFriction(1, 1);

        _this = this;

        me.input.bindKey(me.input.KEY.R, "start2");

        me.input.bindKey(me.input.KEY.W, "vorw");
        me.input.bindKey(me.input.KEY.A, "links");
        me.input.bindKey(me.input.KEY.D, "rechts");

        me.input.bindPointer(me.input.KEY.R); // wg touch
    },

    /**
     * update the entity
     */
    update: function (dt) {

        if (me.input.isKeyPressed('start2')) {
            Vl = 300;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('vorw')) {
            Vl = 400;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('rechts')) {
            Vl = 300;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('links')) {
            Vl = 400;
            Vr = 300;
            beta = true;
            logic.kurviere();
        }

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
    onCollision: function (response, other) {
        console.log("Roboter: bin mit etwas kollidiert");
        // Make all other objects solid
        return true;
    }
});