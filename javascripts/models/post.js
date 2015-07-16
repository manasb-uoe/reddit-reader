/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "backbone",
    "reddit"
], function (Backbone, reddit) {
    "use strict";

    var PostModel = Backbone.Model.extend({
        vote: function () {
            reddit.vote({
                itemId: this.get("name"),
                voteDir: this.get("likes")
            });
        }
    });

    return PostModel;
});