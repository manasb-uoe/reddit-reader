/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "backbone",
    "reddit"
], function (Backbone, reddit) {
    "use strict";

    var CommentModel = Backbone.Model.extend({
        vote: function () {
            reddit.vote({
                itemId: this.get("name"),
                voteDir: this.get("likes")
            });
        }
    });

    return CommentModel;
});