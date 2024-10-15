package com.clickhousedemo.config;

import com.clickhouse.jdbc.ClickHouseDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ClickHouseConfig {

    @Bean
    public ClickHouseDataSource clickHouseDataSource() throws SQLException {
        Properties properties = new Properties();
        String url = "jdbc:clickhouse://localhost:8123/default";
        return new ClickHouseDataSource(url, properties);
    }
}