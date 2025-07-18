package app.services;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.util.LocaleUtil;

import java.io.File;

@ServiceDescriptor
public final class UploadService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public UploadService(Configuration configuration) {
        super(configuration);
    }

    public File getProfileImageDirectory() {
        return getSystemConfiguration().ImageDirectory;
    }

    public File getDocumentDirectory() {
        return getSystemConfiguration().AttachmentDirectory;
    }

    public void deleteImageFile(String name) {
        if (!LocaleUtil.isNullOrEmpty(name)) {
            File image = new File(getProfileImageDirectory(), name);
            try {
                if (!image.delete()) {
                    getLogger().warn("Could not delete image file {}", image);
                }
            } catch (Exception e) {
                getLogger().error("Error deleting file {}", e);
            }
        }
    }
}
