package dev.portero.atlas.data;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlFunction<T> {

    T apply(Connection connection) throws SQLException;
}
