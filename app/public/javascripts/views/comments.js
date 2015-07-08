/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "jquery",
    "underscore",
    "backbone",
    "collections/comments",
    "views/comments_item",
    "views/posts_item",
    "views/thread_navigator",
    "models/post",
    "swig",
    "text!../../templates/comments_page.html",
    "text!../../templates/error.html"
], function ($, _, Backbone, commentsCollection, CommentsItemView, PostsItemView, ThreadNavigatorView, PostModel, swig, commentsTemplate, errorTemplate) {
    "use strict";

    var CommentsView = Backbone.View.extend({
        el: "#content",
        initialize: function () {
            commentsCollection.on("reset", this.addSelectedPost, this);
            commentsCollection.on("reset", this.addAllComments, this);
            commentsCollection.on("reset", this.initThreadNavigator, this);
            commentsCollection.on("error", this.showErrorMessage, this);
        },
        render: function (subreddit, postId, sort) {
            this.subreddit = subreddit;
            this.postId = postId;
            this.sort = sort;

            this.$el.html(commentsTemplate);

            this.$progressIndicator = $("#comments-progress-indicator");
            this.$commentsContainer = $("#comments-container");
            this.$errorContainer = $("#comments-error-container");
            this.$sortTabsContainer = $("#comments-sort-tabs-container");
            this.$postContainer = $("#post-container");

            this.reset();
        },
        addAllComments: function () {
            this.$progressIndicator.hide();
            this.$errorContainer.hide();

            this.$commentsContainer.empty();
            this.$commentsContainer.show();

            var self = this;
            commentsCollection.each(function (comment, pos) {
                if (pos != 0 && comment.get("level") == 0) {
                    self.$commentsContainer.append("<hr class='thread-separator'>");
                }
                self.addComment(comment);
            });
        },
        addComment: function (comment) {
            var commentsItem = new CommentsItemView({model: comment});
            this.$commentsContainer.append(commentsItem.render().el);
        },
        addSelectedPost: function () {
            var postItem = new PostsItemView({model: new PostModel(commentsCollection.post)});
            this.$postContainer.html(postItem.render().el);
        },
        initThreadNavigator: function () {
            var threadNavigator = new ThreadNavigatorView({comments: commentsCollection});
            threadNavigator.render();
            threadNavigator.setVisible(true);
        },
        showErrorMessage: function (error) {
            this.$progressIndicator.hide();
            this.$commentsContainer.hide();

            this.$errorContainer.html(swig.render(errorTemplate, {locals: {error: error.status}}));
            this.$errorContainer.show();
        },
        refreshSortTabs: function () {
            var tabs = ['best', 'top', 'new', 'controversial', 'old'];
            var template = [
                '{% for tab in tabs %}',
                '<li role="presentation" class="posts-sort-tab {% if tab == sort %} active {% endif %}">',
                '<a href="#/r/{{subreddit}}/comments/{{postId}}/{{tab}}">{{tab}}</a>',
                '</li>',
                '{% endfor %}'
            ].join('');
            this.$sortTabsContainer.html(swig.render(template, {locals: {subreddit: this.subreddit, postId: this.postId, sort: this.sort, tabs: tabs}}));
        },
        reset: function () {
            commentsCollection.fetch(this.subreddit, this.postId, this.sort);

            this.$progressIndicator.show();
            this.$commentsContainer.hide();
            this.refreshSortTabs();
        }
    });

    return new CommentsView();
});