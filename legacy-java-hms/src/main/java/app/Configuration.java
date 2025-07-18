package app;

import app.core.Context;
import app.util.RequiredFileMapper;
import app.util.TypeMapperPlugin;
import lib.gintec_rdl.jini.annotations.Property;
import org.simplejavamail.api.mailer.config.TransportStrategy;
import org.sql2o.Sql2o;

import java.io.File;

/**
 * <p>Configuration as loaded from .ini file</p>
 */
public final class Configuration implements Context {

    @Property(name = "IPAddress", default_value = "0.0.0.0")
    public String IPAddress;

    @Property(name = "ListenPort", default_value = "80")
    public int ListenPort;

    @Property(name = "DatabaseUser")
    public String DatabaseUser;

    @Property(name = "DataSource")
    public String DataSource;

    @Property(name = "DatabasePassword")
    public String DatabasePassword;

    @Property(name = "PasswordHashRounds")
    public int PasswordHashRounds;

    @Property(name = "DebugMode")
    public boolean DebugMode;

    @Property(name = "SuperAdminLocalHostLoginOnly")
    public boolean LocalHostLogInOnly;

    @Property(name = "SessionTTLMinutes")
    public int SessionTTLMinutes;

    @Property(name = "ProfileImageDir", mapper = TypeMapperPlugin.class)
    public File ImageDirectory;

    @Property(name = "EnableLogging")
    public boolean EnableLogging;

    @Property(name = "LogDirectory", mapper = RequiredFileMapper.class)
    public File LogDirectory;

    @Property(name = "AttachmentDir", mapper = TypeMapperPlugin.class)
    public File AttachmentDirectory;

    @Property(name = "SSLKeystoreFile", required = false, mapper = TypeMapperPlugin.class)
    public File SSLKeystoreFile;

    @Property(name = "SSLKeystorePassphrase", required = false)
    public String SSLKeystorePassphrase;

    @Property(name = "EnableSSL")
    public boolean EnableSSL;

    @Property(name = "MaxUploadFileSize")
    public long MaxUploadFileSize;

    @Property(name = "MaxRequestSize")
    public long MaxRequestSize;

    @Property(name = "ImageMimeTypes", mapper = TypeMapperPlugin.class)
    public String[] ImageMimeTypes;

    @Property(name = "AttachmentMimeTypes", mapper = TypeMapperPlugin.class)
    public String[] AttachmentMimeTypes;

    @Property(name = "SmtpHost")
    public String SmtpHost;

    @Property(name = "SmtpUser")
    public String SmtpUser;

    @Property(name = "SmtpPassword")
    public String SmtpPassword;

    @Property(name = "SmtpPort")
    public int SmtpPort;

    @Property(name = "EmailSender")
    public String EmailSender;

    @Property(name = "ServerName")
    public String ServerName;

    @Property(name = "SmtpTransport", mapper = TypeMapperPlugin.class)
    public TransportStrategy SmtpTransport;

    private Sql2o sql2oInstance;

    public void setSql2oInstance(Sql2o sql2oInstance) {
        this.sql2oInstance = sql2oInstance;
    }

    public Sql2o getSql2oInstance() {
        return sql2oInstance;
    }
}
