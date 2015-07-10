/**
 * Created by ManasB on 7/9/2015.
 */


define([
    "underscore",
    "jquery",
    "backbone",
    "swig"
], function (_, $, Backbone, swig) {
    "use strict";

    var ThreadNavigatorView = Backbone.View.extend({
        el: "#thread-navigator-container",
        initialize: function (options) {
            this.threads = this.getThreads(options.comments);
            this.currentThread = -1;
        },
        template: [
            "<button class='btn btn-lg btn-warning' id='navigate-down-button'><span class='glyphicon glyphicon-chevron-down'></span></button>",
            "<button class='btn btn-warning' id='thread-indicator'>{{current}} / {{total}}</button>",
            "<button class='btn btn-lg btn-warning' id='navigate-up-button'><span class='glyphicon glyphicon-chevron-up'></span></button>"
        ].join(""),
        render: function () {
            var compiledTemplate = swig.render(this.template, {locals: {current: this.currentThread+1, total: this.threads.length}});
            this.$el.html(compiledTemplate);

            this.$upButton = $("#navigate-up-button");
            this.$downButton = $("#navigate-down-button");

            this.updateControlsVisiblity();
        },
        events: {
            "click #navigate-up-button": function () {this.navigate("up")},
            "click #navigate-down-button": function () {this.navigate("down")}
        },
        setVisible: function (shouldVisible) {
            if (shouldVisible) {
                this.$el.animate({
                    marginBottom: "0"
                }, 500);
            } else {
                this.$el.animate({
                    marginBottom: -this.$el.height() - 100
                }, 500);
            }
        },
        navigate: function (direction) {
            if (direction == "up") {
                this.currentThread--;
            } else {
                this.currentThread++;
            }

            var $targetDiv = $("#comments-container").find("[data-name='" + this.threads[this.currentThread].get("name") + "']");
            $("html,body").animate({scrollTop: $targetDiv.offset().top - 15}, 200);

            this.render();
            this.updateControlsVisiblity();
        },
        getThreads: function (comments) {
            return comments.filter(function (comment) {
                return comment.get("level") == 0;
            });
        },
        updateControlsVisiblity: function () {
            if (this.currentThread == 0 || this.currentThread == -1) {
                this.$upButton.attr("disabled", "disabled");
                this.$downButton.removeAttr("disabled");

                // checks if we have reached the bottom of the document
                // - window.scrollTop gives number of pixles scrolled from the top of the document
                // - window.height gives the height of visible window
                // - document.height gives the height of the entire document
            } else if ($(window).scrollTop() + $(window).height() == $(document).height()) {
                this.$upButton.removeAttr("disabled");
                this.$downButton.attr("disabled", "disabled");

            } else if (this.currentThread == this.threads.length-1) {
                this.$upButton.removeAttr("disabled");
                this.$downButton.attr("disabled", "disabled");

            } else {
                this.$downButton.removeAttr("disabled");
                this.$upButton.removeAttr("disabled");
            }
        }
    });

    return ThreadNavigatorView;
});