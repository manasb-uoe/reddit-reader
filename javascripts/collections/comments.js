/**
 * Created by ManasB on 7/6/2015.
 */

var _ = require("underscore");
var $ = require("jquery");
var Backbone = require("backbone");
var reddit = require("../reddit");
var CommentModel = require("../models/comment");


var CommentsCollection = Backbone.Collection.extend({
    model: CommentModel,
    fetch: function (subreddit, postId, sort) {
        var self = this;
        reddit.getComments({
            subreddit: subreddit,
            id: postId,
            sort: sort,
            success: function (response) {
                self.post = response.post;
                self.reset(response.comments);
            },
            error: function (err, textStatus) {
                if (textStatus == "timeout") {
                    console.log("timed out");
                    setTimeout(function () {
                        self.fetch(subreddit, postId, sort);
                    }, 1000);

                } else {
                    self.trigger("error", err);
                }
            }
        });
    }

});


module.exports = new CommentsCollection();