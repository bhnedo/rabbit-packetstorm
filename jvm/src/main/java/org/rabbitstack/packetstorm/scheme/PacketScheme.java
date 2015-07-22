package org.rabbitstack.packetstorm.scheme;

import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;


/** Defines the scheme for transforming the incoming
 * JSON packet data into <code>Packet</code> object.
 *
 * @author Nedim Sabic
 */
public class PacketScheme implements Scheme {

    private ObjectMapper mapper = new ObjectMapper();

    public static final String PROTOCOL = "protocol";
    public static final String DST_IP = "dstIp";
    public static final String SRC_IP = "srcIp";
    public static final String DST_PORT = "dstPort";
    public static final String SRC_PORT = "srcPort";

    @Override
    public List<Object> deserialize(byte[] ser)  {
        try {
            Packet packet = mapper.readValue(ser, Packet.class);
            return new Values(packet.getProtocol(),
                              packet.getDstIp(),
                              packet.getSrcIp(),
                              packet.getDstPort(),
                              packet.getSrcPort());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new Values();
    }

    @Override
    public Fields getOutputFields() {
        return new Fields(PROTOCOL, DST_IP, SRC_IP, DST_PORT, SRC_PORT);
    }
}
