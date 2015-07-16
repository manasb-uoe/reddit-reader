/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "swig",
    "text!../../templates/comments_item.html"
], function (_, $, Backbone, reddit, swig, postItemTemplate) {
    "use strict";

    var CommentsItemView = Backbone.View.extend({
        initialize: function (options) {
            this.postAuthor = options.postAuthor;
            this.mode = options.model;

            this.model.on("change:likes", this.render, this);
        },
        render: function () {
            // add left padding depending on comment level
            this.model.set("leftPadding", this.model.get("level") * 15);

            var compiledTemplate = swig.render(postItemTemplate, {locals: {comment: this.model.toJSON(), postAuthor: this.postAuthor}});
            this.$el.html(compiledTemplate);

            // decode comment body html and add to corresponding div
            var decodedBody = $("<div>").html(this.model.get("body")).text();
            this.$el.find(".body").html(decodedBody);

            return this;
        },
        events: {
            "click .upvote-button": "vote",
            "click .downvote-button": "vote"
        },
        vote: function (event) {
            if (reddit.getUser()) {
                var $scoreText = $(event.target).parents(".comment").find(".score");

                if ($(event.target).attr("class").indexOf("up") > -1) {
                    if (this.model.get("likes") == -1 || this.model.get("likes") == 0) {
                        this.model.set("likes", 1);
                        $scoreText.text(parseInt($scoreText.text()) + 1);
                    } else {
                        this.model.set("likes", 0);
                    }
                } else {
                    if (this.model.get("likes") == 1 || this.model.get("likes") == 0) {
                        this.model.set("likes", -1);
                    } else {
                        this.model.set("likes", 0);
                    }
                }

                this.model.vote();
            } else {
                alert("You are not authorized to perform this action.");
            }
        }
    });

    return CommentsItemView;
});