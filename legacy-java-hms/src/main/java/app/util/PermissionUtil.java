package app.util;

import app.models.permission.Permission;

import java.util.List;

public interface PermissionUtil {
    static boolean hasPermission(String permission, List<Permission> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (Permission p : list) {
            if (p.getPermissionKey().equalsIgnoreCase(permission)) {
                return true;
            }
        }
        return false;
    }

    static boolean hasPermissions(String[] permissions, List<Permission> list) {
        if (list == null || list.isEmpty()) {
            return false;
        }
        for (String permission : permissions) {
            if (!hasPermission(permission, list)) {
                return false;
            }
        }
        return true;
    }
}
