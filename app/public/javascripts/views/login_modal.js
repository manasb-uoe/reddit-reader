/**
 * Created by ManasB on 6/30/2015.
 */

define([
    "underscore",
    "jquery",
    "backbone",
    "text!../../templates/login_modal.html"
], function (_, $, Backbone, loginModalTemplte) {
    "use strict";

    var LoginModalView = Backbone.View.extend({
        el: "#login-modal-container",
        render: function () {
            this.$el.html(loginModalTemplte);

            this.$usernameInput = $("#login-modal-username-input");
            this.$passwordInput = $("#login-modal-password-input");
            this.$errorContainer = $("#login-modal-error-container");
        },
        events: {
            "click #login-modal-submit-button": "submit"
        },
        submit: function () {
            var self = this;
            $.ajax({
                url: "http://www.reddit.com/api/login",
                method: "POST",
                //dataType: "jsonp",
                crossDomain: true,
                //data: {user: self.$usernameInput.val(), passwd: self.$passwordInput.val(), api_type: 'json'},
                data: "user=enthusiast_94&passwrd=techie94&api_type=json",
                success: function (response) {
                    console.log(response);
                },
                error: function (error) {
                    console.log(error);
                }
            });
        }
    });

    return new LoginModalView();
});