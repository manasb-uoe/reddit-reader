/**
 * Created by ManasB on 6/28/2015.
 */

/**
 * RequireJS configuration
 */

require.config({
    paths: {
        backbone: "./libs/backbone-min",
        underscore: "./libs/underscore-min",
        jquery: "./libs/jquery-2.1.4.min",
        swig: "./libs/swig.min",
        bootstrap: "./libs/bootstrap.min",
        text: "./libs/text"
    },
    shim: {
        backbone: {
            deps: ["underscore", "jquery"],
            exports: "Backbone"
        },
        bootstrap: {
            deps: ["jquery"]
        }
    }
});


/**
 * App entry point
 */

require([
    "router"
], function (App) {
    App.init();
});