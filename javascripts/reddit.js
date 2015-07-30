/**
 * Created by ManasB on 7/16/2015.
 */

define(["jquery", "backbone", "moment"], function ($, Backbone, moment) {

    var state = "reddit-reader-backbone-state";
    var unAuthApiBase = "https://www.reddit.com";
    var authApiBase = "https://oauth.reddit.com";
    var authUrl = unAuthApiBase + "/api/v1/authorize?" + $.param({
            client_id: '2j6Q8QsS8lrckQ',
            response_type: "token",
            state: state,
            redirect_uri: 'http://localhost:3000',
            scope: "identity,mysubreddits,read,vote"
        });


    var reddit = {
        getAuthUrl: function () {
            return authUrl;
        },
        getUser: function () {
            var user = localStorage.getItem("user");
            if (user) {
                return JSON.parse(user);
            } else {
                return undefined;
            }
        },
        auth: function (options) {
            if (!options.accessToken || !options.state) throw new Error("'accessToken' and 'state' are required parameters");

            if (options.state != state) throw new Error("Response state does not match request state");

            $.ajax({
                url: authApiBase + "/api/v1/me.json",
                method: "GET",
                dataType: "json",
                timeout: 6000,
                beforeSend: function (jqXHR) {
                    jqXHR.setRequestHeader("Authorization", "bearer " + options.accessToken);
                },
                success: function (response) {
                    localStorage.setItem("user", JSON.stringify({username: response.name, accessToken: options.accessToken, timestamp: Date.now()}));

                    if (options.success) options.success();
                },
                error: options.error
            });
        },
        deauth: function () {
            localStorage.clear();
        },
        checkAccessToken: function (user) {
            var hasExpired = (Date.now() - user.timestamp) >= 3600000;
            if (hasExpired) {
                this.deauth();
                this.trigger("access_token_expired");
            }

            return !hasExpired;
        },
        getPosts: function (options) {
            var settings = $.extend({
                sort: "hot",
                error: function (err) {
                    throw err;
                }
            }, options);

            var user = this.getUser();

            if (user) {
                if (!this.checkAccessToken(user)) return;
            }

            // build posts url
            var postsUrl = user ? authApiBase : unAuthApiBase;
            if (!settings.subreddit) {
                postsUrl += "/" + settings.sort + ".json";
            } else {
                postsUrl += "/r/" + settings.subreddit + "/" + settings.sort + ".json";
            }
            if (settings.after) {
                postsUrl +=  "?after=" + settings.after;
            }

            $.ajax({
                url: postsUrl,
                method: "GET",
                dataType: "json",
                timeout: 6000,
                beforeSend: !user ? undefined : function (jqXHR) {
                    jqXHR.setRequestHeader("Authorization", "bearer " + user.accessToken);
                },
                success: function (json) {
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

                    if (settings.success) {
                        settings.success({posts: posts, after: json.data.after});
                    }
                },
                error: settings.error
            });
        },
        getComments: function (options) {
            if (!options.subreddit || !options.id) throw new Error("'subreddit' and 'id' are required parameters");

            var settings = $.extend({
                sort: "best",
                error: function (err) {
                    throw err;
                }
            }, options);

            var user = this.getUser();

            if (user) {
                if (!this.checkAccessToken(user)) return;
            }

            // build comments url
            var commentsUrl = user ? authApiBase : unAuthApiBase;
            commentsUrl += "/r/" + settings.subreddit + "/comments/" + settings.id + "/.json?sort=" + settings.sort;

            $.ajax({
                url: commentsUrl,
                method: "GET",
                dataType: "json",
                timeout: 6000,
                beforeSend: !user ? undefined : function (jqXHR) {
                    jqXHR.setRequestHeader("Authorization", "bearer " + user.accessToken);
                },
                success: function (json) {
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


                    if (settings.success) {
                        settings.success({post: post, comments: comments});
                    }
                },
                error: settings.error
            });
        },
        getSubreddits: function (options) {
            var settings = $.extend({
                urls: {
                    defaults: "/subreddits/default.json?limit=100",
                    user: "/reddits/mine.json?limit=100",
                    popular: "/subreddits/popular.json?limit=10"
                },
                error: function (err) {
                    throw err;
                }
            }, options);

            if (Object.keys(settings.urls).indexOf(settings.type) == -1) throw new Error("Allowed types are: " + Object.keys(settings.urls));

            var user = this.getUser();

            if (user) {
                if (!this.checkAccessToken(user)) return;
            }

            $.ajax({
                url: user ? authApiBase + settings.urls[settings.type] : unAuthApiBase + settings.urls[settings.type],
                method: "GET",
                dataType: "json",
                timeout: 6000,
                beforeSend: !user ? undefined : function (jqXHR) {
                    jqXHR.setRequestHeader("Authorization", "bearer " + user.accessToken);
                },
                success: function (json) {
                    var subreddits = [];
                    json.data.children.forEach(function (subreddit) {
                        subreddits.push(subreddit.data);
                    });

                    if (settings.success) settings.success(subreddits);
                },
                error: settings.error
            });
        },
        vote: function (options) {
            var settings = $.extend({
                error: function (err) {
                    throw err;
                }
            }, options);

            var user = this.getUser();
            if (!user) throw new Error("Current user is not authenticated");

            if (!this.checkAccessToken(user)) return;

            if (!settings.itemId || !settings.voteDir) throw new Error("'itemId' and 'voteDir' are required parameters");

            $.ajax({
                url: authApiBase + "/api/vote",
                method: "POST",
                data: {id: settings.itemId, dir: settings.voteDir},
                timeout: 6000,
                beforeSend: function (jqXHR) {
                    jqXHR.setRequestHeader("Authorization", "bearer " + user.accessToken);
                },
                success: settings.success,
                error: settings.error
            });
        }
    };

    return reddit;
});