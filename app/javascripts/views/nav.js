/**
 * Created by ManasB on 6/28/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "swig",
    "text!../../templates/navigation_bar.html",
    "bootstrap"
], function (_, $, Backbone, swig, navBarTemplate) {
    "use strict";

    var NavView = Backbone.View.extend({
        el: "#navigation-bar-container",
        render: function () {
            var compiledTemplate = swig.render(navBarTemplate);
            this.$el.html(compiledTemplate);
        }
    });

    return new NavView();
});