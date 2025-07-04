package backend.academy.scrapper.config;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.UpdateCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationsRunner {
    private final DataSourceConfig dataSourceConfig;

    public void runMigrations() {
        Path currentPath = Path.of(".").toAbsolutePath();
        Path parent1 = currentPath.getParent();
        if (parent1 == null) {
            throw new RuntimeException("Could not find parent directory for: " + currentPath);
        }
        Path parent2 = parent1.getParent();
        if (parent2 == null) {
            throw new RuntimeException("Could not find grandparent directory for: " + currentPath);
        }
        Path migrationsPath = parent2.resolve("migrations");

        try (Connection connection = DriverManager.getConnection(
                dataSourceConfig.url(), dataSourceConfig.username(), dataSourceConfig.password())) {
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            DirectoryResourceAccessor resourceAccessor = new DirectoryResourceAccessor(migrationsPath.toFile());

            CommandScope updateCommand = new CommandScope("update");
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, "db.changelog-master.xml");

            Scope.child("resourceAccessor", resourceAccessor, () -> {
                try {
                    updateCommand.execute();
                } catch (LiquibaseException e) {
                    log.error("Произошла ошибка во время выполнения команды Liquibase", e);
                    throw new RuntimeException(e);
                }
            });

            log.info("Миграции Liquibase успешно применены.");

        } catch (SQLException e) {
            log.error("Ошибка подключения к базе данных", e);
            throw new RuntimeException(e);
        } catch (LiquibaseException | IOException e) {
            log.error("Произошла ошибка во время инициализации Liquibase", e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
