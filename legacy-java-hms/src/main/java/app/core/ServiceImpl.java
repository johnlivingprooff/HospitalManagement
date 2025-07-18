package app.core;

import app.Configuration;
import app.services.audit.AuditService;
import app.util.ReflectionUtil;
import org.skife.jdbi.v2.DBI;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service for interfacing with backend database
 */
public class ServiceImpl extends Service {

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public ServiceImpl(Configuration configuration) {
        super(configuration);
    }

    /**
     * <p>Use this method to create a customizable DBI interface connection</p>
     *
     * @return .
     */
    protected final DBI openHandle() {
        return new DBI(
                getSystemConfiguration().DataSource,
                getSystemConfiguration().DatabaseUser,
                getSystemConfiguration().DatabasePassword
        );
    }

    /**
     * @return System configuration
     */
    public final Configuration getSystemConfiguration() {
        return (Configuration) getContext();
    }

    /**
     * <p>Using the given class as a DAO, open a connection to the database</p>
     *
     * @param clazz DAO class
     * @param <T>   Type
     * @return Returns a database connection
     * @see DBI#onDemand(Class)
     */
    final protected <T> T withDao(Class<T> clazz) {
        return openHandle().onDemand(clazz);
    }

    final protected void auditLog(String message, AuditService.LogEntry logEntry) {
        getService(AuditService.class).log(message, logEntry);
    }

    final protected Sql2o getSql2oInstance() {
        return getSystemConfiguration().getSql2oInstance();
    }

    /**
     * <p>Helper method to run sql queries using Sql2o with an automatic cleanup mechanism that will throw a
     * runtime exception if an error occurs.</p>
     * <p>There's no need to close the passed connection as it will automatically be closed by the method</p>
     *
     * @param task Task to run
     * @throws RuntimeException .
     */
    final protected <T> T executeSelect(SqlSelectTask<T> task) throws RuntimeException {
        try (Connection connection = getSql2oInstance().open()) {
            return task.select(connection);
        } catch (Exception e) {
            throw logAndGenerateException(e.getMessage(), e);
        }
    }

    /**
     * Runs the encapsulated queries inside a transaction.
     *
     * @param task .
     * @return .
     * @see #executeSelect(SqlSelectTask)
     */
    /*final protected <T> T executeUpdate(SqlUpdateTask<T> task) throws RuntimeException {
        try (Connection connection = getSql2oInstance().open()) {
            return task.execute(connection);
        } catch (Exception e) {
            throw logAndGenerateException(e.getMessage(), e);
        }
    }*/
    final protected <T> T executeUpdate(SqlUpdateTask<T> task) throws RuntimeException {
        try {
            return getSql2oInstance().runInTransaction((connection, o) -> {
                return task.execute(connection);
            });
        } catch (Exception e) {
            throw logAndGenerateException(e.getMessage(), e);
        }
    }

    final protected Query bindParameters(Query query, Object bean) {
        Map<String, List<Integer>> mappings;
        mappings = query.getParamNameToIdxMap();

        for (String key : mappings.keySet()) {
            query.addParameter(key, ReflectionUtil.getPropertyValue(bean, key, 3));
        }

        return query;
    }

    final protected RuntimeException logAndGenerateException(String message, Exception e) {
        getLogger().error(message, e);
        return new RuntimeException(e);
    }

    final protected void logAndThrowException(String message, Exception e) throws RuntimeException {
        throw logAndGenerateException(message, e);
    }

    public interface SqlSelectTask<T> {
        T select(Connection connection);
    }

    public interface SqlUpdateTask<T> {
        T execute(Connection connection);
    }

    protected final String format(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }
}
