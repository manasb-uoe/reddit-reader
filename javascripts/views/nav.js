/**
 * Created by ManasB on 7/30/2015.
 */

define([
    "jquery",
    "backbone",
    "reddit",
    "swig",
    "text!../../templates/nav.html"
], function ($, Backbone, reddit, swig, navTemplate) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#nav",
        initialize: function () {
            this.on("logout", this.logout, this);

            var self = this;
            $(document).on("access_token_expired", function () {
                self.render();
                Backbone.history.loadUrl();

                alert("Session has expired, please login again.");
            });
        },
        render: function () {
            var user = reddit.getUser();
            var compiledTemplate = swig.render(navTemplate, {
                locals: {
                    username: user ? user.username : undefined,
                    authUrl: !user ? reddit.getAuthUrl() : undefined
                }
            });
            this.$el.html(compiledTemplate);

            this.$currentSubreddit = $("#current-subreddit");
            this.$subredditInput = $("#subreddit-input");
        },
        events: {
            "click #toggle-sidebar-button": function () {
                this.trigger("toggle.sidebar");
            },
            "keypress #subreddit-input": "jumpToSubreddit",
            "click #logout-button": function () {
                this.trigger("logout");
            }
        },
        updateCurrentSubreddit: function (subreddit) {
            var text = subreddit != "Front page" ? "Subreddit: " + "r/" + subreddit : subreddit;
            this.$currentSubreddit.text(text);
        },
        jumpToSubreddit: function (event) {
            if (event.which == 1 || event.which == 13) {
                event.preventDefault();
                Backbone.history.navigate("/r/" + this.$subredditInput.val(), {trigger: true});
            }
        },
        logout: function () {
            reddit.deauth();

            this.render();

            Backbone.history.loadUrl();
        }
    });

    return new NavView();
});