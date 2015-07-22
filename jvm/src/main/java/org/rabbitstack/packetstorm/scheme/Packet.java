package org.rabbitstack.packetstorm.scheme;

import java.io.Serializable;

/**
 * POJO that represents the packet payload.
 *
 * @author Nedim Sabic
 */
public class Packet implements Serializable {

    private String dstIp;
    private String srcIp;
    private Integer srcPort;
    private Integer dstPort;
    private String protocol;

    public void setDstPort(Integer dstPort) {
        this.dstPort = dstPort;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(Integer srcPort) {
        this.srcPort = srcPort;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    public void setDstPost(Integer dstPost) {
        this.dstPort = dstPost;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getSrcIp() {
        return srcIp;
    }

    public void setSrcIp(String srcIp) {
        this.srcIp = srcIp;
    }

    public String getDstIp() {
        return dstIp;
    }

    public void setDstIp(String dstIp) {
        this.dstIp = dstIp;
    }



}
