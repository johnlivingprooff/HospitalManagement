package app.util;

import java.io.InputStream;
import java.io.OutputStream;

/*
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.jpeg.JpegDirectory;
import com.drew.metadata.png.PngDirectory;*/

public interface IoUtils {

    static boolean copyStream(InputStream inputStream, OutputStream outputStream) {
        try {
            final byte[] buffer = new byte[1024];
            int read;
            while (true) {
                read = inputStream.read(buffer);
                if (read <= 0) {
                    break;
                }
                outputStream.write(buffer, 0, read);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
