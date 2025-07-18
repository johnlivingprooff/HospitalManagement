package app.models.role;

/**
 * <p>Built-in roles that come with the system</p>
 */
public interface Roles {
    String Administrator = "Administrator";
    String Receptionist = "Receptionist";
    String Doctor = "Doctor";
    String Nurse = "Nurse";
    String Patient = "Patient";

    static boolean canModifyRole(String roleKey) {
        return !Doctor.equalsIgnoreCase(roleKey)
                && !Nurse.equalsIgnoreCase(roleKey)
                && !Administrator.equalsIgnoreCase(roleKey);
    }
}
