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
            defaults: "/api/subreddits/defaults",
            user: "/api/subreddits/user"
        },
        fetch: function () {
            // if user is not logged in, reset collection without content
            if (this.type == "user" && !localStorage.getItem("username")) {
                this.reset();
                console.log("NO USER LOL");
            } else {
                var self = this;
                $.ajax({
                    url: self.type == "user" ? self.urls[this.type] + "?session=" + localStorage.getItem("session") : self.urls[this.type],
                    method: "GET",
                    dataType: "json",
                    timeout: 6000,
                    success: function (subreddits) {
                        self.reset(subreddits);
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
        }
    });

    return SubredditsCollection;
});