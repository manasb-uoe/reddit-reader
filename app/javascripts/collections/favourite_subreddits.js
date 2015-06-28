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

            // add Front page as the first item
            if (this.length == 0) {
                var frontPage = new SubredditModel({display_name: "Front page", isSelected: true});
                this.add(frontPage, {at: 0});
                frontPage.save();
            }

            this.trigger("reset");
        }
    });

    return new FavouriteSubredditsCollection();
});