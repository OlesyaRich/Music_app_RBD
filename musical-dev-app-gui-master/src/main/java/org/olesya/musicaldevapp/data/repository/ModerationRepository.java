package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Moderation;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModerationRepository extends BaseTable {
    public ModerationRepository() throws SQLException {
    }

    public void saveModeration(@NonNull Moderation moderation) throws CommonException {
        if (moderation.getProjectId() == null || moderation.getTrackId() == null)
            throw new CommonException("При сохранении модерации все поля должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO moderation VALUES (?,?);");
            ps.setObject(1, moderation.getTrackId());
            ps.setObject(2, moderation.getProjectId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateModeration(@NonNull Moderation oldModeration, @NonNull Moderation updatedModeration) throws CommonException {
        if (updatedModeration.getProjectId() == null || updatedModeration.getTrackId() == null)
            throw new CommonException("При обновлении модерации все поля должны быть заполнены");
        if (oldModeration.getProjectId() == null || oldModeration.getTrackId() == null)
            throw new CommonException("При обновлении модерации все старые поля (идентификаторы поиска) должны быть заполнены");
        try {
            PreparedStatement ps = super.prepareStatement("UPDATE moderation SET track_id=?,project_id=? WHERE track_id=? AND project_id=?;");
            ps.setObject(1, updatedModeration.getTrackId());
            ps.setObject(2, updatedModeration.getProjectId());
            ps.setObject(3, oldModeration.getProjectId());
            ps.setObject(4, oldModeration.getProjectId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteModeration(@NonNull UUID trackId, @NonNull UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM moderation WHERE track_id=? AND project_id=?;"
            );
            ps.setObject(1, trackId);
            ps.setObject(2, projectId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Moderation> getModerationListByIds(UUID trackId, UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = null;

            if (trackId != null && projectId != null) {
                ps = super.prepareStatement(
                        "SELECT m.track_id,m.project_id FROM moderation m WHERE m.track_id=? AND m.project_id=?;"
                );
                ps.setObject(1, trackId);
                ps.setObject(2, projectId);
            } else if (trackId != null) {
                ps = super.prepareStatement(
                        "SELECT m.track_id,m.project_id FROM moderation m WHERE m.track_id=?;"
                );
                ps.setObject(1, trackId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT m.track_id,m.project_id FROM moderation m WHERE m.project_id=?;"
                );
                ps.setObject(1, projectId);
            }

            return ps == null ? null : getModerationList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Moderation> getAllModerations() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT m.track_id,m.project_id FROM moderation m;"
            );
            return getModerationList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Moderation getModeration(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Moderation moderation = null;
            if (rs.next()) {
                moderation = new Moderation(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class)
                );
                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                moderation.setProjectName(projectName);
            }

            rs.close();
            ps.close();
            super.close();
            return moderation;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Moderation> getModerationList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Moderation> moderationList = new ArrayList<>();
            while (rs.next()) {
                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                moderationList.add(new Moderation(
                        rs.getObject(1, UUID.class),
                        rs.getObject(2, UUID.class),
                        projectName
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return moderationList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}