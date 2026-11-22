package ru.netology.data;

import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.handlers.ArrayListHandler;
import java.util.ArrayList;

public class SQLHelper {

    private static final QueryRunner runner = new QueryRunner();

    private final String jdbcUrl;
    private final String user;
    private final String password;

    public SQLHelper(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    @SneakyThrows
    public List<String> getPaymentIds() {
        String sql = "SELECT id FROM payment_entity";

        try (var conn = getConn()) {
            return runner.query(conn, sql, new ColumnListHandler<>());
        }
    }

    @SneakyThrows
    public String getPaymentStatus(String paymentId) {
        String sql = "SELECT status FROM payment_entity WHERE id = ?";

        try (var conn = getConn()) {
            return runner.query(conn, sql, new ScalarHandler<>(), paymentId);
        }
    }

    @SneakyThrows
    public List<String> getCreditRequestIds() {
        String sql = "SELECT id FROM credit_request_entity";

        try (var conn = getConn()) {
            return runner.query(conn, sql, new ColumnListHandler<>());
        }
    }

    @SneakyThrows
    public String getCreditRequestStatus(String creditRequestId) {
        String sql = "SELECT status FROM credit_request_entity WHERE id = ?";

        try (var conn = getConn()) {
            return runner.query(conn, sql, new ScalarHandler<>(), creditRequestId);
        }
    }

    @SneakyThrows
    public long getCreditOrderCount(String creditRequestId) {
        String sql = "SELECT COUNT(*) FROM order_entity o "
                + "JOIN credit_request_entity c ON o.credit_id = c.bank_id "
                + "WHERE c.id = ?";

        try (var conn = getConn()) {
            return runner.query(
                    conn,
                    sql,
                    new ScalarHandler<Long>(),
                    creditRequestId
            );
        }
    }


    @SneakyThrows
    public List<Object[]> getStoredRows() {
        try (var conn = getConn()) {
            List<Object[]> rows = new ArrayList<>();

            rows.addAll(runner.query(conn,
                    "SELECT * FROM payment_entity",
                    new ArrayListHandler()));

            rows.addAll(runner.query(conn,
                    "SELECT * FROM credit_request_entity",
                    new ArrayListHandler()));

            rows.addAll(runner.query(conn,
                    "SELECT * FROM order_entity",
                    new ArrayListHandler()));

            return rows;
        }
    }

}