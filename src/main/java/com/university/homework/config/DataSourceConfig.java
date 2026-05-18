package com.university.homework.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** PostgreSQL DataSource configuration with HikariCP */
@Configuration
@EnableConfigurationProperties(DataSourceConfig.DatabaseProperties.class)
public class DataSourceConfig {

  private final DatabaseProperties databaseProperties;

  public DataSourceConfig(DatabaseProperties databaseProperties) {
    this.databaseProperties = databaseProperties;
  }

  @Bean
  public DataSource dataSource() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl(databaseProperties.getUrl());
    config.setUsername(databaseProperties.getUsername());
    config.setPassword(databaseProperties.getPassword());
    config.setMaximumPoolSize(databaseProperties.getMaxPoolSize());
    config.setMinimumIdle(databaseProperties.getMinIdle());
    config.setConnectionTimeout(databaseProperties.getConnectionTimeout());
    config.setIdleTimeout(databaseProperties.getIdleTimeout());
    config.setMaxLifetime(databaseProperties.getMaxLifetime());
    config.setAutoCommit(true);
    config.setPoolName("HomeworkSearchPool");

    return new HikariDataSource(config);
  }

  @Getter
  @Setter
  @ConfigurationProperties(prefix = "datasource")
  public static class DatabaseProperties {
    private String url ;
    private String username;
    private String password ;
    private int maxPoolSize = 20;
    private int minIdle = 5;
    private long connectionTimeout = 30000;
    private long idleTimeout = 600000;
    private long maxLifetime = 1800000;
  }
}
