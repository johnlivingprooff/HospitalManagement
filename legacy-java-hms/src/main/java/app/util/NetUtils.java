package app.util;

import java.net.InetAddress;

public interface NetUtils {
    static boolean isLoopbackAddress(String address) {
        try {
            return InetAddress.getByName(address).isLoopbackAddress();
        } catch (Exception ignore) {
            return false;
        }
    }
}
