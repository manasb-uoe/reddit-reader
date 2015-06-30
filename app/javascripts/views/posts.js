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
        render: function (subreddit) {
            this.$el.html(postsTemplate);

            this.$progressIndicator = $("#progress-indicator");
            this.$postsContainer = $("#posts-container");
            this.$errorContainer = $("#posts-error-container");
            this.$currentSubreddit = $("#current-subreddit");

            postsCollection.fetch(subreddit);

            this.$currentSubreddit.text(subreddit);
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