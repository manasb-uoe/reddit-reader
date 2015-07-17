/**
 * Created by ManasB on 7/18/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "swig"
], function (_, $, Backbone, swig) {
    "use strict";

    var ContentViewer = Backbone.View.extend({
        el: "#content-viewer-container",
        template: "<div class='mask'></div>",
        render: function (options) {
            this.$el.html(this.template);

            if (options.media) {
                this.$el.find(".mask").html($("<div>").html(options.media).text());
            } else if (options.preview) {
                this.$el.find(".mask").html("<img class='img-responsive img-preview center-block' src='" + options.preview + "'>");
            }

            this.$el.show();
        },
        events: {
            "click .mask": function () {
                this.$el.hide();
                this.$el.empty();
            }
        }
    });

    return new ContentViewer();
});