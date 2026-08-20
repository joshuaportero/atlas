package dev.portero.atlas.data;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlConsumer {

    void accept(Connection connection) throws SQLException;
}
