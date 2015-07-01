/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");

var apiBaseUrl = "http://www.reddit.com";

router.post("/login", function (req, res, next) {
    console.log({user: req.body.username, psswd: req.body.password, api_type: "json"});

    request.post(
        apiBaseUrl + "/api/login",
        {form: {user: req.body.username, passwd: req.body.password, api_type: "json"}},
        function (err, httpResponse) {
            if (err) return next(err);

            res.json(httpResponse.headers["set-cookie"]);
        }
    );
});


module.exports = router;