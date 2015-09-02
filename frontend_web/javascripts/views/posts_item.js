/**
 * Created by ManasB on 6/30/2015.
 */

var $ = require("jquery");
var _ = require("underscore");
var Backbone = require("backbone");
var reddit = require("../reddit");
var contentViewer = require("./content_viewer");
var swig = require("swig");

var postItemTemplate = $("#posts-item-template").html();


var PostItemView = Backbone.View.extend({
    initialize: function (options) {
        this.model = options.model;
        this.shouldShowSelfText = options.shouldShowSelfText || false;

        this.model.on("change:likes", this.render, this);
    },
    render: function () {
        var compiledTemplate = swig.render(postItemTemplate, {locals: this.model.toJSON()});
        this.$el.html(compiledTemplate);

        if (this.shouldShowSelfText) {
            // decode comment self text and add to corresponding div
            var decodedSelfText = $("<div>").html(this.model.get("selftext_html")).text();
            this.$el.find(".self-text").html(decodedSelfText);
        } else {
            this.$el.find(".self-text").hide();
        }

        // render post content viewer with media or image preview when user clicks on the post thumbnail, giving
        // priority to embedded media
        var media = this.model.get("media");
        var preview = this.model.get("preview");

        this.$el.find(".thumbnail-link").click(function (event) {
            event.preventDefault();

            if (media) {
                contentViewer.render({media: media.oembed.html});
            } else if (preview) {
                contentViewer.render({preview: preview.images[0].source.url});
            }
        });

        return this;
    },
    events: {
        "click .upvote-button": "vote",
        "click .downvote-button": "vote"
    },
    vote: function (event) {
        if (reddit.getUser()) {
            var $scoreText = $(event.target).parents(".post").find(".score");

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


module.exports = PostItemView;