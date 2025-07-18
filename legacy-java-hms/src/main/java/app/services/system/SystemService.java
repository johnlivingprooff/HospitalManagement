package app.services.system;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.system.SystemDao;
import app.models.system.SystemSettings;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

@ServiceDescriptor(priority = -1000)
public final class SystemService extends ServiceImpl {
    private static final int FILE_SIZE_THRESHOLD = 0;

    private final MultipartConfigElement attachmentUploadConfig;
    private final MultipartConfigElement pictureUploadConfig;

    private final Set<String> pictureMimeTypes;
    private final Set<String> attachmentMimeTypes;

    private String version;
    private String timestamp;
    private String artifactId;

    private String serverName;

    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public SystemService(Configuration configuration) throws FileNotFoundException {
        super(configuration);

        verifyFolderExists(configuration.ImageDirectory);
        verifyFolderExists(configuration.AttachmentDirectory);

        // File upload configurations
        attachmentUploadConfig = new MultipartConfigElement(
                configuration.AttachmentDirectory.getAbsolutePath(),
                configuration.MaxUploadFileSize,
                configuration.MaxRequestSize,
                FILE_SIZE_THRESHOLD
        );
        pictureUploadConfig = new MultipartConfigElement(
                configuration.ImageDirectory.getAbsolutePath(),
                configuration.MaxUploadFileSize,
                configuration.MaxRequestSize,
                FILE_SIZE_THRESHOLD
        );

        pictureMimeTypes = Set.of(configuration.ImageMimeTypes);
        attachmentMimeTypes = Set.of(configuration.AttachmentMimeTypes);

        serverName = (configuration.EnableSSL ? "https://" : "http://") + configuration.ServerName;

        if (configuration.ListenPort != 80 && configuration.ListenPort != 443) {
            serverName += ":" + configuration.ListenPort;
        }

        loadVersionInformation();
    }

    private void verifyFolderExists(File file) throws FileNotFoundException {
        if (!file.isDirectory()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
    }

    public MultipartConfigElement getPictureUploadConfig() {
        return pictureUploadConfig;
    }

    public MultipartConfigElement getAttachmentUploadConfig() {
        return attachmentUploadConfig;
    }

    public Set<String> getPictureMimeTypes() {
        return pictureMimeTypes;
    }

    public Set<String> getAttachmentMimeTypes() {
        return attachmentMimeTypes;
    }

    public SystemSettings getSettings() {
        return withDao(SystemDao.class).getSystemSettings();
    }

    public void updateSystemSettings(SystemSettings settings) {
        withDao(SystemDao.class).updateSystemSettings(settings);
    }

    private void loadVersionInformation() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("hms.properties")) {
            properties.load(inputStream);
        } catch (Exception e) {
            getLogger().error("Failed to load version information", e);
        }
        version = properties.getProperty("app.version", "n/a");
        artifactId = properties.getProperty("app.name", "n/a");
        timestamp = properties.getProperty("app.build.timestamp", "n/a");
    }

    public String getVersion() {
        return version;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBuildInformation() {
        return getArtifactId() + "-" + getVersion() + " " + getTimestamp();
    }

    public String getWebsiteBaseUrl() {
        return serverName;
    }
}
