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

            this.$modal = $("#login-modal");
            this.$usernameInput = $("#login-modal-username-input");
            this.$passwordInput = $("#login-modal-password-input");
            this.$errorContainer = $("#login-modal-error-container");
            this.$submitButton = $("#login-modal-submit-button");
        },
        events: {
            "click #login-modal-submit-button": "submit"
        },
        submit: function () {
            this.$errorContainer.hide();

            var username = this.$usernameInput.val();
            var password = this.$passwordInput.val();

            if (username.trim().length == 0 || password.trim().length == 0) {
                this.$errorContainer.html("<strong>Error!</strong> Both fields are required.");
                this.$errorContainer.show();
            } else {
                this.$submitButton.prop("disabled", true);
                this.$submitButton.html("Loading...");

                var self = this;
                $.ajax({
                    url: "/api/login",
                    method: "POST",
                    data: {username: username, password: password},
                    success: function (response) {
                        if (response.length < 3) {
                            self.$errorContainer.show();
                            self.$errorContainer.html("<strong>Error! </strong>Username or password is incorrect.");
                        } else {
                            // store session token and username in local storage
                            localStorage.setItem("username", username);
                            localStorage.setItem("session", response[2].split(";")[0]);

                            self.trigger("login.success");

                            self.$modal.modal("hide");
                        }

                        self.$submitButton.prop("disabled", false);
                        self.$submitButton.html("Submit");
                    },
                    error: function (error) {
                        self.$errorContainer.show();
                        self.$errorContainer.html(error.statusText);

                        self.$submitButton.prop("disabled", false);
                        self.$submitButton.html("Submit");
                    }
                });
            }
        }
    });

    return new LoginModalView();
});