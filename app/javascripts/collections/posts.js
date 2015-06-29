/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "models/post"
], function (_, $, Backbone, PostModel) {
    var PostsCollection = Backbone.Collection.extend({
        model: PostModel,
        fetch: function (subreddit) {
            var self = this;
            $.ajax({
                url: self.generateURL(subreddit),
                method: "GET",
                dataType: "json",
                success: function (response) {
                    var posts = [];
                    response.data.children.forEach(function (post) {
                        // replace default thumbnails with urls
                        if (["", "default", "self", "nsfw"].indexOf(post.data.thumbnail) > -1) {
                            post.data.thumbnail = undefined;
                        }
                        posts.push(post.data);
                    });

                    self.reset(posts);
                },
                error: function (error) {
                    self.trigger("error", error);
                }
            });
        },
        generateURL: function (subreddit) {
            var baseUrl = "http://www.reddit.com";
            subreddit = subreddit == "Front page" ?  "/" : "/r/" + subreddit;

            return baseUrl + subreddit + ".json";
        }
    });

    return new PostsCollection();
});