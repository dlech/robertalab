/**
 * Stone Entity
 */
game.StoneEntity = me.Entity.extend({
    //constructor
    init: function (x, y, settings) {
        // call the constructor
        this._super(me.Entity, 'init', [x, y, settings]);

        this.body.gravity = 0;
        this.alwaysUpdate = true;
    },

    //update the entity
    update: function (dt) {
        // apply physics to the body (this moves the entity)
        this.body.update(dt);

        // handle collisions against other shapes
        me.collision.check(this);

        // return true if we moved or if the renderable was updated
        return (this._super(me.Entity, 'update', [dt]) || this.body.vel.x !== 0 || this.body.vel.y !== 0);
    },

    /**
     * colision handler
     * (called when colliding with other objects)
     */
    onCollision: function (response, other) {
        console.log("Baustein: etwas ist mit mir kollidiert");
        // Make all other objects solid
        return false;
    }
});