/**
 * Created by ManasB on 7/1/2015.
 */

var express = require('express');
var router = express.Router();
var request = require("request");
var moment = require("moment");

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
                res.json({success: false});
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
                        res.json({success: true, session: session, modhash: json.data.modhash});
                    } else {
                        res.json({success: false});
                    }
                });
            }
        }
    );
});


router.get("/subreddits/:filter", function (req, res, next) {
    var urls = {
        defaults: apiBaseUrl + "/subreddits/default.json?limit=100",
        user: apiBaseUrl + "/reddits/mine.json?limit=100"
    };

    var session = req.query.session;
    var filter = req.params["filter"];

    if (Object.keys(urls).indexOf(filter) == -1) return next(new Error("Allowed filters are: " + Object.keys(urls)));

    var options = {
        url: urls[filter],
        headers: {
            "User-Agent": userAgent,
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


router.get("/posts/:subreddit?", function (req, res, next) {
    var session = req.query.session;
    var subreddit = req.params.subreddit;
    var sort = req.query.sort || "hot";
    var after = req.query.after || null;

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
    var session = req.body.session;
    var modhash = req.body.modhash;

    if (!session || !modhash) return next(new Error("Session and modhash are required parameters for voting"));

    var options = {
        url: apiBaseUrl + "/api/vote",
        form: {id: postId, dir: dir, uh: modhash, api_type: "json"},
        headers: {
            "User-Agent": userAgent,
            Cookie: session
        }
    };

    request.post(options, function (err, httpResponse) {
        if (err) return next(err);

        res.json({success: httpResponse.statusCode == 200});
    });

});


router.get("/comments", function (req, res, next) {
    var subreddit = req.query.subreddit;
    var postId = req.query.id;
    var sort = req.query.sort || "best";
    var session = req.query.session;

    var options = {
        url: apiBaseUrl + "/r/" + subreddit + "/comments/" + postId + "/.json?sort=" + sort,
        headers: {
            "User-Agent": userAgent,
            Cookie: session
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