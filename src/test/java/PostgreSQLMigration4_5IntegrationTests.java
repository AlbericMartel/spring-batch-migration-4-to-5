/*
 * Copyright 2020-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.datasource.init.ScriptStatementFailedException;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Alberic Martel
 */
@Testcontainers
@SpringJUnitConfig
class PostgreSQLMigration4_5IntegrationTests {

	private static final DockerImageName POSTGRESQL_IMAGE = DockerImageName.parse("postgres:15.1");

	@Container
	public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(POSTGRESQL_IMAGE);

	@Autowired
	private DataSource dataSource;

	/**
	 * schema-postgresql-spring-batch-4.3.7.sql is the creation schema extracted from spring-batch-core 4.3.7.
	 * The syntax of migration file provided in spring batch 5.0.0 is invalid for POSTGRESQL.
	 */
	@Test
	void migration4To5Failed() {
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource("/schema-postgresql-spring-batch-4.3.7.sql"));
		databasePopulator.addScript(new ClassPathResource("/org/springframework/batch/core/migration/5.0/migration-postgresql.sql"));

		assertThatCode(() -> databasePopulator.execute(this.dataSource)).isInstanceOf(
				ScriptStatementFailedException.class);
	}

	/**
	 * The file /src/test/resources/migration-postgresql-4-to-5.sql is a proposition for a POSTGRESQL working migration.
	 */
	@Test
	void migration4To5Success() {
		ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
		databasePopulator.addScript(new ClassPathResource("/schema-postgresql-spring-batch-4.3.7.sql"));
		databasePopulator.addScript(new ClassPathResource("/migration-postgresql-4-to-5.sql"));

		assertThatCode(() -> databasePopulator.execute(this.dataSource)).doesNotThrowAnyException();
	}

	@Configuration
	@EnableBatchProcessing
	static class TestConfiguration {

		@Bean
		public DataSource dataSource() throws Exception {
			PGSimpleDataSource datasource = new PGSimpleDataSource();
			datasource.setURL(postgres.getJdbcUrl());
			datasource.setUser(postgres.getUsername());
			datasource.setPassword(postgres.getPassword());
			return datasource;
		}

		@Bean
		public JdbcTransactionManager transactionManager(DataSource dataSource) {
			return new JdbcTransactionManager(dataSource);
		}
	}

}
