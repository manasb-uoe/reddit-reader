/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");
var LocalStorage = require("node-localstorage").LocalStorage;
var moment = require("moment");


var localStorage = new LocalStorage("./session_store");
var apiBaseUrl = "https://www.reddit.com";
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
    if (!username) return next(new Error("Username is required"));

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

    var options = {
        url: postsUrl,
        headers: {
            "User-Agent": userAgent,
            Cookie: session
        }
    };

    request.get(options, function (err, httpResponse, body) {
        if (err) return next(err);

        var json = JSON.parse(body);

        if (json.error) {
            res.sendStatus(parseInt(json.error));
        } else {
            var posts = [];
            json.data.children.forEach(function (post) {
                // replace default thumbnails with urls
                if (["", "default", "self", "nsfw"].indexOf(post.data.thumbnail) > -1) {
                    post.data.thumbnail = undefined;
                }

                // humanize timestamp
                post.data.created_utc = moment.utc(moment.unix(post.data.created_utc)).locale("en").fromNow();

                // replace 'likes' with 1,0 or -1 so that it's easy to use its value while rendering templates
                if (post.data.likes) {
                    post.data.likes = 1;
                } else if (post.data.likes == null) {
                    post.data.likes = 0;
                } else {
                    post.data.likes = -1;
                }

                posts.push(post.data);
            });

            res.json({posts: posts, after: json.data.after});
        }
    });
});


router.post("/vote", function (req, res, next) {
    var postId = req.body.id;
    var dir = req.body.dir;
    var username = req.body.username;

    if (!postId || !dir) return next(new Error("Post id and vote direction are required parameters"));
    if (!username) return next(new Error("Username is required"));

    var userData = localStorage.getItem(username);
    if (!userData) return next(new Error("No session found for provided username"));

    userData = JSON.parse(userData);

    var options = {
        url: apiBaseUrl + "/api/vote",
        form: {id: postId, dir: dir, uh: userData.modhash, api_type: "json"},
        headers: {
            "User-Agent": userAgent,
            Cookie: userData.session
        }
    };

    request.post(options, function (err, httpResponse) {
        if (err) return next(err);

        if (httpResponse.statusCode != 200) {
            return next(new Error("Vote failed with status code: " + httpResponse.statusCode));
        }
    });

});


router.get("/comments", function (req, res, next) {
    var subreddit = req.query["subreddit"];
    var postId = req.query["id"];
    var sort = req.query["sort"] || "best";
    var username = req.query["username"];

    if (!subreddit || !postId) return next(new Error("'subreddit' and 'id' are required query parameters"));

    var userData = undefined;
    if (username) {
        userData = localStorage.getItem(username);
        if (!userData) return next(new Error("No session found for provided username"));

        userData = JSON.parse(userData);
    }

    var options = {
        url: apiBaseUrl + "/r/" + subreddit + "/comments/" + postId + "/.json?sort=" + sort,
        headers: {
            "User-Agent": userAgent,
            Cookie: userData ? userData.session : undefined
        }
    };

    request.get(options, function (err, httpResponse, body) {
        if (err) return next(err);

        var json = JSON.parse(body);

        /**
         * Parse post
         */

        var post = json[0].data.children[0].data;

        // replace default thumbnails with urls
        if (["", "default", "self", "nsfw"].indexOf(post.thumbnail) > -1) {
            post.thumbnail = undefined;
        }

        // humanize timestamp
        post.created_utc = moment.unix(post.created_utc).locale("en").fromNow();

        // replace 'likes' with 1,0 or -1 so that it's easy to use its value while rendering templates
        if (post.likes) {
            post.likes = 1;
        } else if (post.likes == null) {
            post.likes = 0;
        } else {
            post.likes = -1;
        }


        /**
         * Parse comments
         */

        var comments = [];

        var parseComments = function (thread, level) {
            if (thread.kind == "t1") {
                var comment = {body: thread.data.body_html,
                    score: thread.data.score,
                    likes: thread.data.likes,
                    author: thread.data.author,
                    name: thread.data.name,
                    created_utc: thread.data.created_utc,
                    level: level
                };

                // humanize timestamp
                comment.created_utc = moment.utc(moment.unix(comment.created_utc)).locale("en").fromNow();

                // replace 'likes' with 1,0 or -1 so that it's easy to use its value while rendering templates
                if (comment.likes) {
                    post.likes = 1;
                } else if (comment.likes == null) {
                    comment.likes = 0;
                } else {
                    comment.likes = -1;
                }

                comments.push(comment);

                if (thread.data.replies) {
                    level++;
                    thread.data.replies.data.children.forEach(function (thread) {
                        parseComments(thread, level);
                    });
                }
            }
        };

        json[1].data.children.forEach(function (thread) {
            parseComments(thread, 0);
        });

        res.json({post: post, comments: comments});

    });

});


module.exports = router;