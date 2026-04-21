package org.fca_backend.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import io.github.cdimascio.dotenv.Dotenv;

@Configuration
public class DataSourceConfig {

  @Bean
  public DataSource dataSource () {
    Dotenv dotenv = Dotenv.load();

    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setUrl(dotenv.get("DB_URL"));
    dataSource.setUsername(dotenv.get("DB_USER"));
    dataSource.setPassword(dotenv.get("DB_PASSWORD"));

    return dataSource;
  }
}