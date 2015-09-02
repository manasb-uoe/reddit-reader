/**
 * Created by ManasB on 6/28/2015.
 */

var $ = require("jquery");
var _ = require("underscore");
var Backbone = require("backbone");
var reddit = require("../reddit");
var navView = require("./nav");
var SubredditsCollection = require("../collections/subreddits");
var swig = require("swig");

var sidebarTemplate = $("#sidebar-template").html();
var sidebarMenuTemplate = $("#sidebar-menu-template").html();


var SidebarView = Backbone.View.extend({
    el: "#sidebar",
    initialize: function () {
        this.defaultSubreddits = new SubredditsCollection({type: "defaults"});
        this.userSubreddits = new SubredditsCollection({type: "user"});
        this.popularSubreddits = new SubredditsCollection({type: "popular"});

        this.defaultSubreddits.on("reset", this.refreshSidebarMenu, this);
        this.userSubreddits.on("reset", this.refreshSidebarMenu, this);
        this.popularSubreddits.on("reset", this.refreshSidebarMenu, this);

        navView.on("logout", this.render, this);

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
        var compiledTemplate = swig.render(sidebarMenuTemplate, {
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
        var $navbar = $("#navigation-bar");

        switch(type) {
            case "light":
                $darkAppStyle.removeAttr("href");
                $navbar.removeClass("navbar-inverse");
                $navbar.addClass("navbar-default");

                localStorage.setItem("theme", type);
                break;
            case "dark":
                $darkAppStyle.attr("href", "stylesheets/index.dark.css");
                $navbar.addClass("navbar-inverse");
                $navbar.removeClass("navbar-default");

                localStorage.setItem("theme", type);
                break;
            default:
                throw new Error("Theme type can only be 'light' or 'dark'");
                break;
        }
    }
});


module.exports = new SidebarView();