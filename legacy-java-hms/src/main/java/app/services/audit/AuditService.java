package app.services.audit;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.audit.AuditDao;
import app.models.account.Account;
import app.models.audit.AuditLog;
import spark.Request;

import java.util.Date;
import java.util.List;


/**
 * This class is for logging all system events
 */
@ServiceDescriptor
public final class AuditService extends ServiceImpl {
    public AuditService(Configuration configuration) {
        super(configuration);
    }

    public void archiveExpiredLogs() {
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            String sql;
            sql = "update audit_log set archived = true " +
                    "where not archived and current_timestamp - created >= interval '14 days'";
            connection.createQuery(sql).executeUpdate();
            return null;
        });
    }

    public void deleteExpiredLog() {
        String sql = "delete" +
                "from audit_log " +
                "where archived " +
                "and (current_timestamp - created > interval '1 month')";
        executeUpdate((SqlUpdateTask<Void>) connection -> {
            connection.createQuery(sql);
            return null;
        });
    }

    /**
     * Audit log context
     */
    public enum LogType {
        /**
         * Authentication related log
         */
        Auth,
        /**
         * This log even is related to role activities
         */
        Role,
        /**
         * Actions related to Users module
         */
        User,
        /**
         * Action relation to patients module
         */
        Patient,
        System,
        General
    }

    public void log(LogType logType, String message, String where, Date when, String address) {
        withDao(AuditDao.class).createLog(logType, message, when, where, address);
    }

    public void auth(String message, String where, String address) {
        log(LogType.Auth, message, where, new Date(), address);
    }

    public void role(String message, String where, String address) {
        log(LogType.Role, message, where, new Date(), address);
    }

    public void user(String message, String where, String address) {
        log(LogType.User, message, where, new Date(), address);
    }

    public void patient(String message, String where, String address) {
        log(LogType.Patient, message, where, new Date(), address);
    }

    public void system(String message, String where, String address) {
        log(LogType.System, message, where, new Date(), address);
    }

    public void log(String message, Request request) {
        log(LogType.General, message, request.pathInfo(), new Date(), request.ip());
    }

    public void log(String message, LogEntry logEntry) {
        log(LogType.General, message, logEntry.path, new Date(), logEntry.ip);
    }

    public void log(String message) {
        log(LogType.General, message, "/", new Date(), "loopback");
    }

    public List<AuditLog> getFreshLogs() {
        return withDao(AuditDao.class).get(false);
    }

    public List<AuditLog> getArchivedLogs() {
        return withDao(AuditDao.class).get(true);
    }

    public void archiveLogs(long[] ids) {
        openHandle().useTransaction((handle, transactionStatus) -> {
            StringBuilder sb = new StringBuilder("UPDATE audit_log SET Archived = True WHERE Id IN(");
            int n = 0;
            for (long id : ids) {
                if (n > 0) {
                    sb.append(',');
                }
                sb.append(id);
                n++;
            }
            sb.append(')');
            final String sql = sb.toString();
            handle.update(sql);
        });
    }

    public static LogEntry createLogEntry(Request request) {
        return new LogEntry(request.session(false).attribute("account"), request.ip(), request.pathInfo());
    }

    public final static class LogEntry {
        private String ip;
        private String path;
        private Account account;

        LogEntry(Account account, String ip, String path) {
            this.account = account;
            this.ip = ip;
            this.path = path;
        }

        public Account getSubject() {
            return account;
        }
    }
}









