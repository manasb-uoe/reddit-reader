/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");
var LocalStorage = require("node-localstorage").LocalStorage;


var localStorage = new LocalStorage("./session_store");
var apiBaseUrl = "http://www.reddit.com";


router.post("/login", function (req, res, next) {
    if (!req.body.username || !req.body.password) return next(new Error("Username and password are required"));

    request.post(
        apiBaseUrl + "/api/login",
        {form: {user: req.body.username, passwd: req.body.password, api_type: "json"}},
        function (err, httpResponse) {
            if (err) return next(err);

            var setCookie = httpResponse.headers["set-cookie"];

            if (setCookie.length < 3) {
                res.json({status: "failure"});
            } else {
                localStorage.setItem(req.body.username, setCookie[2].split(";")[0]);
                res.json({status: "success"});
            }
        }
    );
});


router.get("/user/subreddits", function (req, res, next) {
    var username = req.query.username;
    if (!username) return next(new Error("No username provided"));

    var session = localStorage.getItem(username);
    if (!session) return next(new Error("No session found for provided username"));

    var options = {
        url: apiBaseUrl + "/reddits/mine.json?limit=100",
        headers: {
            Cookie: session
        }
    };

    request.get(options, function (err, httpResponse, body) {
        if (err) return next(err);

        var json = JSON.parse(body);
        var subreddits = [];
        json.data.children.forEach(function (subreddit) {
            subreddits.push(subreddit.data);
        });

        res.json(subreddits);
    });
});


router.get("/subreddits/:filter", function (req, res, next) {
    var urls = {
        default: "http://www.reddit.com/subreddits/default.json?limit=100",
        popular10: "http://www.reddit.com/subreddits/popular.json?limit=10"
    };

    var filter = req.params["filter"];

    if (Object.keys(urls).indexOf(filter) == -1) return next(new Error("Allowed filters are: " + Object.keys(urls)));

    request.get(urls[filter], function (err, httpResponse, body) {
        if (err) return next(err);

        var json = JSON.parse(body);
        var subreddits = [];
        json.data.children.forEach(function (subreddit) {
            subreddits.push(subreddit.data);
        });

        res.json(subreddits);
    });
});


module.exports = router;