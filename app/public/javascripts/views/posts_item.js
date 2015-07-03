/**
 * Created by ManasB on 6/30/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "swig",
    "text!../../templates/posts_item.html"
], function (_, $, Backbone, swig, postItemTemplate) {
    "use strict";

    var PostItemView = Backbone.View.extend({
        initialize: function () {
            this.model.on("change:likes", this.render, this);
        },
        render: function () {
            var compiledTemplate = swig.render(postItemTemplate, {locals: this.model.toJSON()});
            this.$el.html(compiledTemplate);

            return this;
        },
        events: {
            "click .upvote-button": "vote",
            "click .downvote-button": "vote"
        },
        vote: function (event) {
            if ($(event.target).attr("class").indexOf("up") > -1) {
                if (this.model.get("likes") == -1 || this.model.get("likes") == 0) {
                    this.model.set("likes", 1);
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

            $.ajax({
                url: "/api/vote/",
                method: "POST",
                data: {id: this.model.get("name"), dir: this.model.get("likes"), username: localStorage.getItem("username")},
                success: function () {
                    console.log("success");
                },
                error: function (err) {
                    console.log(err);
                }
            });
        }

    });

    return PostItemView;
});