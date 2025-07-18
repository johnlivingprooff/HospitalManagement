package app.services.permission;

import app.Configuration;
import app.core.ServiceImpl;
import app.core.annotations.ServiceDescriptor;
import app.daos.permission.PermissionDao;
import app.models.account.Account;
import app.models.account.AccountType;
import app.models.permission.Permission;
import app.models.role.Role;
import app.models.role.Roles;

import java.util.List;

@ServiceDescriptor
public final class PermissionService extends ServiceImpl {
    /**
     * Service constructor
     *
     * @param configuration Application configuration
     */
    public PermissionService(Configuration configuration) {
        super(configuration);
    }

    public List<Permission> getRolePermissionList(String roleKey) {
        return withDao(PermissionDao.class).getRolePermissions(roleKey);
    }

    public Role getRole(String roleKey) {
        return withDao(PermissionDao.class).getRole(roleKey);
    }

    public Role getRoleById(long roleId) {
        return withDao(PermissionDao.class).getRoleById(roleId);
    }

    public List<Permission> getPermissions() {
        return withDao(PermissionDao.class).getPermissions();
    }

    /**
     * @return List of active roles
     */
    public List<Role> getRoles() {
        return withDao(PermissionDao.class).getRoles();
    }

    public List<Role> getActiveRoles() {
        return withDao(PermissionDao.class).getActiveRoles();
    }

    private String createPermissionIdListQuery(long[] permissions) {
        String sql = "INSERT INTO RolePermission (RoleKey, PermissionKey) SELECT ?, PermissionKey FROM Permissions WHERE Id IN(";
        StringBuilder builder = new StringBuilder(sql);
        for (int i = 0; i < permissions.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(permissions[i]);
        }
        builder.append(')');
        return builder.toString();
    }

    public void addRole(Role role) {
        role.setId(withDao(PermissionDao.class).addRole(role));
    }

    public void updateRole(Role role) {
        withDao(PermissionDao.class).updateRole(role);
    }

    /**
     * @param roleId .
     * @return Number of users assigned to this role
     */
    public long getRoleUsageCount(long roleId) {
        return withDao(PermissionDao.class).getRoleUsageCount(roleId);
    }


    /**
     * <p>Asserts that the given account's role is Administrator</p>
     *
     * @param account Account whose role to assert
     * @return True if system administrator, false otherwise
     */
    public boolean isRootSystemAdministrator(Account account) {
        return Roles.Administrator.equalsIgnoreCase(account.getRoleKey());
    }

    public String getNextRoleKey() {
        return withDao(PermissionDao.class).getNextRoleKey();
    }

    public List<Permission> getRemainingRolePermissions(Role role) {
        return withDao(PermissionDao.class).getRemainingRolePermissions(role.getRoleKey());
    }

    public void addRolePermission(Role role, Permission permission) {
        withDao(PermissionDao.class).addRolePermission(role.getRoleKey(), permission.getPermissionKey());
    }

    public void removeRolePermission(Role role, Permission permission) {
        withDao(PermissionDao.class).removeRolePermission(role.getRoleKey(), permission.getPermissionKey());
    }

    public boolean doesRoleHavePermission(Role role, Permission permission) {
        return withDao(PermissionDao.class).doesRoleHavePermission(role.getRoleKey(), permission.getPermissionKey());
    }

    public Permission getPermissionById(long id) {
        return withDao(PermissionDao.class).getPermissionById(id);
    }

    public Permission getPermissionParent(Permission permission) {
        return withDao(PermissionDao.class).getPermissionParent(permission);
    }

    public int getRolePermissionDependenceCount(Role role, Permission parentPermission, Permission childPermission) {
        return withDao(PermissionDao.class)
                .getDependenceCount(
                        role.getRoleKey(),
                        parentPermission.getPermissionKey(),
                        childPermission.getPermissionKey()
                );
    }

    public List<? extends Permission> getAccountTypePermissions(AccountType accountType) {
        return executeSelect((SqlSelectTask<List<? extends Permission>>) connection -> {
            String sql = "select p.* " +
                    "from extra_permissions e " +
                    "join permission p on p.permissionKey = e.permission_key " +
                    "where e.account_type = :type";
            return connection.createQuery(sql)
                    .addParameter("type", accountType)
                    .executeAndFetch(Permission.class);
        });
    }
}
