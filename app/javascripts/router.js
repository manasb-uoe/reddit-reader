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
            "(:sort)": "showFrontPage",
            "r/:subreddit(/:sort)": "showSubreddit",
            "*any": "defaultAction"
        },
        showFrontPage: function (sort) {
            console.log("front: " + sort);
            var subreddit = "Front page";
            sort = sort != null ? sort : "hot";

            postsView.render(subreddit, sort);
        },
        showSubreddit: function (subreddit, sort) {
            subreddit = subreddit != null ? subreddit : "Front page";
            sort = sort != null ? sort : "hot";

            postsView.render(subreddit, sort);
        },
        defaultAction: function () {
            this.navigate("#/");
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