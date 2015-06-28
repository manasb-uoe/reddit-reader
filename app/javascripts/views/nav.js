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
            favouritesCollection.on("selected", this.updateSelectedSubreddit, this);
            favouritesCollection.on("selected", this.refreshSubredditsDropdown, this);
        },
        render: function () {
            var compiledTemplate = swig.render(navBarTemplate);
            this.$el.html(compiledTemplate);

            this.$dropdownSubredditsContainer = $("#subreddits-dropdown-container");
            this.$selectedSubreddit = $("#selected-subreddit");

            favouritesCollection.fetch();
        },
        events: {
            "click .subreddits-dropdown-item": "subredditsDropdownItemClicked"
        },
        refreshSubredditsDropdown: function () {
            this.$dropdownSubredditsContainer.empty();

            var self = this;
            favouritesCollection.each(function (subreddit, pos) {
                if (pos == 0) {
                    var header = '<li class="dropdown-header">FAVOURITE SUBREDDITS</li>'
                    self.$dropdownSubredditsContainer.append(header);
                }

                var template = "<li {% if isSelected %} class='active' {% endif %}><a class='subreddits-dropdown-item' data-name='{{ display_name }}'>{{ display_name }}</a></li>";
                var compiledTemplate = swig.render(template, {locals: subreddit.toJSON()});
                self.$dropdownSubredditsContainer.append(compiledTemplate);

                if (pos == favouritesCollection.length-1) {
                    var addFavouriteTemplate = [
                        '<li role="separator" class="divider"></li>',
                        '<li><a id="add-favourite-button"><span class="glyphicon glyphicon-plus"></span>  Add Favourite</a></li>'
                    ].join('');
                    self.$dropdownSubredditsContainer.append(addFavouriteTemplate);
                }
            });

            this.updateSelectedSubreddit();
        },
        updateSelectedSubreddit: function () {
            var selectedSubredditName = favouritesCollection.filter(function (subreddit) {
                return subreddit.get("isSelected");
            })[0].get("display_name");

            this.$selectedSubreddit.text(selectedSubredditName);
        },
        subredditsDropdownItemClicked: function (event) {
            var clickedSubreddit = favouritesCollection.filter(function (subreddit) {
                return subreddit.get("display_name") == $(event.target).attr("data-name");
            })[0];

            favouritesCollection.each(function (subreddit) {
                if (subreddit != clickedSubreddit) {
                    subreddit.set("isSelected", false);
                }
            });

            clickedSubreddit.set("isSelected", true);

            favouritesCollection.trigger("selected");
        }
    });

    return new NavView();
});