var pg = require('pg');
var async = require('async');

function PacketApi(url) {
    this.url = url;
    this.psql = new pg.Client(url);
    this.psql.connect(function(err) {
        if(err) {
            return console.error('Could not connect to postgres', err);
        }
    });
}

/**
 * Query the global packet statistics.
 *
 * @param cb
 */
PacketApi.prototype.globalStats = function(cb) {

    var all = 'SELECT SUM(proto_count) AS "all" FROM proto_counts';
    var tcp = 'SELECT SUM(proto_count) AS "tcp" FROM proto_counts WHERE proto LIKE \'% (TCP)\'';
    var udp = 'SELECT SUM(proto_count) AS "udp" FROM proto_counts WHERE proto LIKE \'% (UDP)\'';

    var pgsql = this.psql;
    var stats = [];

    async.each([all, tcp, udp],
        function(sql, callback) {
            pgsql.query(sql, function(err, rs) {
                if (err) {
                    console.error("Cannot execute packet global stat queries", err);
                }
                stats.push(rs.rows[0]);
                callback();
            })
        },
        function(err) {
            cb(stats);
        }
    );
}

/**
 * Gets the top destination ip addresses by the number of packets.
 *
 * @param cb
 */
PacketApi.prototype.topIps = function(cb) {
    var sql = 'SELECT ip, ip_count as "ipCount" FROM top_dst_ips ORDER BY ip_count DESC LIMIT 10';
    _query(sql, this.psql, cb);
}

/**
 * Gets the top destination ports by the number of packets.
 *
 * @param cb
 */
PacketApi.prototype.topPorts = function(cb) {
    var sql = 'SELECT port, port_count as "portCount" FROM top_dst_ports ORDER BY port_count DESC LIMIT 10';
    _query(sql, this.psql, cb);
}

/**
 * Gets the top protocols by the number of packets.
 *
 * @param cb
 */
PacketApi.prototype.topProtos = function(cb) {
    var sql = 'SELECT proto, proto_count as "protoCount" FROM proto_counts ORDER BY proto_count DESC LIMIT 10';
    _query(sql, this.psql, cb);
}


function _query(sql, psql, cb) {
    psql.query(sql, function(err, rs) {
        if (!err) {
            cb(rs.rows);
        }
    });
}

module.exports = PacketApi;
