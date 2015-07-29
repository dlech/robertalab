/* Game namespace */
var game = {
    // Run on page load.
    "onload" : function() {

        // Initialize the video.
        if (!me.video.init(960, 640, {
            wrapper : "screen",
            scale : "auto",
            scaleMethod : "fit",
            antiAlias : true
        })) {
            alert("Your browser does not support HTML5 canvas.");
            return;
        }

        me.video.setMaxSize(960, 640);

        // add "#debug" to the URL to enable the debug Panel
        if (me.game.HASH.debug === true) {
            window.onReady(function() {
                me.plugin.register.defer(this, me.debug.Panel, "debug", me.input.KEY.V);
            });
        }

        // Initialize the audio.
        me.audio.init("mp3,ogg");

        // Set a callback to run when loading is complete.
        me.loader.onload = this.loaded.bind(this);

        // Load the resources.
        me.loader.preload(game.resources);

        // Initialize melonJS and display a loading screen.
        me.state.change(me.state.LOADING);
    },

    // Run on game resources loaded.
    "loaded" : function() {
        me.state.set(me.state.MENU, new game.TitleScreen());
        me.state.set(me.state.PLAY, new game.PlayScreen());

        // add our player entity in the entity pool
        me.pool.register("mainRoboter", game.RoboterEntity);
        me.pool.register("mainStone", game.StoneEntity);

        // Start the game.
        me.state.change(me.state.PLAY);
    }
};
