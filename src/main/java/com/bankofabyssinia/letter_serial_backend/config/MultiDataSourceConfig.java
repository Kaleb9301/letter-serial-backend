package com.bankofabyssinia.letter_serial_backend.config;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableConfigurationProperties(MultiDataSourceProperties.class)
@ConditionalOnProperty(prefix = "app.datasource", name = "enabled", havingValue = "true")
public class MultiDataSourceConfig {

	@Bean(name = "namedDataSources")
	public Map<String, DataSource> namedDataSources(MultiDataSourceProperties properties) {
		if (properties.getConnections().isEmpty()) {
			throw new IllegalStateException("app.datasource.enabled=true but no app.datasource.connections configured");
		}

		Map<String, DataSource> dataSources = new LinkedHashMap<>();
		for (Map.Entry<String, MultiDataSourceProperties.Connection> entry : properties.getConnections().entrySet()) {
			String name = entry.getKey();
			MultiDataSourceProperties.Connection connection = entry.getValue();

			if (!StringUtils.hasText(connection.getUrl())) {
				throw new IllegalStateException("Missing app.datasource.connections." + name + ".url");
			}
			if (!StringUtils.hasText(connection.getUsername())) {
				throw new IllegalStateException("Missing app.datasource.connections." + name + ".username");
			}

			HikariDataSource dataSource = new HikariDataSource();
			dataSource.setJdbcUrl(connection.getUrl());
			dataSource.setUsername(connection.getUsername());
			dataSource.setPassword(connection.getPassword());
			if (StringUtils.hasText(connection.getDriverClassName())) {
				dataSource.setDriverClassName(connection.getDriverClassName());
			}
			if (connection.getMaximumPoolSize() != null) {
				dataSource.setMaximumPoolSize(connection.getMaximumPoolSize());
			}
			if (connection.getMinimumIdle() != null) {
				dataSource.setMinimumIdle(connection.getMinimumIdle());
			}

			dataSources.put(name, dataSource);
		}

		return dataSources;
	}

	@Primary
	@Bean
	public DataSource dataSource(
			@Qualifier("namedDataSources") Map<String, DataSource> namedDataSources,
			MultiDataSourceProperties properties
	) {
		if (namedDataSources.isEmpty()) {
			throw new IllegalStateException("No datasources configured");
		}
		if (StringUtils.hasText(properties.getPrimary())) {
			DataSource primary = namedDataSources.get(properties.getPrimary());
			if (primary == null) {
				throw new IllegalStateException("Primary datasource '" + properties.getPrimary() + "' is not defined");
			}
			return primary;
		}
		return namedDataSources.values().iterator().next();
	}

	@Bean(name = "namedJdbcTemplates")
	public Map<String, JdbcTemplate> namedJdbcTemplates(
			@Qualifier("namedDataSources") Map<String, DataSource> namedDataSources
	) {
		Map<String, JdbcTemplate> templates = new LinkedHashMap<>();
		namedDataSources.forEach((name, dataSource) -> templates.put(name, new JdbcTemplate(dataSource)));
		return templates;
	}

	@Bean(name = "namedTransactionManagers")
	public Map<String, PlatformTransactionManager> namedTransactionManagers(
			@Qualifier("namedDataSources") Map<String, DataSource> namedDataSources
	) {
		Map<String, PlatformTransactionManager> managers = new LinkedHashMap<>();
		namedDataSources.forEach((name, dataSource) -> managers.put(name, new DataSourceTransactionManager(dataSource)));
		return managers;
	}
}
