package org.rabbitstack.packetstorm.state;

import org.rabbitstack.packetstorm.state.sql.PostgresqlStateConfig;
import storm.trident.state.StateType;

/**
 * Creates the instances of <code>PostgresqlStateConfig</code> class.
 *
 * @author Nedim Sabic
 */
public class PostgresqlStateConfigFactory {

    public static String dbUrl;

    public static PostgresqlStateConfig newPostgresqlStateConfig(String table, String keyColumns[], String valueColumns[]) {
        PostgresqlStateConfig config = new PostgresqlStateConfig();
        config.setUrl(dbUrl);
        config.setTable(table);
        config.setKeyColumns(keyColumns);
        config.setValueColumns(valueColumns);
        config.setType(StateType.TRANSACTIONAL);
        config.setCacheSize(1000);

        return config;
    }

}
