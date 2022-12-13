This repository aims at exposing a bug of postgresql migration between spring batch 4.3.7 and 5.0.0.

On my project, I faced this bug as I needed to use liquibase to populate my DB, only the liquibase user being able to alter the schema.

Here where my changesets:

- Loaded with spring-batch 4.3.7:
```xml
<changeSet id="create_spring_batch_tables" author="amartel" dbms="postgresql">
  <sqlFile path="classpath:/org/springframework/batch/core/schema-postgresql.sql"
    relativeToChangelogFile="false"
    splitStatements="true"
    stripComments="true"
  />
</changeSet> 
```

- Update with spring-batch 5.0.0:
```xml
<changeSet id="update_spring_batch_tables" author="amartel" dbms="postgresql">
  <sqlFile path="classpath:/org/springframework/batch/core/migration/5.0/migration-postgresql.sql"
    relativeToChangelogFile="false"
    splitStatements="true"
    stripComments="true"
  />
</changeSet>
```

In the current repository, the `PostgreSQLMigration4_5IntegrationTests` presents 2 tests:
- The first one with the invalid migration file for postgresql
- The second one using a proposition to fix it: [migration-postgresql-4-to-5.sql](src%2Ftest%2Fresources%2Fmigration-postgresql-4-to-5.sql)
