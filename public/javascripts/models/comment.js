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
            $.post("/api/vote", {
                id: this.get("name"),
                dir: this.get("likes"),
                session: localStorage.getItem("session"),
                modhash: localStorage.getItem("modhash")
            });
        }
    });

    return CommentModel;
});