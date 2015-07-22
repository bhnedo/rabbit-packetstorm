var express = require("express");
var app = express();
var config = require("config");

var dbConfig = config.get('db');
var kafkaConfig = config.get('kafka');

var server = app.listen(8080);

var io = require("socket.io")(server);

const API_ENDPOINT = "/api/v1";

app.use("/scripts", express.static(__dirname + "/../app/scripts"));
app.use("/bower_components", express.static(__dirname + "/../bower_components"));
app.use("/images", express.static(__dirname + "/images"));
app.use("/styles", express.static(__dirname + "/../app/styles"));

var PacketApi = require('./api');
var PacketIoStreamer = require('./stream');

var api = new PacketApi(dbConfig.url);
var streamer = new PacketIoStreamer(io, api, kafkaConfig);
streamer.streamPeriodically(1);
streamer.streamFromKafka();


app.get(API_ENDPOINT + "/global", function (req, res) {
    api.globalStats(function(stats) {
        res.send(stats);
    });
});

app.get(API_ENDPOINT + "/topips", function (req, res) {
    api.topIps(function(stats) {
        res.send(stats);
    });
});

app.get(API_ENDPOINT + "/toports", function (req, res) {
    api.topPorts(function(stats) {
        res.send(stats);
    });
});

app.get(API_ENDPOINT + "/toprotos", function (req, res) {
    api.topProtos(function(stats) {
        res.send(stats);
    });
});

app.all("/*", function(req, res, next) {
    res.sendfile("index.html", { root: __dirname + "/../app" });
});
