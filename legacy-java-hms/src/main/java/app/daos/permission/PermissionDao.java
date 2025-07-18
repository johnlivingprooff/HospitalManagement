package app.daos.permission;

import app.models.permission.Permission;
import app.models.permission.PermissionMapper;
import app.models.role.Role;
import app.models.role.RoleMapper;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import java.util.List;

@RegisterMapper({PermissionMapper.class, RoleMapper.class})
public interface PermissionDao {

    @SqlQuery("SELECT p.* FROM role_permission rp JOIN permission p ON p.permissionKey = rp.permissionKey WHERE rp.RoleKey = :role")
    List<Permission> getRolePermissions(@Bind("role") String roleKey);

    @SqlQuery("SELECT * FROM role WHERE roleKey = :roleKey LIMIT 1")
    Role getRole(@Bind("roleKey") String roleKey);

    @SqlQuery("SELECT * FROM role WHERE Id = :roleId LIMIT 1")
    Role getRoleById(@Bind("roleId") long roleId);

    @SqlQuery("SELECT * FROM permission where privileged = false")
    List<Permission> getPermissions();

    @SqlQuery("SELECT * FROM role WHERE privileged = false order by modified desc")
    List<Role> getRoles();

    @SqlQuery("SELECT * FROM role WHERE active = True and privileged = false order by modified desc")
    List<Role> getActiveRoles();

    @SqlQuery("SELECT COUNT(account.id) from account JOIN role on role.roleKey = account.roleKey WHERE role.id = :id")
    long getRoleUsageCount(@Bind("id") long roleId);

    @SqlUpdate("DELETE FROM role_permission WHERE roleKey = :key")
    void removeRolePermissions(@Bind("key") String roleKey);

    @SqlUpdate("insert into role (roleKey, roleName, roleDescription, created, modified, systemRole, active, privileged) " +
            "values (:roleKey, :roleName, :roleDescription, :created, :modified, false, :active, false)")
    @GetGeneratedKeys
    long addRole(@BindBean Role role);

    @SqlUpdate("update role set roleName = :roleName, roleDescription = :roleDescription, " +
            "modified = :modified, active = :active where id = :id")
    void updateRole(@BindBean Role role);

    @SqlQuery("select ('SystemRole' || greatest(1, id) + 1) as roleName from role order by id desc limit 1")
    String getNextRoleKey();

    @SqlQuery("select * from permission where permissionKey " +
            "not in (select permissionKey from role_permission where roleKey = :role) and privileged = false")
    List<Permission> getRemainingRolePermissions(@Bind("role") String roleKey);

    @SqlUpdate("insert into role_permission (roleKey, permissionKey) values (:roleKey, :permissionKey)")
    void addRolePermission(@Bind("roleKey") String roleKey, @Bind("permissionKey") String permissionKey);

    @SqlUpdate("delete from role_permission where roleKey = :roleKey and permissionKey = :permissionKey")
    void removeRolePermission(@Bind("roleKey") String roleKey, @Bind("permissionKey") String permissionKey);

    @SqlQuery("select exists (select 1 from role_permission where roleKey = :roleKey and permissionKey = :permissionKey)")
    boolean doesRoleHavePermission(@Bind("roleKey") String roleKey, @Bind("permissionKey") String permissionKey);

    @SqlQuery("select * from permission where id = :id")
    Permission getPermissionById(@Bind("id") long id);

    @SqlQuery("select p.* from permission p join permissionDependency pd on pd.parent = p.permissionKey where pd.child = :permissionKey")
    Permission getPermissionParent(@BindBean Permission permission);

    @SqlQuery("select count(rp.id)\n" +
            "from role_permission rp\n" +
            "join permissionDependency pd on pd.child = rp.permissionKey and pd.child != :childPermission\n" +
            "where pd.parent = :parentPermission and rp.roleKey = :roleKey")
    int getDependenceCount(@Bind("roleKey") String roleKey, @Bind("parentPermission") String parent, @Bind("childPermission") String child);
}
