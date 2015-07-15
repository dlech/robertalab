game.PlayScreen = me.ScreenObject.extend({
    /**
     *  action to perform on state change
     */
    onResetEvent: function() {
        // reset the score
        //game.data.score = 0;
		
		me.levelDirector.loadLevel("karte");

        /* // add our HUD to the game world
        this.HUD = new game.HUD.Container();
        me.game.world.addChild(this.HUD);*/
        mainStone = me.game.world.getChildByName("mainStone")[0];
        mainRoboter = me.game.world.getChildByName("mainRoboter")[0];
      //  console.log( mainRoboter.distanceTo(mainStone));
       // console.log( mainRoboter.angleTo(mainStone));

        //var vv = new me.Vector2d(50, 50);

        //console.log(mainRoboter.angleToPoint(new me.Vector2d(50, 50)) );
    },

    /**
     *  action to perform when leaving this screen (state change)
     */
    onDestroyEvent: function() {
        // remove the HUD from the game world
       // me.game.world.removeChild(this.HUD);
    }
});
