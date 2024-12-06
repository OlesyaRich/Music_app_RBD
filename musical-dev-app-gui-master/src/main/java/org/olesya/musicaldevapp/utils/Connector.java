package org.olesya.musicaldevapp.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Connector {
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:postgresql://localhost:6200/musical_dev_app_db",
                "root",
                "123"
        );
    }
}
