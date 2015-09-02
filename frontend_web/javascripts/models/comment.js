/**
 * Created by ManasB on 7/6/2015.
 */

var Backbone = require("backbone");
var reddit = require("../reddit");


var CommentModel = Backbone.Model.extend({
    vote: function () {
        reddit.vote({
            itemId: this.get("name"),
            voteDir: this.get("likes")
        });
    }
});


module.exports = CommentModel;