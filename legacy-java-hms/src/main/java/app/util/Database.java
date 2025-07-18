package app.util;

import app.Configuration;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import javax.sql.DataSource;

public interface Database {
    public static void MigrateDatabase(Configuration configuration) {
        Flyway flyway = Flyway
                .configure()
                .dataSource(
                        configuration.DataSource,
                        configuration.DatabaseUser,
                        configuration.DatabasePassword
                ).load();

        final MigrationInfo migrationInfo = flyway.info().current();
        if (migrationInfo == null) {
            flyway.clean();
        }
        flyway.migrate();
    }
}
