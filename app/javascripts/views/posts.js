/**
 * Created by ManasB on 6/29/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/posts",
    "views/posts_item",
    "swig",
    "text!../../templates/posts_page.html",
    "text!../../templates/error.html"

], function (_, $, Backbone, postsCollection, PostItemView, swig, postsTemplate, errorTemplate) {
    "use strict";

    var PostsView = Backbone.View.extend({
        el: "#content",
        initialize: function () {
            postsCollection.on("reset", this.addAllPosts, this);
            postsCollection.on("error", this.showErrorMessage, this);
        },
        render: function (subreddit, sort) {
            this.$el.html(postsTemplate);

            this.$progressIndicator = $("#progress-indicator");
            this.$postsContainer = $("#posts-container");
            this.$errorContainer = $("#posts-error-container");
            this.$currentSubreddit = $("#current-subreddit");
            this.$sortTabsContainer = $("#posts-sort-tabs-container");

            postsCollection.fetch(subreddit, sort);

            this.$currentSubreddit.text(subreddit);

            this.refreshSortTabs(subreddit, sort);
        },
        refreshSortTabs: function (subreddit, sort) {
            var tabs = ['hot', 'new', 'rising', 'controversial', 'top'];
            var template = [
                '{% for tab in tabs %}',
                '<li role="presentation" class="posts-sort-tab {% if tab == sort %} active {% endif %}">',
                '<a {% if subreddit == "Front page" %} href="#/{{tab}}" {% else %} href="#/r/{{subreddit}}/{{tab}}" {% endif %}>{{tab}}</a>',
                '</li>',
                '{% endfor %}'
            ].join('');
            this.$sortTabsContainer.html(swig.render(template, {locals: {subreddit: subreddit, sort: sort, tabs: tabs}}));
        },
        addAllPosts: function () {
            this.$progressIndicator.hide();
            this.$postsContainer.empty();
            this.$postsContainer.show();

            var self = this;
            postsCollection.each(function (post) {
                self.addPost(post);
            });
        },
        addPost: function (post) {
            var postItem = new PostItemView({model: post});
            this.$postsContainer.append(postItem.render().el);
        },
        showErrorMessage: function (error) {
            this.$progressIndicator.hide();
            this.$postsContainer.hide();

            this.$errorContainer.html(swig.render(errorTemplate, {locals: {error: error.status}}));
            this.$errorContainer.show();
        }
    });

    return new PostsView();
});