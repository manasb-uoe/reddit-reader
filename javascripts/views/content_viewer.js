/**
 * Created by ManasB on 7/18/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone"
], function (_, $, Backbone) {
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

            // prevent body scrolling while content viewer is being displayed
            $("body").css({
                overflow: "hidden"
            });
        },
        events: {
            "click .mask": function () {
                this.$el.hide();
                this.$el.empty();

                $("body").css({
                    overflow: "auto"
                });
            }
        }
    });

    return new ContentViewer();
});