package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.ProjectUser;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectUserRepository extends BaseTable {
    public ProjectUserRepository() throws SQLException {
    }

    public UUID saveProjectUser(@NonNull ProjectUser projectUser) throws CommonException {
        if (projectUser.getProjectId() == null || projectUser.getUserId() == null)
            throw new CommonException("При сохранении связи пользователя с проектом все поля должны быть заполнены");
        try {
            //PreparedStatement ps = super.prepareStatement("INSERT INTO project_users VALUES (?,?,?);");
            PreparedStatement ps = super.prepareStatement("CALL add_user_to_project(?,?,?);");
            UUID projectUserId = UUID.randomUUID();
            ps.setObject(1, projectUserId);
            ps.setObject(2, projectUser.getProjectId());
            ps.setObject(3, projectUser.getUserId());
            super.executeSqlStatementUpdate(ps);
            return projectUserId;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateProjectUser(@NonNull ProjectUser projectUser) throws CommonException {
        if (projectUser.getProjectUserId() == null || projectUser.getProjectId() == null || projectUser.getUserId() == null)
            throw new CommonException("При обновлении связи пользователя с проектом все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE project_users SET project_id=?,user_id=? WHERE project_users_id=?;");
            ps.setObject(1, projectUser.getProjectId());
            ps.setObject(2, projectUser.getUserId());
            ps.setObject(3, projectUser.getProjectUserId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteProjectUser(@NonNull UUID projectUserId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM project_users WHERE project_users_id=?;"
            );
            ps.setObject(1, projectUserId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public ProjectUser getProjectUserById(@NonNull UUID projectUserId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT pu.project_users_id,pu.project_id,pu.user_id FROM project_users pu WHERE pu.project_users_id=?;"
            );
            ps.setObject(1, projectUserId);
            return getProjectUser(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<ProjectUser> getAllProjectUsers() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT pu.project_users_id,pu.project_id,pu.user_id FROM project_users pu;"
            );
            return getProjectUserList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<ProjectUser> getProjectUsersByProjectIdAndUserId(UUID projectId, UUID userId) throws CommonException {
        try {
            PreparedStatement ps = null;
            if (projectId != null && userId != null) {
                ps = super.prepareStatement(
                        "SELECT pu.project_users_id,pu.project_id,pu.user_id FROM project_users pu WHERE pu.project_id=? AND pu.user_id=?;"
                );
                ps.setObject(1, projectId);
                ps.setObject(2, userId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT pu.project_users_id,pu.project_id,pu.user_id FROM project_users pu WHERE pu.project_id=?;"
                );
                ps.setObject(1, projectId);
            } else if (userId != null) {
                ps = super.prepareStatement(
                        "SELECT pu.project_users_id,pu.project_id,pu.user_id FROM project_users pu WHERE pu.user_id=?;"
                );
                ps.setObject(1, userId);
            }

            return ps == null ? null : getProjectUserList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private ProjectUser getProjectUser(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            ProjectUser projectUser = null;
            if (rs.next()) {
                projectUser = new ProjectUser(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class),
                        rs.getObject(3, UUID.class)
                );

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                projectUser.setProjectName(projectName);

                UserRepository userRepository = new UserRepository();
                String userName = userRepository.getUserById(rs.getObject(2, UUID.class)).getUserName();
                projectUser.setUserName(userName);
            }
            rs.close();
            ps.close();
            super.close();
            return projectUser;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<ProjectUser> getProjectUserList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<ProjectUser> projectUserList = new ArrayList<>();
            while (rs.next()) {
                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();

                UserRepository userRepository = new UserRepository();
                String userName = userRepository.getUserById(rs.getObject(3, UUID.class)).getUserName();
                projectUserList.add(new ProjectUser(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class),
                        rs.getObject(3, UUID.class),
                        projectName,
                        userName
                ));
            }
            rs.close();
            ps.close();
            super.close();
            return projectUserList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
