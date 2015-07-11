/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/subreddits",
    "views/login_modal",
    "swig",
    "text!../../templates/sidebar.html",
    "text!../../templates/sidebar_menu.html",
    "bootstrap"
], function (_, $, Backbone, SubredditsCollection, loginModalView, swig, sidebarTemplate, sidebarMenu) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#sidebar",
        initialize: function () {
            this.defaultSubreddits = new SubredditsCollection({type: "defaults"});
            this.userSubreddits = new SubredditsCollection({type: "user"});

            loginModalView.on("login.success", this.render, this);
            this.defaultSubreddits.on("reset", this.refreshSidebarMenu, this);
            this.userSubreddits.on("reset", this.refreshSidebarMenu, this);
        },
        render: function () {
            this.$el.html(sidebarTemplate);

            this.$subredditInput = $("#subreddit-input");
            this.$menu = $("#menu-accordion");

            this.refreshSidebarMenu();

            this.defaultSubreddits.fetch();
            this.userSubreddits.fetch();
        },
        events: {
            "keypress #subreddit-input": "jumpToSubreddit",
            "click #subreddit-go-button": "jumpToSubreddit",
            "click #logout-button": "logout"
        },
        refreshSidebarMenu: function () {
            var compiledTemplate = swig.render(sidebarMenu, {
                locals: {
                    username: localStorage.getItem("username"),
                    defaults: this.defaultSubreddits.toJSON(),
                    subs: this.userSubreddits.toJSON()
                }
            });
            this.$menu.html(compiledTemplate);
        },
        jumpToSubreddit: function (event) {
            if (event.which == 1 || event.which == 13) {
                event.preventDefault();
                Backbone.history.navigate("/r/" + this.$subredditInput.val(), {trigger: true});
            }
        },
        logout: function () {
            // clear cache
            localStorage.clear();

            this.render();

            Backbone.history.loadUrl();
        }
    });

    return new NavView();
});