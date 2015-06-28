/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "backbone",
    "views/nav"
], function (_, Backbone, navView) {
    "use strict";

    var AppRouter = Backbone.Router.extend({
        routes: {
            "": "showIndex"
        },
        showIndex: function () {
            console.log("render index page");
        }
    });

    var init = function () {
        var appRouter = new AppRouter();

        // render navigation bar view
        navView.render();

        // begin monitoring hashchange events and dispatching routes
        Backbone.history.start();
    };

    return {
        init: init
    }
});