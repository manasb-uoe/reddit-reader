/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/favourite_subreddits",
    "collections/subreddits",
    "views/login_modal",
    "swig",
    "text!../../templates/sidebar.html",
    "text!../../templates/sidebar_menu.html",
    "bootstrap"
], function (_, $, Backbone, favouritesCollection, SubredditsCollection, loginModalView, swig, sidebarTemplate, sidebarMenu) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#sidebar",
        initialize: function () {
            this.popularSubreddits = new SubredditsCollection({type: "popular10"});
            this.defaultSubreddits = new SubredditsCollection({type: "default"});
            this.userSubreddits = new SubredditsCollection({type: "user"});

            loginModalView.on("login.success", this.render, this);
            this.popularSubreddits.on("reset", this.refreshSidebarMenu, this);
            this.defaultSubreddits.on("reset", this.refreshSidebarMenu, this);
            this.userSubreddits.on("reset", this.refreshSidebarMenu, this);
        },
        render: function () {
            this.$el.html(sidebarTemplate);

            this.$subredditInput = $("#subreddit-input");
            this.$menu = $("#menu-accordion");

            this.refreshSidebarMenu();

            this.defaultSubreddits.fetch();
            if (localStorage.getItem("username")) {
                this.userSubreddits.fetch();
            }
        },
        events: {
            "keypress #subreddit-input": "jumpToSubreddit",
            "click #subreddit-go-button": "jumpToSubreddit"
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
        }
    });

    return new NavView();
});