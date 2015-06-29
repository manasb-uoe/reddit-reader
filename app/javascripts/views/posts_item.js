/**
 * Created by ManasB on 6/30/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "swig",
    "text!../../templates/posts_item.html"
], function (_, $, Backbone, swig, postItemTemplate) {
    "use strict";

    var PostItemView = Backbone.View.extend({
        render: function () {
            var compiledTemplate = swig.render(postItemTemplate, {locals: this.model.toJSON()});
            this.$el.html(compiledTemplate);

            return this;
        }
    });

    return PostItemView;
});