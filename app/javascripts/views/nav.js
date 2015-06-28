/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/subreddits",
    "swig",
    "text!../../templates/navigation_bar.html",
    "bootstrap"
], function (_, $, Backbone, subredditsCollection, swig, navBarTemplate) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#navigation-bar-container",
        initialize: function () {
            subredditsCollection.on("reset", this.refreshSubredditsDropdown, this);
            subredditsCollection.on("selected", this.updateSelectedSubreddit, this);
            subredditsCollection.on("selected", this.refreshSubredditsDropdown, this);
        },
        render: function () {
            var compiledTemplate = swig.render(navBarTemplate);
            this.$el.html(compiledTemplate);

            this.$dropdownSubredditsContainer = $("#subreddits-dropdown-container");
            this.$selectedSubreddit = $("#selected-subreddit");

            subredditsCollection.fetch("popular10");
        },
        events: {
            "click .subreddits-dropdown-item": "subredditsDropdownItemClicked"
        },
        refreshSubredditsDropdown: function () {
            this.$dropdownSubredditsContainer.empty();

            var self = this;
            subredditsCollection.each(function (subreddit) {
                var template = "<li {% if isSelected %} class='active' {% endif %}><a class='subreddits-dropdown-item' data-name='{{ display_name }}'>{{ display_name }}</a></li>";
                var compiledTemplate = swig.render(template, {locals: subreddit.toJSON()});
                self.$dropdownSubredditsContainer.append(compiledTemplate);
            });

            this.updateSelectedSubreddit();
        },
        updateSelectedSubreddit: function () {
            var selectedSubredditName = subredditsCollection.filter(function (subreddit) {
                return subreddit.get("isSelected");
            })[0].get("display_name");

            this.$selectedSubreddit.text(selectedSubredditName);
        },
        subredditsDropdownItemClicked: function (event) {
            var clickedSubreddit = subredditsCollection.filter(function (subreddit) {
                return subreddit.get("display_name") == $(event.target).attr("data-name");
            })[0];

            subredditsCollection.each(function (subreddit) {
                if (subreddit != clickedSubreddit) {
                    subreddit.set("isSelected", false);
                }
            });

            clickedSubreddit.set("isSelected", true);

            subredditsCollection.trigger("selected");
        }
    });

    return new NavView();
});