/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "reddit",
    "models/subreddit"
], function (_, $, Backbone, reddit, SubredditModel) {
    var SubredditsCollection = Backbone.Collection.extend({
        initialize: function (options) {
            this.type = options.type;
        },
        cacheTimeToLive: 86400000,
        model: SubredditModel,
        fetch: function () {
            var self = this;
            var getSubreddits = function () {
                reddit.getSubreddits({
                    type: self.type,
                    success: function (response) {
                        self.reset(response);

                        // cache subreddits along with a timestamp
                        var subredditsToCache = {timestamp: Date.now(), subreddits: response};
                        localStorage.setItem(self.type + "_subreddits", JSON.stringify(subredditsToCache));
                    },
                    error: function (jqXHR, textStatus, err) {
                        if (textStatus == "timeout") {
                            setTimeout(function () {
                                self.fetch();
                            }, 1000);
                        } else {
                            console.log(textStatus);
                        }
                    }
                });
            };

            // if user is not logged in, reset collection without content
            if (this.type == "user" && !reddit.getUser()) {
                this.reset();
            } else {
                var cachedSubreddits = localStorage.getItem(this.type + "_subreddits");
                if (cachedSubreddits) {
                    cachedSubreddits = JSON.parse(cachedSubreddits);
                    // use cached subreddits if they are not older than 1 day
                    if ((Date.now() - cachedSubreddits.timestamp) <= this.cacheTimeToLive) {
                        this.reset(cachedSubreddits.subreddits);
                    } else {
                        getSubreddits();
                    }
                } else {
                    getSubreddits();
                }
            }
        }
    });

    return SubredditsCollection;
});