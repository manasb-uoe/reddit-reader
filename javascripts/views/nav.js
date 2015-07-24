/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "collections/subreddits",
    "swig",
    "text!../../templates/sidebar.html",
    "text!../../templates/sidebar_menu.html",
    "bootstrap"
], function (_, $, Backbone, reddit, SubredditsCollection, swig, sidebarTemplate, sidebarMenu) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#sidebar",
        initialize: function () {
            this.defaultSubreddits = new SubredditsCollection({type: "defaults"});
            this.userSubreddits = new SubredditsCollection({type: "user"});
            this.popularSubreddits = new SubredditsCollection({type: "popular"});

            this.defaultSubreddits.on("reset", this.refreshSidebarMenu, this);
            this.userSubreddits.on("reset", this.refreshSidebarMenu, this);
            this.popularSubreddits.on("reset", this.refreshSidebarMenu, this);

            var self = this;
            $(document).on("access_token_expired", function () {
                self.render();
                Backbone.history.loadUrl();

                alert("Session has expired, please login again.");
            });
        },
        render: function () {
            this.$el.html(sidebarTemplate);

            this.$subredditInput = $("#subreddit-input");
            this.$menu = $("#menu-accordion");

            this.refreshSidebarMenu();

            this.defaultSubreddits.fetch();
            this.userSubreddits.fetch();
            this.popularSubreddits.fetch();

            var selectedTheme = localStorage.getItem("theme");
            if (selectedTheme) {
                this.switchTheme(selectedTheme);
            }

            $("#sidebar-button").click(function () {
                var $sidebarWrapper = $(".sidebar-wrapper");

                if ($sidebarWrapper.css("marginLeft") == "-200px") {
                    $sidebarWrapper.css("marginLeft", "0");
                    $("body").css("overflow", "hidden");
                } else {
                    $sidebarWrapper.css("marginLeft", "-200px");
                    $("body").css("overflow", "auto");
                }
            });
        },
        events: {
            "keypress #subreddit-input": "jumpToSubreddit",
            "click #subreddit-go-button": "jumpToSubreddit",
            "click #logout-button": "logout",
            "click #light-theme-button": function () {this.switchTheme("light")},
            "click #dark-theme-button": function () {this.switchTheme("dark")}
        },
        refreshSidebarMenu: function () {
            var user = reddit.getUser();
            var compiledTemplate = swig.render(sidebarMenu, {
                locals: {
                    username: user ? user.username : undefined,
                    authUrl: !user ? reddit.getAuthUrl() : undefined,
                    defaults: this.defaultSubreddits.toJSON(),
                    popular: this.popularSubreddits.toJSON(),
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
            reddit.deauth();

            this.render();

            Backbone.history.loadUrl();
        },
        switchTheme: function (type) {
            var $darkAppStyle = $("#dark-app-style");

            switch(type) {
                case "light":
                    $darkAppStyle.removeAttr("href");
                    localStorage.setItem("theme", type);
                    break;
                case "dark":
                    $darkAppStyle.attr("href", "stylesheets/index.dark.css");
                    localStorage.setItem("theme", type);
                    break;
                default:
                    throw new Error("Theme type can only be 'light' or 'dark'");
                    break;
            }
        }
    });

    return new NavView();
});