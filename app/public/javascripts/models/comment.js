/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "underscore",
    "backbone"
], function (_, Backbone) {
    "use strict";

    var CommentModel = Backbone.Model.extend({
        vote: function () {
            console.log("voting ajax");
            console.log({
                id: this.get("name"),
                dir: this.get("likes"),
                username: localStorage.getItem("username")
            });
            $.post("/api/vote", {
                id: this.get("name"),
                dir: this.get("likes"),
                username: localStorage.getItem("username")
            });
        }
    });

    return CommentModel;
});