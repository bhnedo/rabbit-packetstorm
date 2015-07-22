var kafka = require("kafka-node");

var PacketIoStreamer = function(io, api, kafkaConfig) {
    this.io = io;
    this.api = api;
    this.topic = kafkaConfig.topic;
    this.zookeeperHost = kafkaConfig.zookeeper;

};

/**
 * Streams the global packet stats from Kafka topic to browser.
 *
 */
PacketIoStreamer.prototype.streamFromKafka = function() {
    var io = this.io;
    var api = this.api;

    var kafkaConsumer = new kafka.Consumer(
        new kafka.Client(this.zookeeperHost),
        [
            {topic: this.topic}
        ]
    );

    api.globalStats(function(stats) {
        var totalPackets = stats[0].all;
        var tcpPackets = stats[1].tcp;
        var udpPackets = stats[2].udp;
        kafkaConsumer.on('message', function(message) {
            totalPackets++;
            var proto = message.value;
            if (proto.indexOf('(TCP)') > -1) {
                tcpPackets++;
            } else if (proto.indexOf('(UDP)') > -1) {
                udpPackets++;
            }
            io.emit('packets', {totalPackets: totalPackets,
                                 udpPackets: udpPackets,
                                 tcpPackets: tcpPackets
            })
        });
    });
};

/**
 * Executes the queries periodically and pushes the results
 * to the client via socket.io
 *
 * @param interval
 */
PacketIoStreamer.prototype.streamPeriodically = function(interval) {
    var api = this.api;
    var io = this.io;
    setInterval(function() {
        api.topIps(function(stats) {
            io.emit('ips', stats);
        });
        api.topPorts(function(stats) {
            io.emit('ports', stats);
        });
        api.topProtos(function(stats) {
            io.emit('protos', stats);
        });

    }, interval * 1000);
};

module.exports = PacketIoStreamer;
