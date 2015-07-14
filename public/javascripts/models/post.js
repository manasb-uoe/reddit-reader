/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "jquery",
    "underscore",
    "backbone"
], function ($, _, Backbone) {
    "use strict";

    var PostModel = Backbone.Model.extend({
        vote: function () {
            $.post("/api/vote", {
                id: this.get("name"),
                dir: this.get("likes"),
                session: localStorage.getItem("session"),
                modhash: localStorage.getItem("modhash")
            });
        }
    });

    return PostModel;
});