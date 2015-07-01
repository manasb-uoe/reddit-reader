/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "models/subreddit"
], function (_, $, Backbone, SubredditModel) {
    var SubredditsCollection = Backbone.Collection.extend({
        initialize: function (options) {
            this.type = options.type;
        },
        model: SubredditModel,
        urls: {
            default: "http://www.reddit.com/subreddits/default.json?limit=100",
            popular10: "http://www.reddit.com/subreddits/popular.json?limit=10"
        },
        fetch: function () {
            this.type = this.type ? this.type : "default";

            var self = this;
            $.ajax({
                url: self.urls[this.type],
                method: "GET",
                dataType: "json",
                success: function (response) {
                    var subreddits = [];
                    response.data.children.forEach(function (subreddit) {
                        subreddits.push(subreddit.data);
                    });
                    self.reset(subreddits);
                }
            });
        }
    });

    return SubredditsCollection;
});