/**
 * Created by ManasB on 6/29/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "collections/posts",
    "views/posts_item",
    "views/login_modal",
    "swig",
    "text!../../templates/posts_page.html",
    "text!../../templates/error.html"

], function (_, $, Backbone, postsCollection, PostItemView, loginModalView, swig, postsTemplate, errorTemplate) {
    "use strict";

    var PostsView = Backbone.View.extend({
        el: "#content",
        initialize: function () {
            this.subreddit = null;
            this.sort = null;

            postsCollection.on("reset", this.addAllPosts, this);
            postsCollection.on("update", this.addAllPosts, this);
            postsCollection.on("error", this.showErrorMessage, this);
            postsCollection.on("no.more.posts.to.load", this.allPostsLoaded, this);
            loginModalView.on("login.success", this.reset, this);
        },
        render: function (subreddit, sort) {
            this.subreddit = subreddit;
            this.sort = sort;

            this.$el.html(postsTemplate);

            this.$progressIndicator = $("#progress-indicator");
            this.$postsContainer = $("#posts-container");
            this.$errorContainer = $("#posts-error-container");
            this.$currentSubreddit = $("#current-subreddit");
            this.$sortTabsContainer = $("#posts-sort-tabs-container");
            this.$morePostsButton = $("#more-posts-button");

            this.reset();
        },
        events: {
            "click #more-posts-button": "loadMorePosts"
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

            this.$morePostsButton.prop("disabled", false);
            this.$morePostsButton.html("Load more");
            this.$morePostsButton.show();

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
        },
        loadMorePosts: function () {
            postsCollection.fetch(this.subreddit, this.sort);
            this.$morePostsButton.prop("disabled", true);
            this.$morePostsButton.html("Loading...");
        },
        allPostsLoaded: function () {
            this.$morePostsButton.html("No more posts to load :(");
            this.$morePostsButton.prop("disabled", true);
            console.log("disabled");
        },
        updateCurrentSubreddit: function () {
            var text = this.subreddit != "Front page" ? "Subreddit: " + "r/" + this.subreddit : this.subreddit;
            this.$currentSubreddit.text(text);
        },
        reset: function () {
            postsCollection.fetch(this.subreddit, this.sort);

            this.$progressIndicator.show();
            this.$postsContainer.hide();
            this.updateCurrentSubreddit();
            this.refreshSortTabs(this.subreddit, this.sort);
            this.$morePostsButton.hide();
        }
    });

    return new PostsView();
});