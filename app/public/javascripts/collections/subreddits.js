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
            popular10: "http://www.reddit.com/subreddits/popular.json?limit=10",
            user: "/api/user/subreddits"
        },
        fetch: function () {
            this.type = this.type ? this.type : "default";

            var self = this;

            var successCallback = function (response) {
                if (self.type == "user") {
                    response = JSON.parse(response);
                }

                var subreddits = [];
                response.data.children.forEach(function (subreddit) {
                    subreddits.push(subreddit.data);
                });

                self.reset(subreddits);
            };

            if (this.type == "user") {
                if (localStorage.getItem("session")) {
                    $.ajax({
                        url: self.urls[this.type],
                        method: "POST",
                        dataType: "json",
                        data: {session: localStorage.getItem("session")},
                        success: successCallback
                    });
                } else {
                    self.reset();
                }
            } else {
                $.ajax({
                    url: self.urls[this.type],
                    method: "GET",
                    dataType: "json",
                    success: successCallback
                });
            }
        }
    });

    return SubredditsCollection;
});