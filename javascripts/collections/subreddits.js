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
        cacheTimeToLive: 86400000,
        model: SubredditModel,
        urls: {
            defaults: "/api/subreddits/defaults",
            user: "/api/subreddits/user",
            popular: "/api/subreddits/popular"
        },
        fetch: function () {
            // if user is not logged in, reset collection without content
            if (this.type == "user" && !localStorage.getItem("username")) {
                this.reset();
            } else {
                var cachedSubreddits = localStorage.getItem(this.type + "_subreddits");
                if (cachedSubreddits) {
                    cachedSubreddits = JSON.parse(cachedSubreddits);
                    // use cached subreddits if they are not older than 1 day
                    if ((Date.now() - cachedSubreddits.timestamp) <= this.cacheTimeToLive) {
                        this.reset(cachedSubreddits.subreddits);
                    } else {
                        this.fetchAjax();
                    }
                } else {
                    this.fetchAjax();
                }
            }
        },
        fetchAjax: function () {
            var self = this;
            $.ajax({
                url: self.type == "user" ? self.urls[this.type] + "?session=" + localStorage.getItem("session") : self.urls[this.type],
                method: "GET",
                dataType: "json",
                timeout: 6000,
                success: function (subreddits) {
                    self.reset(subreddits);

                    // cache subreddits along with a timestamp
                    var subredditsToCache = {timestamp: Date.now(), subreddits: subreddits};
                    localStorage.setItem(self.type + "_subreddits", JSON.stringify(subredditsToCache));
                },
                error: function (jqXHR, textStatus) {
                    if (textStatus == "timeout") {
                        setTimeout(function () {
                            self.fetch();
                        }, 1000);
                    } else {
                        console.log(textStatus);
                    }
                }
            });
        }
    });

    return SubredditsCollection;
});