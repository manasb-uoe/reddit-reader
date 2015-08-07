
/**
 * App entry point
 */

window.$ = window.jQuery = require("jquery");    // needed by bootstrap js
var App = require("./router");

App.init();