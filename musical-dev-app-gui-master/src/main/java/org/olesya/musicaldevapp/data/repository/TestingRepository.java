package org.olesya.musicaldevapp.data.repository;

import lombok.NonNull;
import org.olesya.musicaldevapp.data.entity.Testing;
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

public class TestingRepository extends BaseTable {
    public TestingRepository() throws SQLException {
    }

    public UUID saveDevelopment(@NonNull Testing testing) throws CommonException {
        if (testing.getProjectId() == null || testing.getTestContent() == null || testing.getCreateDate() == null || testing.getLastChangeDate() == null
                || testing.getFileExtension() == null || testing.getFileExtension().isEmpty()) {
            throw new CommonException("При сохранении тестирования все поля должны быть заполнены");
        }
        try {
            FileUtils.checkIsFileNull(testing.getTestContent());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в тестировании, не существует");
        }
        UUID testId = UUID.randomUUID();
        try {
            PreparedStatement ps = super.prepareStatement("INSERT INTO testing VALUES (?,?,?,?,?,?);");
            ps.setObject(1, testId);
            ps.setObject(2, testing.getProjectId());
            ps.setBinaryStream(
                    3,
                    new FileInputStream(testing.getTestContent()),
                    testing.getTestContent().length()
            );
            ps.setObject(4, testing.getCreateDate());
            ps.setObject(5, testing.getLastChangeDate());
            ps.setString(6, testing.getFileExtension());
            super.executeSqlStatementUpdate(ps);
            return testId;
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void updateTesting(@NonNull Testing testing, UUID oldProjectId) throws CommonException {
        if (testing.getTestId() == null || testing.getProjectId() == null || testing.getTestContent() == null || testing.getCreateDate() == null || testing.getLastChangeDate() == null
                || testing.getFileExtension() == null || testing.getFileExtension().isEmpty()) {
            throw new CommonException("При обновлении тестирования все поля должны быть заполнены");
        }
        try {
            FileUtils.checkIsFileNull(testing.getTestContent());
        } catch (CommonException e) {
            throw new CommonException("Файл, указанный в тестировании, не существует");
        }
        try {
            PreparedStatement ps = null;
            int parameterIndexOffset = 0;
            if (oldProjectId == null) {
                ps = super.prepareStatement("UPDATE testing SET test_content=?,create_date=?," +
                        "last_change_date=?,file_extension=? WHERE test_id=? AND project_id=?;");
                ps.setObject(6, testing.getProjectId());
            } else {
                ps = super.prepareStatement("UPDATE testing SET project_id=?,test_content=?,create_date=?," +
                        "last_change_date=?,file_extension=? WHERE test_id=? AND project_id=?;");
                ps.setObject(1, testing.getProjectId());
                ps.setObject(7, oldProjectId);
                parameterIndexOffset = 1;
            }
            ps.setBinaryStream(
                    1 + parameterIndexOffset,
                    new FileInputStream(testing.getTestContent()),
                    testing.getTestContent().length()
            );
            ps.setObject(2 + parameterIndexOffset, testing.getCreateDate());
            ps.setObject(3 + parameterIndexOffset, testing.getLastChangeDate());
            ps.setString(4 + parameterIndexOffset, testing.getFileExtension());
            ps.setObject(5 + parameterIndexOffset, testing.getTestId());

            super.executeSqlStatementUpdate(ps);
        } catch (SQLException | FileNotFoundException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public void deleteTesting(@NonNull UUID testId) throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "DELETE FROM testing WHERE test_id=?;"
            );
            ps.setObject(1, testId);
            super.executeSqlStatementUpdate(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Testing> getTestingsByTestIdAndProjectId(UUID testId, UUID projectId) throws CommonException {
        try {
            PreparedStatement ps = null;
            if (testId != null && projectId != null) {
                ps = super.prepareStatement(
                        "SELECT t.test_id,t.project_id,t.test_content,t.create_date,t.last_change_date,t.file_extension " +
                                "FROM testing t WHERE t.test_id=? AND t.project_id=?;"
                );
                ps.setObject(1, testId);
                ps.setObject(2, projectId);
            } else if (testId != null) {
                ps = super.prepareStatement(
                        "SELECT t.test_id,t.project_id,t.test_content,t.create_date,t.last_change_date,t.file_extension " +
                                "FROM testing t WHERE t.test_id=?;"
                );
                ps.setObject(1, testId);
            } else if (projectId != null) {
                ps = super.prepareStatement(
                        "SELECT t.test_id,t.project_id,t.test_content,t.create_date,t.last_change_date,t.file_extension " +
                                "FROM testing t WHERE t.project_id=?;"
                );
                ps.setObject(1, projectId);
            }

            return ps == null ? null : getTestingList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    public List<Testing> getAllTestings() throws CommonException {
        try {
            PreparedStatement ps = super.prepareStatement(
                    "SELECT t.test_id,t.project_id,t.test_content,t.create_date,t.last_change_date,t.file_extension " +
                            "FROM testing t;"
            );
            return getTestingList(ps);
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private Testing getTesting(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            Testing testing = null;
            if (rs.next()) {
                UUID testId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(6);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(3), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для тестирования с id: '" +
                                        testId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                testing = new Testing(
                        testId,
                        rs.getObject(2, UUID.class),
                        tempFile,
                        rs.getDate(4).toLocalDate(),
                        rs.getDate(5).toLocalDate(),
                        fileExtension
                );

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();
                testing.setProjectName(projectName);
            }

            rs.close();
            ps.close();
            super.close();
            return testing;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }

    private List<Testing> getTestingList(@NonNull PreparedStatement ps) throws CommonException {
        try {
            ResultSet rs = super.executeSqlStatementRead(ps);
            List<Testing> testingList = new ArrayList<>();
            while (rs.next()) {
                UUID testId = rs.getObject(1, UUID.class);
                String fileExtension = rs.getString(6);
                File tempFile = null;
                if (fileExtension != null && !fileExtension.isEmpty())
                    try {
                        tempFile = FileUtils.getFileFromBinaryStream(rs.getBinaryStream(3), fileExtension);
                    } catch (IOException e) {
                        throw new CommonException(
                                "Ошибка при создании временного файла для тестирования с id: '" +
                                        testId + "'. Текст ошибки: '" + e.getMessage() + "'"
                        );
                    }

                ProjectRepository projectRepository = new ProjectRepository();
                String projectName = projectRepository.getProjectById(rs.getObject(2, UUID.class)).getProjectName();

                testingList.add(new Testing(
                        testId,
                        rs.getObject(2, UUID.class),
                        tempFile,
                        rs.getDate(4).toLocalDate(),
                        rs.getDate(5).toLocalDate(),
                        fileExtension,
                        projectName
                ));
            }

            rs.close();
            ps.close();
            super.close();
            return testingList;
        } catch (SQLException e) {
            throw new CommonException(e.getMessage());
        }
    }
}
