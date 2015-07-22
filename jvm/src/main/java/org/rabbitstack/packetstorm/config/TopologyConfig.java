package org.rabbitstack.packetstorm.config;

/**
 * Maps the yaml configuration file to this class.
 *
 * @author Nedim Sabic
 */
public class TopologyConfig {

    private Db db;
    private Zookeeper zookeeper;

    public TopologyConfig()  {

    }

    public Db getDb() {
        return db;
    }

    public Zookeeper getZookeeper() {
        return zookeeper;
    }

    public class Db {

        private String url;

        public String getUrl() {
            return url;
        }
    }

    public class Zookeeper {

        private String hosts;

        public String getHosts() {
            return hosts;
        }
    }
}
