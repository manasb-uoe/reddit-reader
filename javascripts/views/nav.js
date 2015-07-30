/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "views/nav2",
    "collections/subreddits",
    "swig",
    "text!../../templates/sidebar.html",
    "text!../../templates/sidebar_menu.html",
    "bootstrap"
], function (_, $, Backbone, reddit, navView2, SubredditsCollection, swig, sidebarTemplate, sidebarMenu) {
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

            navView2.on("logout", this.render, this);
            navView2.on("toggle.sidebar", this.toggleSidebar, this);

            var self = this;
            $(document).on("access_token_expired", function () {
                self.render();
            });
        },
        render: function () {
            this.$el.html(sidebarTemplate);

            this.$menu = $("#menu-accordion");

            this.refreshSidebarMenu();

            this.defaultSubreddits.fetch();
            this.userSubreddits.fetch();
            this.popularSubreddits.fetch();

            var selectedTheme = localStorage.getItem("theme");
            if (selectedTheme) {
                this.switchTheme(selectedTheme);
            }
        },
        events: {
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
        },
        toggleSidebar: function () {
            var $sidebarWrapper = $(".sidebar-wrapper");
            var $contentWrapper = $(".content-wrapper");

            if ($sidebarWrapper.css("marginLeft") == "-215px") {
                $sidebarWrapper.css("marginLeft", "0");
                $contentWrapper.css("marginLeft", "215px");
            } else {
                $sidebarWrapper.css("marginLeft", "-215px");
                $contentWrapper.css("marginLeft", "0");
            }
        }
    });

    return new NavView();
});