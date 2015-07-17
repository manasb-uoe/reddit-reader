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
        fetch: function (subreddit, sort, shouldLoadMore, user) {
            // reset 'after' value if subreddit, sort or user has been changed
            if (this.subreddit != subreddit || this.sort != sort || this.user != user) {
                this.after = undefined;
            }

            this.subreddit = subreddit;
            this.sort = sort;
            this.shouldLoadMore = shouldLoadMore;
            this.user = user;

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
                error: function (err, textStatus) {
                    if (textStatus == "timeout") {
                        console.log("timed out");
                        setTimeout(function () {
                            self.fetch(subreddit ,sort, shouldLoadMore, user);
                        }, 1000);

                    } else {
                        self.trigger("error", err);
                    }
                }
            });
        }
    });

    return new PostsCollection();
});