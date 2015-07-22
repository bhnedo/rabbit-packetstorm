package org.rabbitstack.packetstorm;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Fields;
import org.rabbitstack.packetstorm.config.TopologyConfig;
import org.rabbitstack.packetstorm.config.YamlConfigReader;
import org.rabbitstack.packetstorm.filter.IsInternalIp;
import org.rabbitstack.packetstorm.scheme.PacketScheme;
import org.rabbitstack.packetstorm.state.PostgresqlStateConfigFactory;
import org.rabbitstack.packetstorm.state.sql.PostgresqlState;
import storm.kafka.BrokerHosts;
import storm.kafka.ZkHosts;
import storm.kafka.trident.*;
import storm.kafka.trident.mapper.FieldNameBasedTupleToKafkaMapper;
import storm.kafka.trident.selector.DefaultTopicSelector;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.FilterNull;

import java.io.IOException;
import java.util.Properties;

/** Trident based topology which consumes the input streams
 *  structured as network packets, and then applies Trident
 *  primitives to provide a variety of stats about incoming
 *  network packets, such as the top destination packets by ip
 *  address, port, top protocols, etc.
 *
 * @author Nedim Sabic
 */
public class PacketStormTopology {

    private static final String KAFKA_TOPIC_NAME = "packetstorm";

    public static void main(String args[]) throws IOException, AlreadyAliveException, InvalidTopologyException {

        TridentTopology topology = new TridentTopology();
        YamlConfigReader ymlConfigReader = new YamlConfigReader();
        TopologyConfig config = ymlConfigReader.getConfig();
        

        // initialize the spout to consume
        // messages from the Kafka topics
        BrokerHosts hosts = new ZkHosts(config.getZookeeper().getHosts());
        TridentKafkaConfig kafkaConfig = new TridentKafkaConfig(hosts, KAFKA_TOPIC_NAME);
        kafkaConfig.scheme = new SchemeAsMultiScheme(new PacketScheme());
        OpaqueTridentKafkaSpout kafkaSpout = new OpaqueTridentKafkaSpout(kafkaConfig);
        PostgresqlStateConfigFactory.dbUrl = config.getDb().getUrl();

        TridentKafkaStateFactory stateFactory = new TridentKafkaStateFactory()
                .withKafkaTopicSelector(new DefaultTopicSelector("packets"))
                .withTridentTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper(PacketScheme.DST_IP, PacketScheme.PROTOCOL));


        Stream stream = topology.newStream("kafkaSpout", kafkaSpout)
                                .global();
        stream.partitionPersist(stateFactory, new Fields(PacketScheme.DST_IP, PacketScheme.PROTOCOL), new TridentKafkaUpdater(), new Fields());

        stream.each(new Fields(PacketScheme.PROTOCOL), new FilterNull())
              .shuffle()
              .groupBy(new Fields(PacketScheme.PROTOCOL))
              .persistentAggregate(PostgresqlState.newFactory(PostgresqlStateConfigFactory.newPostgresqlStateConfig("proto_counts", new String[]{"proto"}, new String[]{"proto_count"})),
                      new Fields(PacketScheme.PROTOCOL),
                      new Count(),
                      new Fields("protocolCount"))
              .parallelismHint(6);


        stream.each(new Fields(PacketScheme.DST_IP), new FilterNull())
              .each(new Fields(PacketScheme.DST_IP), new IsInternalIp())
              .shuffle()
              .groupBy(new Fields(PacketScheme.DST_IP))
              .persistentAggregate(PostgresqlState.newFactory(PostgresqlStateConfigFactory.newPostgresqlStateConfig("top_dst_ips", new String[]{"ip"}, new String[]{"ip_count"})),
                      new Fields(PacketScheme.DST_IP),
                      new Count(),
                      new Fields("topCount"))
              .parallelismHint(6);

        stream.each(new Fields(PacketScheme.DST_PORT), new FilterNull())
              .shuffle()
              .groupBy(new Fields(PacketScheme.DST_PORT))
              .persistentAggregate(PostgresqlState.newFactory(PostgresqlStateConfigFactory.newPostgresqlStateConfig("top_dst_ports", new String[]{"port"}, new String[]{"port_count"})),
                      new Fields(PacketScheme.DST_PORT),
                      new Count(),
                      new Fields("topCount"))
              .parallelismHint(6);



        Config conf = new Config();
        conf.setNumWorkers(3);

        Properties props = new Properties();
        props.put("metadata.broker.list", "localhost:9092");
        props.put("request.required.acks", "1");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        conf.put(TridentKafkaState.KAFKA_BROKER_PROPERTIES, props);

        // submit the topology to the Storm cluster
        StormSubmitter.submitTopology("packetstorm", conf, topology.build());
    }
}
