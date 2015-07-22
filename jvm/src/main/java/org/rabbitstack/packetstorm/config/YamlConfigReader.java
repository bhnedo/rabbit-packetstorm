package org.rabbitstack.packetstorm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * Yaml config file reader.
 *
 * @author Nedim Sabic
 */
public class YamlConfigReader {

    private ObjectMapper om = new ObjectMapper(new YAMLFactory());
    private TopologyConfig config;

    public YamlConfigReader() throws IOException {
        InputStream is = this.getClass().getResourceAsStream("/config.yml");
        this.config = om.readValue(is, TopologyConfig.class);
    }

    public TopologyConfig getConfig() {
        return config;
    }
}