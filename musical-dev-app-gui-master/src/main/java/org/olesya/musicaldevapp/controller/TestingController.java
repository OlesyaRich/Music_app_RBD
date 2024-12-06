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

public class TestingController {
    @FXML
    private TableView<Testing> testingTable;

    @FXML
    private TableColumn<Testing, UUID> testIdTestingTable;

    @FXML
    private TableColumn<Testing, String> projectNameTestingTable;

    @FXML
    private TableColumn<Testing, File> testContentTestingTable;

    @FXML
    private TableColumn<Testing, LocalDate> createDateTestingTable;

    @FXML
    private TableColumn<Testing, LocalDate> lastChangeDateTestingTable;

    @FXML
    private TableColumn<Testing, String> fileExtensionTestingTable;

    @FXML
    private Button testingFilterButton;

    @FXML
    private TextField testingIdTestingTextField;

    @FXML
    private TextField projectNameTestingTextField;

    @FXML
    private TextField testIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private TextField contentInputField;

    @FXML
    private TextField createDateInputField;

    @FXML
    private TextField lastChangeDateInputField;

    @FXML
    private TextField fileExtensionInputField;

    @FXML
    private TextField roleNameInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    private ProjectRepository projectRepository;
    private ProjectUserRepository projectUserRepository;
    private RoleRepository roleRepository;
    private TestingRepository testingRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Testing selectedTesting = null;

    public void initialize() throws SQLException, CommonException {
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        roleRepository = new RoleRepository();
        testingRepository = new TestingRepository();
        setCellValueFactories();
        baseFillTable();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedTesting();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        testIdTestingTable.setCellValueFactory(new PropertyValueFactory<>("testId"));
        projectNameTestingTable.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        testContentTestingTable.setCellValueFactory(new PropertyValueFactory<>("testContent"));
        createDateTestingTable.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        lastChangeDateTestingTable.setCellValueFactory(new PropertyValueFactory<>("lastChangeDate"));
        fileExtensionTestingTable.setCellValueFactory(new PropertyValueFactory<>("fileExtension"));
    }

    private void baseFillTable() throws CommonException {
        testingTable.refresh();
        ObservableList<Testing> testingObservableList = FXCollections.observableArrayList();
        List<Testing> testings = testingRepository.getAllTestings();
        testingObservableList.addAll(testings);
        testingTable.setItems(testingObservableList);
        testingTable.refresh();
    }

    private void setOnActionFilterButton() {
        testingFilterButton.setOnAction(event -> {
            try {
                List<Testing> testings = testingRepository.getAllTestings();
                ObservableList<Testing> testingObservableList = FXCollections.observableArrayList();
                if (!testingIdTestingTextField.getText().isEmpty() || !projectNameTestingTextField.getText().isEmpty()) {
                    testings = testingRepository.getTestingsByTestIdAndProjectId(
                            getTestingId(),
                            getProjectId()
                    );
                }
                testingObservableList.addAll(testings == null ? List.of() : testings);
                testingTable.setItems(testingObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private UUID getTestingId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(testingIdTestingTextField, "ID теста");
    }

    private UUID getProjectId() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameTestingTextField.getText());
            //if (project == null) throw new CommonException("Проект с указанным наименованием не существует");
            return project == null ? null : project.getProjectId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        testingTable.getSelectionModel().clearSelection();
        selectedTesting = null;
    }

    private void clearFields() {
        testIdInputField.setText("");
        projectNameInputField.setText("");
        contentInputField.setText("");
        createDateInputField.setText("");
        lastChangeDateInputField.setText("");
        fileExtensionInputField.setText("");
    }

    private void autoFillFields() {
        testIdInputField.setText(selectedTesting.getProjectId().toString());
        projectNameInputField.setText(selectedTesting.getProjectName());
        contentInputField.setText(selectedTesting.getTestContent().toString());
        createDateInputField.setText(selectedTesting.getCreateDate().toString());
        lastChangeDateInputField.setText(selectedTesting.getLastChangeDate().toString());
        fileExtensionInputField.setText(selectedTesting.getFileExtension());
    }

    private void setOnChangedSelectedTesting() {
        testingTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedTesting = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedTesting = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getTestIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(testIdInputField, "ID теста");
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

    private String getFileExtensionInputField() throws CommonException {
        return fileExtensionInputField.getText();
    }

    private File getContentInputField() throws CommonException {
        String fileExtension = getFileExtensionInputField();
        if (fileExtension == null || fileExtension.isEmpty())
            throw new CommonException("Поле 'Расширение файла' не может быть пустым");
        return ControllerUtils.getFileFromTextField(contentInputField, fileExtensionInputField.getText(), "Файл");
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
                UUID oldProjectId = selectedTesting.getProjectId();
                selectedTesting.setProjectId(getProjectIdInputField());
                selectedTesting.setTestContent(getContentInputField());
                selectedTesting.setCreateDate(getCreateDateInputField());
                selectedTesting.setLastChangeDate(LocalDate.now());
                selectedTesting.setFileExtension(getFileExtensionInputField());
                testingRepository.updateTesting(
                        selectedTesting,
                        oldProjectId
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedTesting));
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
                Testing testing = new Testing();
                testing.setProjectId(getProjectIdInputField());
                testing.setTestContent(getContentInputField());
                testing.setCreateDate(LocalDate.now());
                testing.setLastChangeDate(LocalDate.now());
                testing.setFileExtension(getFileExtensionInputField());
                testingRepository.saveDevelopment(
                        testing
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", testing));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
                clearSelection();
                UUID projectId = testing.getProjectId();
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
        return !testIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty()
                && !contentInputField.getText().isEmpty()
                && !createDateInputField.getText().isEmpty()
                && !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !projectNameInputField.getText().isEmpty()
                && !contentInputField.getText().isEmpty()
                //&& !createDateInputField.getText().isEmpty()
                //&& !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedTesting != null) {
                    testingRepository.deleteTesting(
                            selectedTesting.getTestId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedTesting));
                    ControllerUtils.showSuccessfulDeletionDialog("Запись удалена успешно");
                    baseFillTable();
                    clearFields();
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
