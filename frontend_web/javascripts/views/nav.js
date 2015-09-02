/**
 * Created by ManasB on 7/30/2015.
 */

var $ = require("jquery");
var _ = require("underscore");
var reddit = require("../reddit");
var Backbone = require("backbone");
var swig = require("swig");

var navTemplate = $("#nav-template").html();


var NavView = Backbone.View.extend({
    el: "#nav",
    initialize: function () {
        this.on("logout", this.logout, this);

        var self = this;
        $(document).on("access_token_expired", function () {
            self.render();
            Backbone.history.loadUrl();

            alert("Session has expired, please login again.");
        });
    },
    render: function () {
        var user = reddit.getUser();
        var compiledTemplate = swig.render(navTemplate, {
            locals: {
                username: user ? user.username : undefined,
                authUrl: !user ? reddit.getAuthUrl() : undefined
            }
        });
        this.$el.html(compiledTemplate);

        this.$currentSubreddit = $("#current-subreddit");
        this.$subredditInput = $("#subreddit-input");
    },
    events: {
        "submit form": "jumpToSubreddit",
        "click #logout-button": function () {
            this.trigger("logout");
        }
    },
    updateCurrentSubreddit: function (subreddit) {
        var text = subreddit != "Front page" ? "Subreddit: " + "r/" + subreddit : subreddit;
        this.$currentSubreddit.text(text);
    },
    jumpToSubreddit: function (event) {
        event.preventDefault();

        window.location.hash = "/r/" + this.$subredditInput.val();
    },
    logout: function () {
        reddit.deauth();

        this.render();

        Backbone.history.loadUrl();
    }
});


module.exports = new NavView();