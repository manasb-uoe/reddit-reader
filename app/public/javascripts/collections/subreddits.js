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
            default: "/api/subreddits/default",
            popular10: "/api/subreddits/popular10",
            user: "/api/user/subreddits"
        },
        fetch: function () {
            this.type = this.type ? this.type : "default";

            var self = this;
            $.ajax({
                url: self.type == "user" ? self.urls[this.type] + "?username=" + localStorage.getItem("username") : self.urls[this.type],
                method: "GET",
                dataType: "json",
                timeout: 3000,
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
        },
        fetchSubredditsAjax: function () {

        }
    });

    return SubredditsCollection;
});