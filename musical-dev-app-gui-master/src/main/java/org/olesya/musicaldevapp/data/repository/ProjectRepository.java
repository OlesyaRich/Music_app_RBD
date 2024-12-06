package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Project;
import org.olesya.musicaldevapp.data.entity.ProjectAggregatedInfo;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectRepository extends BaseTable {
    public ProjectRepository() throws SQLException {
    }

    public UUID saveProject(@NonNull Project project) throws CommonException {
        if (project.getProjectName() == null || project.getProjectName().isEmpty() || project.getCreateDate() == null
                || project.getLastChangeDate() == null || project.getVersion() == null || project.getVersion().isEmpty())
            throw new CommonException("При сохранении проекта все поля должны быть заполнены");
        try {
            UUID projectId = UUID.randomUUID();
            PreparedStatement ps = super.prepareStatement("INSERT INTO project VALUES (?,?,?,?,?);");
            ps.setObject(1, projectId);
            ps.setString(2, project.getProjectName());
            ps.setObject(3, project.getCreateDate());
            ps.setObject(4, project.getLastChangeDate());
            ps.setString(5, project.getVersion());
            super.executeSqlStatementUpdate(ps);
            return projectId;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateProject(@NonNull Project project) throws CommonException {
        if (project.getProjectId() == null || project.getProjectName() == null || project.getProjectName().isEmpty() || project.getCreateDate() == null
                || project.getLastChangeDate() == null || project.getVersion() == null || project.getVersion().isEmpty())
            throw new CommonException("При обновлении проекта все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE project SET project_name=?,create_date=?,last_change_date=?,version=? WHERE project_id=?;");
            ps.setString(1, project.getProjectName());
            ps.setObject(2, project.getCreateDate());
            ps.setObject(3, project.getLastChangeDate());
            ps.setString(4, project.getVersion());
            ps.setObject(5, project.getProjectId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteProject(@NonNull UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM project WHERE project_id=?;"
            );
            ps.setObject(1, projectId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Project getProjectById(@NonNull UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT p.project_id,p.project_name,p.create_date,p.last_change_date,p.version " +
                            "FROM project p WHERE p.project_id=?;"
            );
            ps.setObject(1, projectId);
            return getProject(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Project> getAllProjects() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT p.project_id,p.project_name,p.create_date,p.last_change_date,p.version " +
                            "FROM project p;"
            );
            return getProjectList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Project getProjectByName(@NonNull String projectName) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT p.project_id,p.project_name,p.create_date,p.last_change_date,p.version " +
                            "FROM project p WHERE p.project_name=?;"
            );
            ps.setString(1, projectName);
            return getProject(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public ProjectAggregatedInfo getProjectAggregatedInfo(@NonNull UUID projectId) throws CommonException {
        try {
            ProjectAggregatedInfo projectAggregatedInfo = null;
            PreparedStatement ps = super.prepareStatement(
                    "SELECT projectname,createdate,lastchangedate FROM get_project_info(?);"
            );
            ps.setObject(1, projectId);
            ResultSet rs = super.executeSqlStatementRead(ps);
            if (rs.next()) {
                projectAggregatedInfo = new ProjectAggregatedInfo(
                        projectId,
                        rs.getString(1),
                        rs.getDate(2).toLocalDate(),
                        rs.getDate(3).toLocalDate()
                );
            }
            rs.close();
            ps.close();
            super.close();
            return projectAggregatedInfo;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private Project getProject(PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Project project = null;
            while (rs.next()) {
                project = new Project(
                        rs.getObject(1, UUID.class),
                        rs.getString(2),
                        rs.getDate(3).toLocalDate(),
                        rs.getDate(4).toLocalDate(),
                        rs.getString(5)
                );
            }
            rs.close();
            ps.close();
            super.close();
            return project;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<Project> getProjectList(PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Project> projects = new ArrayList<>();
            while (rs.next()) {
                projects.add(new Project(
                        rs.getObject(1, UUID.class),
                        rs.getString(2),
                        rs.getDate(3).toLocalDate(),
                        rs.getDate(4).toLocalDate(),
                        rs.getString(5)
                ));
            }
            rs.close();
            ps.close();
            super.close();
            return projects;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
