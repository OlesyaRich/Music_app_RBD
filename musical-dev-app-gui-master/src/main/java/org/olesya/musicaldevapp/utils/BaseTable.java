package org.olesya.musicaldevapp.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaseTable implements AutoCloseable {
    protected Connection connection;

    public BaseTable() throws SQLException {
        connection = Connector.getConnection();
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed())
            connection.close();
    }

    /**
     * Метод для получения подготовленного к выполнению SQL-выражения
     * @param sql SQL-выражение в String формате
     * @return Подготовленное к выполнению SQL-выражение PreparedStatement
     * @throws SQLException Ошибка при работе с БД
     */
    protected PreparedStatement prepareStatement(String sql) throws SQLException {
        reopenConnection();
        return connection.prepareStatement(sql);
    }

    /**
     * Метод для выполнения выражений модификации данных
     * @param sqlStatement SQL-выражение для выполнения
     * @return (1) количество строк для SQL DML, или (2) 0, если выражение не вернуло никаких данных
     * @throws SQLException Ошибка при работе с БД
     */
    protected int executeSqlStatementUpdate(PreparedStatement sqlStatement) throws SQLException {
        reopenConnection();
        int result = sqlStatement.executeUpdate();
        sqlStatement.close();
        connection.close();
        return result;
    }

    /**
     * Метод для выполнения выражений чтения данных. !!!!!ВАЖНО НЕ ЗАБЫВАТЬ ЗАКРЫВАТЬ ResultSet, Statement и Connection ПОСЛЕ ПОЛУЧЕНИЯ РЕЗУЛЬТАТА ИЗ ResultSet
     * @param sqlStatement SQL-выражение для выполнения
     * @return Результат выполнения выражения в виде ResultSet, или null, в случае, если выражение не вернула никаких данных
     * @throws SQLException Ошибка при работе с БД
     */
    protected ResultSet executeSqlStatementRead(PreparedStatement sqlStatement) throws SQLException {
        reopenConnection();
        return sqlStatement.executeQuery();
    }

    /**
     * Метод для первичного открытия (если соединение не было открыто) или переоткрытия соединения с БД (если соединение было закрыто)
     * @throws SQLException Ошибка при работе с БД
     */
    private void reopenConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = Connector.getConnection();
        }
    }
}
