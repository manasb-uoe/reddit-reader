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
        model: SubredditModel,
        urls: {
            default: "http://www.reddit.com/subreddits/default.json?limit=100",
            popular10: "http://www.reddit.com/subreddits/popular.json?limit=10"
        },
        fetch: function (where) {
            where = where ? where : "default";

            var self = this;
            $.ajax({
                url: self.urls[where],
                method: "GET",
                dataType: "json",
                success: function (response) {
                    var subreddits = [{display_name: "Front page", isSelected: true}];
                    response.data.children.forEach(function (subreddit) {
                        subreddits.push(subreddit.data);
                    });
                    self.reset(subreddits);
                }
            });
        }
    });

    return new SubredditsCollection();
});