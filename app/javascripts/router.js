/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "backbone",
    "views/nav",
    "views/posts"
], function (_, Backbone, navView, postsView) {
    "use strict";

    var AppRouter = Backbone.Router.extend({
        routes: {
            "r/(:subreddit)": "showPosts",
            "*any": "defaultAction"
        },
        showPosts: function (subreddit) {
            postsView.render(subreddit);
        },
        defaultAction: function () {
            this.navigate("#/r/");
        }
    });

    var init = function () {
        // render navigation bar view
        navView.render();

        var appRouter = new AppRouter();

        // begin monitoring hashchange events and dispatching routes
        Backbone.history.start();
    };

    return {
        init: init
    }
});