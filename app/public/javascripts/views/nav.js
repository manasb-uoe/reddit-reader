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
    "text!../../templates/navigation_bar.html",
    "text!../../templates/navigation_drawer.html",
    "bootstrap"
], function (_, $, Backbone, favouritesCollection, SubredditsCollection, loginModalView, swig, navBarTemplate, drawerTemplate) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#navigation-bar-container",
        initialize: function () {
            this.popularSubreddits = new SubredditsCollection({type: "popular10"});
            this.defaultSubreddits = new SubredditsCollection({type: "default"});
            this.userSubreddits = new SubredditsCollection({type: "user"});

            favouritesCollection.on("reset", this.refreshSubredditsDropdown, this);
            loginModalView.on("login.success", this.render, this);
            this.popularSubreddits.on("reset", this.refreshDrawerContent, this);
            this.defaultSubreddits.on("reset", this.refreshDrawerContent, this);
            this.userSubreddits.on("reset", this.refreshDrawerContent, this);
        },
        render: function () {
            var compiledTemplate = swig.render(navBarTemplate, {locals: {username: localStorage.getItem("username")}});
            this.$el.html(compiledTemplate);

            this.$dropdownSubredditsContainer = $("#subreddits-dropdown-container");
            this.$drawer = $("#navigation-drawer-container");
            this.$brandIcon = $(".brand-glyphicon");

            favouritesCollection.fetch();
            this.popularSubreddits.fetch();
            this.defaultSubreddits.fetch();
            this.userSubreddits.fetch();

            this.isDrawerVisible = false;
        },
        events: {
            "click .navbar-brand": "toggleDrawer"
        },
        refreshSubredditsDropdown: function () {
            this.$dropdownSubredditsContainer.empty();

            var self = this;
            favouritesCollection.each(function (subreddit, pos) {
                if (pos == 0) {
                    var header = '<li class="dropdown-header">FAVOURITE SUBREDDITS</li>'
                    self.$dropdownSubredditsContainer.append(header);
                }

                var template = "<li><a {% if display_name == \'Front page\'%} href = '#/r/' {% else%} href = '#/r/{{ display_name }}' {% endif %} class='subreddits-dropdown-item' data-name='{{ display_name }}'>{{ display_name }}</a></li>";
                var compiledTemplate = swig.render(template, {locals: subreddit.toJSON()});
                self.$dropdownSubredditsContainer.append(compiledTemplate);
            });

            if (favouritesCollection.length > 0) {
                var separator = '<li role="separator" class="divider"></li>';
                this.$dropdownSubredditsContainer.append(separator);
            }

            var addFavouriteTemplate = '<li><a id="add-favourite-button"><span class="glyphicon glyphicon-plus"></span>  Add Favourite</a></li>';
            this.$dropdownSubredditsContainer.append(addFavouriteTemplate);
        },
        toggleDrawer: function () {
            if (this.isDrawerVisible) {
                this.$drawer.animate({
                    marginLeft: "-300px"
                }, 300);

                this.$brandIcon.addClass("glyphicon-menu-hamburger");
                this.$brandIcon.removeClass("glyphicon-chevron-left");

                this.isDrawerVisible = false;
            } else {
                this.$drawer.animate({
                    marginLeft: "0"
                }, 300);

                this.$brandIcon.removeClass("glyphicon-menu-hamburger");
                this.$brandIcon.addClass("glyphicon-chevron-left");

                this.isDrawerVisible = true;
            }
        },
        refreshDrawerContent: function () {
            var compiledTemplate = swig.render(drawerTemplate, {
                locals: {
                    categories: {
                        "My Subscriptions": this.userSubreddits.toJSON(),
                        "Popular Subreddits": this.popularSubreddits.toJSON(),
                        "Default Subreddits": this.defaultSubreddits.toJSON()
                    }
                }
            });
            this.$drawer.html(compiledTemplate);
        }
    });

    return new NavView();
});