/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "backbone",
    "models/subreddit",
    "localStorage"
], function (_, Backbone, SubredditModel) {
    var FavouriteSubredditsCollection = Backbone.Collection.extend({
        model: SubredditModel,
        localStorage: new Backbone.LocalStorage("RedditReaderLocalStorage"),
        fetch: function () {
            Backbone.Collection.prototype.fetch.call(this);

            this.trigger("reset");
        }
    });

    return new FavouriteSubredditsCollection();
});