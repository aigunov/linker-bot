package backend.academy.scrapper.config;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.springframework.stereotype.Service;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class LiquibaseMigrationService {

    public void migrate(Path changelogPath, String url, String username, String password) throws SQLException, LiquibaseException {
        Connection connection = DriverManager.getConnection(url, username, password);
        try {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changelogPath.toString() + "/db.changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update();
        } finally {
            connection.close();
        }
    }
}
