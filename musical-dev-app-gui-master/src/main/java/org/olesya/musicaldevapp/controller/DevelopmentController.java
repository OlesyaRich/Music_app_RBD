package org.olesya.musicaldevapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;
import org.olesya.musicaldevapp.data.entity.*;
import org.olesya.musicaldevapp.data.repository.*;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.ControllerUtils;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class DevelopmentController {
    @FXML
    private TableView<Development> developmentTable;

    @FXML
    private TableColumn<Development, UUID> fileIdDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, String> projectNameDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, File> codeFileDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, String> versionDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, LocalDate> createDateDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, LocalDate> lastChangeDateDevelopmentTableColumn;

    @FXML
    private TableColumn<Development, String> fileExtensionDevelopmentTableColumn;

    @FXML
    private Button developmentFilterButton;

    @FXML
    private TextField fileIdDevelopmentTextField;

    @FXML
    private TextField projectNameDevelopmentTextField;

    @FXML
    private TextField fileIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private TextField fileInputField;

    @FXML
    private TextField versionInputField;

    @FXML
    private TextField createDateInputField;

    @FXML
    private TextField lastChangeDateInputField;

    @FXML
    private TextField fileExtensionInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    private DevelopmentRepository developmentRepository;
    private ProjectRepository projectRepository;
    private ProjectUserRepository projectUserRepository;
    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Development selectedDevelopment = null;

    public void initialize() throws SQLException, CommonException {
        developmentRepository = new DevelopmentRepository();
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedDevelopment();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        fileIdDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileId"));
        projectNameDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        codeFileDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("codeFile"));
        versionDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("version"));
        createDateDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        lastChangeDateDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("lastChangeDate"));
        fileExtensionDevelopmentTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileExtension"));
    }

    private void baseFillTable() throws CommonException {
        developmentTable.refresh();
        ObservableList<Development> developmentObservableList = FXCollections.observableArrayList();
        List<Development> developments = developmentRepository.getAllDevelopment();
        developmentObservableList.addAll(developments);
        developmentTable.setItems(developmentObservableList);
        developmentTable.refresh();
    }

    private void setOnActionFilterButton() {
        developmentFilterButton.setOnAction(event -> {
            try {
                List<Development> developments = developmentRepository.getAllDevelopment();
                ObservableList<Development> developmentObservableList = FXCollections.observableArrayList();
                if (!fileIdDevelopmentTextField.getText().isEmpty() || !projectNameDevelopmentTextField.getText().isEmpty()) {
                    developments = developmentRepository.getDevelopmentsByFileIdAndProjectId(
                            getFileId(),
                            getProjectId()
                    );
                }
                developmentObservableList.addAll(developments == null ? List.of() : developments);
                developmentTable.setItems(developmentObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private UUID getProjectId() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameDevelopmentTextField.getText());
            //if (project == null) throw new CommonException("Проект с указанным наименованием не существует");
            return project == null ? null : project.getProjectId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private UUID getFileId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(fileIdDevelopmentTextField, "ID файла");
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        developmentTable.getSelectionModel().clearSelection();
        selectedDevelopment = null;
    }

    private void clearFields() {
        fileIdInputField.setText("");
        projectNameInputField.setText("");
        fileInputField.setText("");
        versionInputField.setText("");
        createDateInputField.setText("");
        lastChangeDateInputField.setText("");
        fileExtensionInputField.setText("");
    }

    private void autoFillFields() {
        fileIdInputField.setText(selectedDevelopment.getFileId().toString());
        projectNameInputField.setText(selectedDevelopment.getProjectName());
        versionInputField.setText(selectedDevelopment.getVersion());
        fileInputField.setText(selectedDevelopment.getCodeFile().toString());
        createDateInputField.setText(selectedDevelopment.getCreateDate().toString());
        lastChangeDateInputField.setText(selectedDevelopment.getLastChangeDate().toString());
        fileExtensionInputField.setText(selectedDevelopment.getFileExtension());
    }

    private void setOnChangedSelectedDevelopment() {
        developmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedDevelopment = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedDevelopment = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getFileIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(fileIdInputField, "ID файла");
    }

    private UUID getProjectIdInputField() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameInputField.getText());
            if (project == null) throw new CommonException("Проект с указанным наименованием не существует");
            return project.getProjectId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private String getVersionInputField() throws CommonException {
        return versionInputField.getText();
    }

    private String getFileExtensionInputField() throws CommonException {
        return fileExtensionInputField.getText();
    }

    private File getContentInputField() throws CommonException {
        String fileExtension = getFileExtensionInputField();
        if (fileExtension == null || fileExtension.isEmpty())
            throw new CommonException("Поле 'Расширение файла' не может быть пустым");
        return ControllerUtils.getFileFromTextField(fileInputField, fileExtensionInputField.getText(), "Файл");
    }

    private LocalDate getCreateDateInputField() throws CommonException {
        return ControllerUtils.getLocalDateFromTextField(createDateInputField, "Дата создания");
    }

    private LocalDate getLastChangeDateInputField() throws CommonException {
        return ControllerUtils.getLocalDateFromTextField(lastChangeDateInputField, "Дата последнего редактирования");
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                UUID oldProjectId = selectedDevelopment.getProjectId();
                selectedDevelopment.setProjectId(getProjectIdInputField());
                selectedDevelopment.setCodeFile(getContentInputField());
                selectedDevelopment.setVersion(getVersionInputField());
                selectedDevelopment.setCreateDate(getCreateDateInputField());
                selectedDevelopment.setLastChangeDate(LocalDate.now());
                selectedDevelopment.setFileExtension(getFileExtensionInputField());
                developmentRepository.updateDevelopment(
                        selectedDevelopment,
                        oldProjectId
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedDevelopment));
                ControllerUtils.showSuccessfulUpdatingDialog("Запись изменена успешно");
                UUID projectId = getProjectIdInputField();
                List<ProjectUser> existingRows = projectUserRepository.getProjectUsersByProjectIdAndUserId(projectId, currentUser.getUserId());
                baseFillTable();
                clearFields();
                clearSelection();
                if (existingRows != null && !existingRows.isEmpty())
                    return;
                projectUserRepository.saveProjectUser(
                        new ProjectUser(
                                null,
                                projectId,
                                currentUser.getUserId()
                        )
                );
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setAddButtonOnAction() {
        addButton.setOnAction(event -> {
            try {
                if (!checkAddAvailable())
                    throw new CommonException(
                            "Для сохранения записи все поля должны быть заполнены"
                    );
                Development development = new Development();
                development.setProjectId(getProjectIdInputField());
                development.setCodeFile(getContentInputField());
                development.setVersion(getVersionInputField());
                development.setCreateDate(LocalDate.now());
                //development.setLastChangeDate(LocalDate.now());
                development.setFileExtension(getFileExtensionInputField());
                developmentRepository.saveDevelopment(
                        development
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", development));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
                UUID projectId = development.getProjectId();
                List<ProjectUser> existingRows = projectUserRepository.getProjectUsersByProjectIdAndUserId(projectId, currentUser.getUserId());
                if (existingRows != null && !existingRows.isEmpty())
                    return;
                projectUserRepository.saveProjectUser(
                        new ProjectUser(
                                null,
                                projectId,
                                currentUser.getUserId()
                        )
                );
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkSaveAvailable() throws CommonException {
        return !fileIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty()
                && !versionInputField.getText().isEmpty()
                && !fileInputField.getText().isEmpty()
                && !createDateInputField.getText().isEmpty()
                && !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !projectNameInputField.getText().isEmpty()
                && !versionInputField.getText().isEmpty()
                && !fileInputField.getText().isEmpty()
                //&& !createDateInputField.getText().isEmpty()
                //&& !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedDevelopment != null) {
                    developmentRepository.deleteDevelopment(
                            selectedDevelopment.getFileId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedDevelopment));
                    ControllerUtils.showSuccessfulDeletionDialog("Запись удалена успешно");
                    baseFillTable();
                    clearFields();
                    clearSelection();
                } else throw new CommonException(
                        "Для удаления записи выберите соответствующую строку таблицы"
                );
            } catch (CommonException e) {
                if (e.getMessage().contains("нарушает ограничение внешнего ключа"))
                    throw new RuntimeException(new CommonException("Удаление невозможно - есть связанные записи!"));
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkIfTheCurrentUserIsAdmin() throws CommonException {
        Role userRole = roleRepository.getRoleById(
                currentUser.getRoleId()
        );
        return userRole.getRoleName().equals("ADMIN");
    }
}
