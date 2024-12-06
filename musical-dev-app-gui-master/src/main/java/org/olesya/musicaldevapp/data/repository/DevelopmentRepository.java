package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Development;
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

public class DevelopmentRepository extends BaseTable {
    public DevelopmentRepository() throws SQLException {
    }

    public UUID saveDevelopment(@NonNull Development development) throws CommonException {
        if (development.getProjectId() == null || development.getCodeFile() == null || development.getVersion() == null || development.getVersion().isEmpty()
                || development.getCreateDate() == null || development.getLastChangeDate() == null || development.getFileExtension() == null || development.getFileExtension().isEmpty()) {
            throw new CommonException("При сохранении разработки все поля должны быть заполнены");
        }
        try {
            FileUtils.checkIsFileNull(development.getCodeFile());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в разработке, не существует");
        }
        UUID fileId = UUID.randomUUID();
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO development VALUES (?,?,?,?,?,?,?);");
            ps.setObject(1, fileId);
            ps.setObject(2, development.getProjectId());
            ps.setBinaryStream(
                    3,
                    new FileInputStream(development.getCodeFile()),
                    development.getCodeFile().length()
            );
            ps.setString(4, development.getVersion());
            ps.setObject(5, development.getCreateDate());
            ps.setObject(6, development.getLastChangeDate());
            ps.setString(7, development.getFileExtension());
            super.executeSqlStatementUpdate(ps);
            return fileId;
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateDevelopment(@NonNull Development development, UUID oldProjectId) throws CommonException {
        if (development.getFileId() == null || development.getProjectId() == null || development.getCodeFile() == null || development.getVersion() == null || development.getVersion().isEmpty()
                || development.getCreateDate() == null
                //|| development.getLastChangeDate() == null
                || development.getFileExtension() == null || development.getFileExtension().isEmpty()) {
            throw new CommonException("При обновлении разработки все поля должны быть заполнены");
        }
        try {
            FileUtils.checkIsFileNull(development.getCodeFile());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в разработке, не существует");
        }
        try {
            PreparedStatement ps = null;
            int parameterIndexOffset = 0;
            if (oldProjectId == null) {
                ps = super.prepareStatement("UPDATE development SET code_file=?," +
                        "version=?,create_date=?,last_change_date=?,file_extension=? WHERE file_id=? AND project_id=?;");
                ps.setObject(7, development.getProjectId());
            } else {
                ps = super.prepareStatement("UPDATE development SET project_id=?,code_file=?," +
                        "version=?,create_date=?,last_change_date=?,file_extension=? WHERE file_id=? AND project_id=?;");
                ps.setObject(1, development.getProjectId());
                ps.setObject(8, oldProjectId);
                parameterIndexOffset = 1;
            }
            ps.setBinaryStream(
                    1 + parameterIndexOffset,
                    new FileInputStream(development.getCodeFile()),
                    development.getCodeFile().length()
            );
            ps.setString(2 + parameterIndexOffset, development.getVersion());
            ps.setObject(3 + parameterIndexOffset, development.getCreateDate());
            ps.setObject(4 + parameterIndexOffset, development.getLastChangeDate());
            ps.setString(5 + parameterIndexOffset, development.getFileExtension());
            ps.setObject(6 + parameterIndexOffset, development.getFileId());
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteDevelopment(@NonNull UUID developmentId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM development WHERE file_id=?;"
            );
            ps.setObject(1, developmentId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Development> getDevelopmentsByFileIdAndProjectId(UUID fileId, UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = null;
            if (fileId != null && projectId != null) {
                ps = super.prepareStatement(
                        "SELECT d.file_id,d.project_id,d.code_file,d.version,d.create_date,d.last_change_date," +
                                "d.file_extension FROM development d WHERE d.file_id=? AND d.project_id=?;"
                );
                ps.setObject(1, fileId);
                ps.setObject(2, projectId);
            } else if (fileId != null) {
                ps = super.prepareStatement(
                        "SELECT d.file_id,d.project_id,d.code_file,d.version,d.create_date,d.last_change_date," +
                                "d.file_extension FROM development d WHERE d.file_id=?;"
                );
                ps.setObject(1, fileId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT d.file_id,d.project_id,d.code_file,d.version,d.create_date,d.last_change_date," +
                                "d.file_extension FROM development d WHERE d.project_id=?;"
                );
                ps.setObject(1, projectId);
            }
            return ps == null ? null : getDevelopmentList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Development> getAllDevelopment() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT d.file_id,d.project_id,d.code_file,d.version,d.create_date,d.last_change_date," +
                            "d.file_extension FROM development d;"
            );
            return getDevelopmentList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private Development getDevelopment(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Development development = null;
            if (rs.next()) {
                UUID fileId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(7);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(3), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для разработки с id: '" +
                                        fileId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                development = new Development(
                        fileId,
                        rs.getObject(2, UUID.class),
                        tempFile,
                        rs.getString(4),
                        rs.getDate(5).toLocalDate(),
                        rs.getDate(6).toLocalDate(),
                        fileExtension
                );

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                development.setProjectName(projectName);
            }

            rs.close();
            ps.close();
            super.close();
            return development;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<Development> getDevelopmentList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Development> developmentList = new ArrayList<>();
            while (rs.next()) {
                UUID fileId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(7);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(3), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для разработки с id: '" +
                                        fileId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();

                developmentList.add(new Development(
                        fileId,
                        rs.getObject(2, UUID.class),
                        tempFile,
                        rs.getString(4),
                        rs.getDate(5).toLocalDate(),
                        rs.getDate(6).toLocalDate(),
                        fileExtension,
                        projectName
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return developmentList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
