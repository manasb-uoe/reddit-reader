/**
 * Created by ManasB on 7/6/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "swig",
    "text!../../templates/comments_item.html"
], function (_, $, Backbone, swig, postItemTemplate) {
    "use strict";

    var CommentsItemView = Backbone.View.extend({
        render: function () {
            // add left padding depending on comment level
            this.model.set("leftPadding", this.model.get("level") * 15);

            var compiledTemplate = swig.render(postItemTemplate, {locals: this.model.toJSON()});
            this.$el.html(compiledTemplate);

            // decode comment body html before adding to document
            var decoded = $("<div>").html(this.model.get("body")).text();
            this.$el.find(".body").html(decoded);

            return this;
        }
    });

    return CommentsItemView;
});