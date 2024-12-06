package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Role;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoleRepository extends BaseTable {
    public RoleRepository() throws SQLException, CommonException {
        initializeData();
    }
    
    public boolean initializeData() throws CommonException {
        List<String> necessaryRoles = List.of(
                "USER",
                "ADMIN"
        );
        boolean dataWasInitialized = false;
        List<Role> roles = getAllRoles();
        for (String s : necessaryRoles)
            if (roles.stream().noneMatch(r -> r.getRoleName().equals(s))) {
                saveRole(new Role(null, s));
                dataWasInitialized = true;
            }
        return dataWasInitialized;
    }

    public UUID saveRole(@NonNull Role role) throws CommonException {
        if (role.getRoleName() == null || role.getRoleName().isEmpty())
            throw new CommonException("При сохранении роли все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO roles VALUES (?,?);");
            UUID roleId = UUID.randomUUID();
            ps.setObject(1, roleId);
            ps.setString(2, role.getRoleName());
            super.executeSqlStatementUpdate(ps);
            return roleId;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateRole(@NonNull Role role) throws CommonException {
        if (role.getRoleId() == null || role.getRoleName() == null || role.getRoleName().isEmpty())
            throw new CommonException("При обновлении роли все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE roles SET role_name=? WHERE role_id=?;");
            ps.setString(1, role.getRoleName());
            ps.setObject(2, role.getRoleId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteRole(@NonNull UUID roleId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM roles WHERE role_id=?;"
            );
            ps.setObject(1, roleId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Role getRoleById(@NonNull UUID roleId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT r.role_id,r.role_name FROM roles r WHERE r.role_id=?;"
            );
            ps.setObject(1, roleId);
            return getRole(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Role> getAllRoles() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT r.role_id,r.role_name FROM roles r;"
            );
            return getRoleList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Role getRoleByName(@NonNull String roleName) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT r.role_id,r.role_name FROM roles r WHERE r.role_name=?;"
            );
            ps.setString(1, roleName);
            return getRole(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Role getRole(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Role role = null;
            if (rs.next()) {
                role = new Role(
                        rs.getObject(1, UUID.class),
                        rs.getString(2)
                );
            }

            rs.close();
            ps.close();
            super.close();
            return role;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Role> getRoleList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Role> roleList = new ArrayList<>();
            while (rs.next()) {
                roleList.add(new Role(
                        rs.getObject(1, UUID.class),
                        rs.getString(2)
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return roleList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
