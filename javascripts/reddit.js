/**
 * Created by ManasB on 7/16/2015.
 */

define(["jquery", "backbone", "moment"], function ($, Backbone, moment) {

    var unAuthApiBase = "https://www.reddit.com";

    var reddit = {
        getAuthUrl: function () {
            this.state = (Math.random() + 1).toString(36).substring(2, 5); // random string

            return "https://www.reddit.com/api/v1/authorize?" + $.param({
                    client_id: '2j6Q8QsS8lrckQ',
                    response_type: "token",
                    state: this.state,
                    redirect_uri: 'http://localhost:3000/',
                    scope: "identity,mysubreddits"
                });
        },
        getPosts: function (options) {
            var settings = $.extend({
                sort: "hot",
                error: function (err) {
                    throw err;
                }
            }, options);

            // build posts url
            var postsUrl = undefined;
            if (!settings.subreddit) {
                postsUrl = unAuthApiBase + "/" + settings.sort + ".json";
            } else {
                postsUrl = unAuthApiBase + "/r/" + settings.subreddit + "/" + settings.sort + ".json";
            }
            if (settings.after) {
                postsUrl +=  "?after=" + settings.after;
            }

            $.ajax({
                url: postsUrl,
                method: "GET",
                dataType: "json",
                timeout: 6000,
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
            if (!options.subreddit || !options.id) throw new Error("'subreddit' and 'sort' are required parameters");

            var settings = $.extend({
                sort: "best",
                error: function (err) {
                    throw err;
                }
            }, options);

            $.ajax({
                url: unAuthApiBase + "/r/" + settings.subreddit + "/comments/" + settings.id + "/.json?sort=" + settings.sort,
                method: "GET",
                dataType: "json",
                timeout: 6000,
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
        }
    };

    return reddit;
});