/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");
var LocalStorage = require("node-localstorage").LocalStorage;
var moment = require("moment");


var localStorage = new LocalStorage("./session_store");
var apiBaseUrl = "http://www.reddit.com";
var userAgent = "RedditReaderBackbone by enthusiast_94";


router.post("/login", function (req, res, next) {
    if (!req.body.username || !req.body.password) return next(new Error("Username and password are required"));

    var options = {
        url: apiBaseUrl + "/api/login",
        form: {user: req.body.username, passwd: req.body.password, api_type: "json"},
        headers: {
            "User-Agent": userAgent
        }
    };

    request.post(options, function (err, httpResponse) {
            if (err) return next(err);

            var setCookie = httpResponse.headers["set-cookie"];

            if (setCookie.length < 3) {
                res.json({status: "error"});
            } else {
                var session = setCookie[2].split(";")[0];
                console.log(session);
                options = {
                    url: apiBaseUrl + "/api/me.json",
                    headers: {
                        "User-Agent": userAgent,
                        Cookie: session
                    }
                };

                request.get(options, function (err, httpResponse, body) {
                    if (err) return next(err);

                    var json = JSON.parse(body);
                    if (json.data) {
                        localStorage.setItem(req.body.username, JSON.stringify({session: session, modhash: json.data.modhash}));
                        res.json({status: "success"});
                    } else {
                        res.json({status: "error"});
                    }
                });
            }
        }
    );
});


router.get("/user/subreddits", function (req, res, next) {
    var username = req.query.username;
    if (!username) return next(new Error("No username provided"));

    var userData = localStorage.getItem(username);
    if (!userData) return next(new Error("No session found for provided username"));

    var options = {
        url: apiBaseUrl + "/reddits/mine.json?limit=100",
        headers: {
            "User-Agent": userAgent,
            Cookie: JSON.parse(userData).session
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
        default: apiBaseUrl + "/subreddits/default.json?limit=100",
        popular10: apiBaseUrl + "/subreddits/popular.json?limit=10"
    };

    var filter = req.params["filter"];

    if (Object.keys(urls).indexOf(filter) == -1) return next(new Error("Allowed filters are: " + Object.keys(urls)));

    var options = {
        url: urls[filter],
        headers: {
            "User-Agent": userAgent
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


router.get("/posts/:subreddit?", function (req, res, next) {
    var username = req.query["username"];

    var session = undefined;
    if (username) {
        var userData = localStorage.getItem(username);
        if (!userData) return next(new Error("No session found for provided username"));
        session = JSON.parse(userData).session;
    }

    var subreddit = req.params["subreddit"];
    var sort = req.query["sort"] || "hot";
    var after = req.query["after"] || null;

    // build posts url
    var postsUrl = undefined;
    if (!subreddit) {
        postsUrl = apiBaseUrl + "/" + sort + ".json";
    } else {
        postsUrl = apiBaseUrl + "/r/" + subreddit + "/" + sort + ".json";
    }
    if (after) {
        postsUrl +=  "?after=" + after;
    }

    console.log(userData);

    var options = {
        url: postsUrl,
        headers: {
            "User-Agent": userAgent,
            Cookie: session
        }
    };

    console.log(postsUrl);

    request.get(options, function (err, httpResponse, body) {
        if (err) return next(err);

        var json = JSON.parse(body);

        if (json.error) {
            res.send(parseInt(json.error));
        } else {
            var posts = [];
            json.data.children.forEach(function (post) {
                // replace default thumbnails with urls
                if (["", "default", "self", "nsfw"].indexOf(post.data.thumbnail) > -1) {
                    post.data.thumbnail = undefined;
                }

                // humanize timestamp
                post.data.created_utc = moment.utc(moment.unix(post.data.created_utc)).locale("en").fromNow();

                posts.push(post.data);
            });

            res.json({posts: posts, after: json.data.after});
        }
    });
});


//router.post("/vote/", function (req, res, next) {
//
//});


module.exports = router;