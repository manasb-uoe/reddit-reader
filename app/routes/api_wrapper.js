/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");

var apiBaseUrl = "http://www.reddit.com";

router.post("/login", function (req, res, next) {
    request.post(
        apiBaseUrl + "/api/login",
        {form: {user: req.body.username, passwd: req.body.password, api_type: "json"}},
        function (err, httpResponse) {
            if (err) return next(err);

            res.json(httpResponse.headers["set-cookie"]);
        }
    );
});

router.post("/user/subreddits", function (req, res, next) {
    var options = {
        url: apiBaseUrl + "/reddits/mine.json?limit=100",
        headers: {
            Cookie: req.body.session
        }
    };
    request.get(options, function (err, httpResponse, body) {
        if (err) return next(err);

        res.json(body);
    });
});


module.exports = router;