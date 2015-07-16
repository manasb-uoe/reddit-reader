/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "models/comment"
], function (_, $, Backbone, reddit, CommentModel) {
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
                error: function (jqXHR, textStatus, error) {
                    if (textStatus == "timeout") {
                        console.log("timed out");
                        setTimeout(function () {
                            self.fetch(subreddit, postId, sort);
                        }, 1000);

                    } else {
                        self.trigger("error", error);
                    }
                }
            });
        }

    });

    return new CommentsCollection();
});