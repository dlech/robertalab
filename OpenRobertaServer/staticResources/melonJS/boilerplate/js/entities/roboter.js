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

        me.game.viewport.follow(this.pos, me.game.viewport.AXIS.BOTH);

        me.input.bindKey(me.input.KEY.T, "start2");

        me.input.bindKey(me.input.KEY.W, "vorw");
        me.input.bindKey(me.input.KEY.A, "links");
        me.input.bindKey(me.input.KEY.D, "rechts");

        me.input.bindKey(me.input.KEY.R, "ani");

        me.input.bindPointer(me.input.KEY.T); // wg touch

        this.renderable.addAnimation("walk", [3, 2, 1, 0]);
        //this.renderable.addAnimation("walk",  [0, 1, 2, 3]);
        // define a standing animation (using the first frame)
        this.renderable.addAnimation("stand", [3]);
        // set the standing animation as default
        this.renderable.setCurrentAnimation("stand");
        //this.renderable.setAnimationFrame(2);
    },

    /**
     * update the entity
     */
    update: function (dt) {

        if (me.input.isKeyPressed('start2')) {

            if (!this.renderable.isCurrentAnimation("walk")) {
                this.renderable.setCurrentAnimation("walk");
            }

            Vl = 300;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('vorw')) {
            if (!this.renderable.isCurrentAnimation("walk")) {
                this.renderable.setCurrentAnimation("walk");
            }
            Vl = 400;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('rechts')) {
            if (!this.renderable.isCurrentAnimation("walk")) {
                this.renderable.setCurrentAnimation("walk");
            }
            Vl = 300;
            Vr = 400;
            beta = true;
            logic.kurviere();
        }
        if (me.input.isKeyPressed('links')) {
            if (!this.renderable.isCurrentAnimation("walk")) {
                this.renderable.setCurrentAnimation("walk");
            }
            Vl = 400;
            Vr = 300;
            beta = true;
            logic.kurviere();
        }

        if (this.body.vel.x == 0 && this.body.vel.y == 0) {
            if (!this.renderable.isCurrentAnimation("stand")) {
                this.renderable.setCurrentAnimation("stand");
            }
        }

        // apply physics to the body (this moves the entity)
        this.body.update(dt);

        // handle collisions against other shapes
        me.collision.check(this);


        // return true if we moved or if the renderable was updated
        // return (this._super(me.Entity, 'update', [dt]) || this.body.vel.x !== 0 || this.body.vel.y !== 0 || this.renderable.angle != 0);
        return (this._super(me.Entity, 'update', [dt])) || true;
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