package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.User;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserRepository extends BaseTable {
    public UserRepository() throws SQLException {
    }

    public UUID saveUser(@NonNull User user) throws CommonException {
        if (user.getRoleId() == null || user.getUserName() == null || user.getUserName().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty())
            throw new CommonException("При сохранении пользователя все поля должны быть заполнены");
        UUID userId = UUID.randomUUID();
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO users VALUES (?,?,?,?,?);");
            ps.setObject(1, userId);
            ps.setObject(2, user.getRoleId());
            ps.setString(3, user.getUserName());
            if (user.getUserAge() == null)
                ps.setNull(4, Types.SMALLINT);
            else
                ps.setInt(4, user.getUserAge());
            ps.setString(5, user.getPassword());
            super.executeSqlStatementUpdate(ps);
            return userId;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateUser(@NonNull User user) throws CommonException {
        if (user.getUserId() == null || user.getRoleId() == null || user.getUserName() == null || user.getUserName().isEmpty()
                || user.getPassword() == null || user.getPassword().isEmpty())
            throw new CommonException("При обновлении пользователя все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE users SET role_id=?,username=?,user_age=?,password=? WHERE user_id=?;");
            ps.setObject(1, user.getRoleId());
            ps.setString(2, user.getUserName());
            if (user.getUserAge() == null)
                ps.setNull(3, Types.SMALLINT);
            else
                ps.setInt(3, user.getUserAge());
            ps.setString(4, user.getPassword());
            ps.setObject(5, user.getUserId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteUser(@NonNull UUID userId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM users WHERE user_id=?;"
            );
            ps.setObject(1, userId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public User getUserById(@NonNull UUID userId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u WHERE u.user_id=?;"
            );
            ps.setObject(1, userId);
            return getUser(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<User> getAllUsers() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u;"
            );
            return getUserList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public User getUserByUserNameAndPassword(String username, String password) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u WHERE u.username=? AND u.password=?;"
            );
            ps.setString(1, username);
            ps.setString(2, password);
            return getUser(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<User> getUsersByRoleIdAndUsername(UUID roleId, String userName) throws CommonException {
        try {
            PreparedStatement ps = null;

            if (roleId != null && userName != null && !userName.isEmpty()) {
                ps = super.prepareStatement(
                        "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u WHERE u.role_id=? AND u.username=?;"
                );
                ps.setObject(1, roleId);
                ps.setString(2, userName);
            } else if (roleId != null) {
                ps = super.prepareStatement(
                        "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u WHERE u.role_id=?;"
                );
                ps.setObject(1, roleId);
            } else if (userName != null && !userName.isEmpty()) {
                ps = super.prepareStatement(
                        "SELECT u.user_id,u.role_id,u.username,u.user_age,u.password FROM users u WHERE u.username=?;"
                );
                ps.setString(1, userName);
            }

            return ps == null ? null : getUserList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private User getUser(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            User user = null;
            while (rs.next()) {
                user = new User(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class),
                        rs.getString(3),
                        rs.getString(5),
                        rs.getInt(4)
                );

                RoleRepository roleRepository = new RoleRepository();
                String roleName = roleRepository.getRoleById(user.getRoleId()).getRoleName();
                user.setRoleName(roleName);
            }

            rs.close();
            ps.close();
            super.close();
            return user;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<User> getUserList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<User> userList = new ArrayList<>();
            while (rs.next()) {
                RoleRepository roleRepository = new RoleRepository();
                String roleName = roleRepository.getRoleById(rs.getObject(2, UUID.class)).getRoleName();
                userList.add(new User(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class),
                        rs.getString(3),
                        rs.getString(5),
                        rs.getInt(4),
                        roleName
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return userList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
