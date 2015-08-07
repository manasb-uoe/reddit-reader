/**
 * Created by ManasB on 6/28/2015.
 */

var Backbone = require("backbone");
var reddit = require("../reddit");


var PostModel = Backbone.Model.extend({
    vote: function () {
        reddit.vote({
            itemId: this.get("name"),
            voteDir: this.get("likes")
        });
    }
});


module.exports = PostModel;