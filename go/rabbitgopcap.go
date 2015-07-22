/*
 Packet sniffer based on the excellent gopacket package.

 It will try to start the packet capture on all available
 interfaces.

 The decoded packet data is aggregated to Kafka topics for later consumption.

 Author: Nedim Sabic

*/
package main


import (
	"github.com/google/gopacket/pcap"
	"log"
	"github.com/google/gopacket"
	"github.com/google/gopacket/layers"
	"encoding/json"
	"github.com/Shopify/sarama"
	"fmt"
	"net"
	"flag"
	"strings"
)


// Packt is a decoded packet data with which we will feed the Kafka topics
type Packt struct {
	SrcPort uint16		`json:"srcPort"`
	DstPort uint16		`json:"dstPort"`
	DstIP string		`json:"dstIp"`
	SrcIP string		`json:"srcIp"`
	Protocol string		`json:"protocol"`
}


var spanlen int32 = 1600
var kafkaBrokers string

func emitPacket(pckt Packt, producer sarama.AsyncProducer) {
	// Serialize the packet struct to JSON and send
	// to kafka topic
	var json, err = json.Marshal(pckt)
	if err == nil {
		producer.Input() <- &sarama.ProducerMessage {
			Topic: "packetstorm",
			Value: sarama.ByteEncoder(json),
		}
	} else {
		log.Fatal("Couldn't marshall the packet data")
	}
}

func decodePackets(iface string, producer sarama.AsyncProducer) {

	// Try to open live packet capture
	h, err := pcap.OpenLive(iface,
							spanlen,
							true, pcap.BlockForever)
	if err != nil {
		log.Printf("Couldn't start live packet capture on %s interface", iface)
	}

	ps := gopacket.NewPacketSource(h, h.LinkType())
	packets := ps.Packets()

	// Read packets from the channel
	for {
		select {

			case packet := <-packets:
				if packet.TransportLayer() != nil && packet.NetworkLayer() != nil {
					pckt := Packt{}
					// Figure out the application layer protocol
					// from the destination port
					var dstPort, srcPort uint16
					if packet.TransportLayer().LayerType() == layers.LayerTypeTCP {
						tcp := packet.TransportLayer().(*layers.TCP)
						dstPort = uint16(tcp.DstPort)
						srcPort = uint16(tcp.SrcPort)
						pckt.Protocol = ianaPort(packet.TransportLayer().LayerType(), dstPort)
					} else if packet.TransportLayer().LayerType() == layers.LayerTypeUDP {
						udp := packet.TransportLayer().(*layers.UDP)
						dstPort = uint16(udp.DstPort)
						srcPort = uint16(udp.SrcPort)
						pckt.Protocol = ianaPort(packet.TransportLayer().LayerType(), dstPort)
					}

					pckt.DstPort = dstPort
					pckt.SrcPort = srcPort

					// Extract the protocol, the source and the destination IP
					// addresses
					if packet.NetworkLayer().LayerType() == layers.LayerTypeIPv4 {
						ipv4 := packet.NetworkLayer().(*layers.IPv4)
						pckt.SrcIP = ipv4.SrcIP.String()
						pckt.DstIP = ipv4.DstIP.String()
					} else {
						pckt.Protocol = "N/A (IPv6)"
					}

					// Emit the packet data to Kafka
					emitPacket(pckt, producer)
				}
		}
	}
}

func ianaPort(layerType gopacket.LayerType, port uint16) string {
	if layerType == layers.LayerTypeTCP {
		proto, in := layers.TCPPortNames[layers.TCPPort(port)]
		if in {
			return fmt.Sprintf("%s (TCP)", proto)
		} else {
			return "N/A (TCP)"
		}
	} else {
		proto, in := layers.UDPPortNames[layers.UDPPort(port)]
		if in {
			return fmt.Sprintf("%s (UDP)", proto)
		} else {
			return "N/A (UDP)"
		}
	}
}

func main() {

	config := sarama.NewConfig()
	config.Producer.Compression = sarama.CompressionSnappy

	flag.StringVar(&kafkaBrokers, "brokers", "localhost:9092", "The kafka broker addresses")
	flag.Parse()

	brokers := []string{}

	for _, broker := range strings.Split(kafkaBrokers, ",") {
		brokers = append(brokers, broker)
	}

	producer, err := sarama.NewAsyncProducer(brokers, config)
	if err == nil {
		fmt.Println("Connected to Kafka brokers", "[" + kafkaBrokers + "]")
		ifaces, err := net.Interfaces()
		if err != nil {
			log.Fatal("Cannot get network interfaces")
		}
		for _, iface := range ifaces {
			addrs, _ := iface.Addrs()
			if iface.Name != "lo" && len(addrs) > 0 {
				fmt.Printf("Starting live capture on %s interface...", iface.Name)
				decodePackets(iface.Name, producer)
			}

		}
	} else {
		log.Fatal("Can't create the Kafka producer")
	}
}
