/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "models/post"
], function (_, $, Backbone, reddit, PostModel) {
    var PostsCollection = Backbone.Collection.extend({
        model: PostModel,
        fetch: function (subreddit, sort, shouldLoadMore) {
            // reset 'after' value if subreddit or sort has been changed
            if (this.subreddit != subreddit || this.sort != sort) {
                this.after = undefined;
            }

            this.subreddit = subreddit;
            this.sort = sort;
            this.shouldLoadMore = shouldLoadMore;

            var self = this;
            reddit.getPosts({
                subreddit: this.subreddit == "Front page" ? undefined : this.subreddit,
                sort: this.sort,
                after: this.after,
                success: function (response) {
                    if (self.shouldLoadMore) {
                        if (self.after != null) {
                            self.add(response.posts);
                        }
                    } else {
                        self.reset(response.posts);
                    }

                    self.after = response.after;

                    if (self.after == null) {
                        self.trigger("no.more.posts.to.load");
                    }
                },
                error: function (jqXHR, textStatus, error) {
                    if (textStatus == "timeout") {
                        console.log("timed out");
                        setTimeout(function () {
                            self.fetchPostsAjax(postsUrl);
                        }, 1000);

                    } else {
                        self.trigger("error", error);
                    }
                }
            });
        }
    });

    return new PostsCollection();
});