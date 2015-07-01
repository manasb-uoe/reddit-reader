/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "models/post",
    "moment"
], function (_, $, Backbone, PostModel, moment) {
    var PostsCollection = Backbone.Collection.extend({
        model: PostModel,
        initialize: function () {
            this.subreddit = null;
            this.sort = null;
            this.after = null;
        },
        fetch: function (subreddit, sort) {
            // reset after value if subreddit or sort has been changed
            if (this.subreddit != subreddit || this.sort != sort) {
                this.after = null;
                console.log("setting after to null");
            }

            this.subreddit = subreddit;
            this.sort = sort;

            var self = this;
            $.ajax({
                url: self.generateURL(),
                method: "GET",
                dataType: "json",
                success: function (response) {
                    var posts = [];
                    response.data.children.forEach(function (post) {
                        // replace default thumbnails with urls
                        if (["", "default", "self", "nsfw"].indexOf(post.data.thumbnail) > -1) {
                            post.data.thumbnail = undefined;
                        }

                        // humanize timestamp
                        post.data.created_utc = moment.utc(moment.unix(post.data.created_utc)).locale("en").fromNow();

                        posts.push(post.data);
                    });

                    if (self.after != null) {
                        self.add(posts);
                    } else {
                        self.reset(posts);
                    }

                    self.after = response.data.after;

                    if (self.after == null) {
                        self.trigger("no.more.posts.to.load");
                    }
                },
                error: function (error) {
                    self.trigger("error", error);
                }
            });
        },
        generateURL: function () {
            var baseUrl = "http://www.reddit.com";
            var subreddit = this.subreddit == "Front page" ?  "/" : "/r/" + this.subreddit;

            var url = baseUrl + subreddit + "/" + this.sort + ".json";

            if (this.after != null) {
                url = url + "?after=" + this.after;
            }

            console.log(url);

            return url;
        }
    });

    return new PostsCollection();
});