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
                    timeout: 6000,
                    success: function (response) {
                        if (response.success) {
                            localStorage.setItem("username", username);
                            localStorage.setItem("session", response.session);
                            localStorage.setItem("modhash", response.modhash);

                            self.trigger("login.success");

                            self.$usernameInput.val("");
                            self.$passwordInput.val("");
                            self.$modal.modal("hide");

                            // reload current page
                            Backbone.history.loadUrl();
                        } else {
                            self.$errorContainer.show();
                            self.$errorContainer.html("<strong>Error! </strong>Username or password is incorrect.");
                        }

                        self.$submitButton.prop("disabled", false);
                        self.$submitButton.html("Submit");
                    },
                    error: function (jqXHR, textStatus) {
                        self.$errorContainer.show();
                        self.$errorContainer.html(textStatus);

                        self.$submitButton.prop("disabled", false);
                        self.$submitButton.html("Submit");
                    }
                });
            }
        }
    });

    return new LoginModalView();
});