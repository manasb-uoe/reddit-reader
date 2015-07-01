/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/favourite_subreddits",
    "swig",
    "text!../../templates/navigation_bar.html",
    "bootstrap"
], function (_, $, Backbone, favouritesCollection, swig, navBarTemplate) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#navigation-bar-container",
        initialize: function () {
            favouritesCollection.on("reset", this.refreshSubredditsDropdown, this);
        },
        render: function () {
            var compiledTemplate = swig.render(navBarTemplate);
            this.$el.html(compiledTemplate);

            this.$dropdownSubredditsContainer = $("#subreddits-dropdown-container");

            favouritesCollection.fetch();
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
        }
    });

    return new NavView();
});