package com.game.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static HikariConfig config = new HikariConfig();
    private static HikariDataSource ds;

    static {
        // Cấu hình connection pool với HikariCP để tối ưu hiệu suất
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/game_db");
        config.setUsername("your_username");
        config.setPassword("your_password");
        config.setMaximumPoolSize(100); // Số lượng connection tối đa
        config.setMinimumIdle(20); // Số connection tối thiểu duy trì
        config.setIdleTimeout(300000); // Thời gian timeout cho connection không sử dụng
        config.setConnectionTimeout(20000); // Thời gian chờ để lấy connection
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}