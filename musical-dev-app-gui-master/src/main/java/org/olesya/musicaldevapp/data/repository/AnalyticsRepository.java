package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Analytics;
import org.olesya.musicaldevapp.utils.BaseTable;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AnalyticsRepository extends BaseTable {
    public AnalyticsRepository() throws SQLException {
    }

    public UUID saveAnalytics(@NonNull Analytics analytics) throws CommonException {
        if (analytics.getProjectId() == null || analytics.getRequirementTypeId() == null || analytics.getRequirementContent() == null
                || analytics.getCreateDate() == null || analytics.getLastChangeDate() == null || analytics.getFileExtension() == null || analytics.getFileExtension().isEmpty())
            throw new CommonException("При сохранении аналитики все поля должны быть заполнены");
        try {
            FileUtils.checkIsFileNull(analytics.getRequirementContent());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в аналитике, не существует");
        }
        UUID requirementId = UUID.randomUUID();
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO analytics VALUES (?, ?, ?, ?, ?, ?, ?);");
            ps.setObject(1, requirementId);
            ps.setObject(2, analytics.getProjectId());
            ps.setObject(3, analytics.getRequirementTypeId());
            ps.setBinaryStream(
                    4,
                    new FileInputStream(analytics.getRequirementContent()),
                    analytics.getRequirementContent().length()
            );
            ps.setObject(5, analytics.getCreateDate());
            ps.setObject(6, analytics.getLastChangeDate());
            ps.setString(7, analytics.getFileExtension());
            super.executeSqlStatementUpdate(ps);
            return requirementId;
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateAnalytics(@NonNull Analytics analytics, UUID oldProjectId) throws CommonException {
        if (analytics.getRequirementId() == null || analytics.getProjectId() == null || analytics.getRequirementTypeId() == null || analytics.getRequirementContent() == null
                || analytics.getCreateDate() == null || analytics.getLastChangeDate() == null || analytics.getFileExtension() == null || analytics.getFileExtension().isEmpty())
            throw new CommonException("При обновления аналитики все поля должны быть заполнены");
        try {
            FileUtils.checkIsFileNull(analytics.getRequirementContent());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в аналитике, не существует");
        }
        try {
            PreparedStatement ps = null;
            int parameterIndexOffset = 0;
            if (oldProjectId == null) {
                ps = super.prepareStatement("UPDATE analytics SET requirement_type_id=?,requirement_content=?," +
                        "create_date=?,last_change_date=?,file_extension=? WHERE requirement_id=? AND project_id=?;");
                ps.setObject(7, analytics.getProjectId());
            } else {
                ps = super.prepareStatement("UPDATE analytics SET project_id=?,requirement_type_id=?,requirement_content=?," +
                        "create_date=?,last_change_date=?,file_extension=? WHERE requirement_id=? AND project_id=?;");
                ps.setObject(1, analytics.getProjectId());
                ps.setObject(8, oldProjectId);
                parameterIndexOffset = 1;
            }
            ps.setObject(1 + parameterIndexOffset, analytics.getRequirementTypeId());
            ps.setBinaryStream(
                    2 + parameterIndexOffset,
                    new FileInputStream(analytics.getRequirementContent()),
                    analytics.getRequirementContent().length()
            );
            ps.setObject(3 + parameterIndexOffset, analytics.getCreateDate());
            ps.setObject(4 + parameterIndexOffset, analytics.getLastChangeDate());
            ps.setString(5 + parameterIndexOffset, analytics.getFileExtension());
            ps.setObject(6 + parameterIndexOffset, analytics.getRequirementId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteAnalytics(@NonNull UUID requirementId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM analytics WHERE requirement_id=?;"
            );
            ps.setObject(1, requirementId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Analytics> getAllAnalytics() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                            "a.create_date,a.last_change_date,a.file_extension FROM analytics a;"
            );
            return getAnalyticsList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Analytics> getAnalyticsByRequirementIdAndProjectId(UUID requirementId, UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = null;
            if (requirementId != null && projectId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.requirement_id=? AND a.project_id=?;"
                );
                ps.setObject(1, requirementId);
                ps.setObject(2, projectId);
            } else if (requirementId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.requirement_id=?;"
                );
                ps.setObject(1, requirementId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.project_id=?;"
                );
                ps.setObject(1, projectId);
            }

            return ps == null ? null : getAnalyticsList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Analytics> getAnalyticsByProjectIdAndRequirementTypeId(UUID projectId, UUID requirementTypeId) throws CommonException {
        try {
            PreparedStatement ps = null;

            if (projectId != null && requirementTypeId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.project_id=? AND a.requirement_type_id=?;"
                );
                ps.setObject(1, projectId);
                ps.setObject(2, requirementTypeId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.project_id=?;"
                );
                ps.setObject(1, projectId);
            } else if (requirementTypeId != null) {
                ps = super.prepareStatement(
                        "SELECT a.requirement_id,a.project_id,a.requirement_type_id,a.requirement_content," +
                                "a.create_date,a.last_change_date,a.file_extension FROM analytics a WHERE a.requirement_type_id=?;"
                );
                ps.setObject(1, requirementTypeId);
            }

            return ps == null ? null : getAnalyticsList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public Integer getProjectRequirementsCount(@NonNull UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement("SELECT get_requirements_count(?);");
            ps.setObject(1, projectId);
            ResultSet rs = super.executeSqlStatementRead(ps);
            Integer projectRequirementsAmount = null;
            if (rs.next()) {
                projectRequirementsAmount = rs.getInt(1);
            }
            rs.close();
            ps.close();
            super.close();
            return projectRequirementsAmount;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private Analytics getAnalytics(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Analytics analytics = null;
            if (rs.next()) {
                UUID requirementId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(7);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(4), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для аналитики id: '" +
                                        requirementId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                analytics = new Analytics(
                        requirementId,
                        rs.getObject(2, UUID.class),
                        rs.getObject(3, UUID.class),
                        tempFile,
                        rs.getDate(5).toLocalDate(),
                        rs.getDate(6).toLocalDate(),
                        fileExtension
                );

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                analytics.setProjectName(projectName);
                RequirementTypeRepository requirementTypeRepository = new RequirementTypeRepository();
                String requirementTypeName = requirementTypeRepository.getRequirementTypeById(rs.getObject(3, UUID.class)).getRequirementTypeName();
                analytics.setRequirementTypeName(requirementTypeName);
            }

            rs.close();
            ps.close();
            super.close();
            return analytics;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<Analytics> getAnalyticsList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Analytics> analyticsList = new ArrayList<>();
            while (rs.next()) {
                UUID requirementId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(7);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(4), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для аналитики id: '" +
                                        requirementId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                RequirementTypeRepository requirementTypeRepository = new RequirementTypeRepository();
                String requirementTypeName = requirementTypeRepository.getRequirementTypeById(rs.getObject(3, UUID.class)).getRequirementTypeName();

                analyticsList.add(new Analytics(
                        requirementId,
                        rs.getObject(2, UUID.class),
                        rs.getObject(3, UUID.class),
                        tempFile,
                        rs.getDate(5).toLocalDate(),
                        rs.getDate(6).toLocalDate(),
                        fileExtension,
                        projectName,
                        requirementTypeName
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return analyticsList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}