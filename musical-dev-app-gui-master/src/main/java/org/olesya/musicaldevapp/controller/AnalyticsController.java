package org.olesya.musicaldevapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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

public class AnalyticsController {
    @FXML
    private TableView<Analytics> analyticsTable;

    @FXML
    private TableColumn<Analytics, UUID> requirementIdAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, String> projectNameAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, String> requirementTypeNameAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, File> requirementContentAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, LocalDate> createDateAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, LocalDate> lastChangeDateAnalyticsTableColumn;

    @FXML
    private TableColumn<Analytics, String> fileExtensionAnalyticsTableColumn;

    @FXML
    private Button analyticsFilterButton;

    @FXML
    private TextField requirementIdAnalyticsTextField;

    @FXML
    private TextField projectNameAnalyticsTextField;

    @FXML
    private TextField requirementTypeNameAnalyticsTextField;

    @FXML
    private TextField requirementIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private ChoiceBox<String> requirementTypeChoiceBox;

    @FXML
    private TextField contentInputField;

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

    private AnalyticsRepository analyticsRepository;
    private ProjectRepository projectRepository;
    private ProjectUserRepository projectUserRepository;
    private RequirementTypeRepository requirementTypeRepository;
    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Analytics selectedAnalytics = null;

    public void initialize() throws SQLException, CommonException {
        analyticsRepository = new AnalyticsRepository();
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        requirementTypeRepository = new RequirementTypeRepository();
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerRequirementId();
        setOnChangeListenerProjectName();
        setOnChangeListenerRequirementTypeName();
        setOnActionFilterButton();
        try {
            fillChoiceBox();
        } catch (CommonException e) {
            saveChangesButton.setDisable(true);
            deleteButton.setDisable(true);
            addButton.setDisable(true);
            stopSelectionButton.setDisable(true);
            ControllerUtils.showCommonWarningAlert(e.getMessage());
        }
        setSaveChangesButtonOnAction();
        setOnActionStopSelectionButton();
        setOnChangedSelectedProject();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        requirementIdAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("requirementId"));
        projectNameAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        requirementTypeNameAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("requirementTypeName"));
        requirementContentAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("requirementContent"));
        createDateAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        lastChangeDateAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("lastChangeDate"));
        fileExtensionAnalyticsTableColumn.setCellValueFactory(new PropertyValueFactory<>("fileExtension"));
    }

    private void baseFillTable() throws CommonException {
        analyticsTable.refresh();
        ObservableList<Analytics> analyticsObservableList = FXCollections.observableArrayList();
        List<Analytics> analytics = analyticsRepository.getAllAnalytics();
        analyticsObservableList.addAll(analytics);
        analyticsTable.setItems(analyticsObservableList);
        analyticsTable.refresh();
    }

    private void setOnActionFilterButton() {
        analyticsFilterButton.setOnAction(event -> {
            try {
                if (!requirementTypeNameAnalyticsTextField.getText().isEmpty()) {
                    UUID requirementId = getRequirementId();
                    UUID projectId = getProjectId();
                    List<Analytics> analytics = analyticsRepository.getAnalyticsByRequirementIdAndProjectId(
                            requirementId,
                            projectId
                    );
                    ObservableList<Analytics> analyticsObservableList = FXCollections.observableArrayList();
                    analyticsObservableList.addAll(analytics);
                    analyticsTable.setItems(analyticsObservableList);
                }
                if (!requirementTypeNameAnalyticsTextField.getText().isEmpty()) {
                    UUID projectId = getProjectId();
                    UUID requirementTypeId = getRequirementTypeId();
                    List<Analytics> analytics = analyticsRepository.getAnalyticsByProjectIdAndRequirementTypeId(
                            projectId,
                            requirementTypeId
                    );
                    ObservableList<Analytics> analyticsObservableList = FXCollections.observableArrayList();
                    analyticsObservableList.addAll(analytics);
                    analyticsTable.setItems(analyticsObservableList);
                }
                List<Analytics> analytics = analyticsRepository.getAllAnalytics();
                ObservableList<Analytics> analyticsObservableList = FXCollections.observableArrayList();
                analyticsObservableList.addAll(analytics == null ? List.of() : analytics);
                analyticsTable.setItems(analyticsObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setOnChangeListenerRequirementId() {
        requirementIdAnalyticsTextField.textProperty().addListener((observable, oldValue, newValue) ->
                analyticsFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerProjectName() {
        projectNameAnalyticsTextField.textProperty().addListener((observable, oldValue, newValue) ->
                analyticsFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerRequirementTypeName() {
        requirementTypeNameAnalyticsTextField.textProperty().addListener((observable, oldValue, newValue) ->
                analyticsFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private boolean checkFilterButtonAvailability() {
        if (requirementTypeNameAnalyticsTextField.getText().isEmpty())
             return true;
        return requirementIdAnalyticsTextField.getText().isEmpty();
    }

    private UUID getRequirementId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(requirementIdAnalyticsTextField, "ID требования");
    }

    private UUID getProjectId() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameAnalyticsTextField.getText());
            //if (project == null) throw new CommonException("Проект с указанным наименованием не существует");
            return project == null ? null : project.getProjectId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private UUID getRequirementTypeId() throws CommonException {
        try {
            RequirementType requirementType = requirementTypeRepository.getRequirementTypeByName(requirementTypeNameAnalyticsTextField.getText());
            //if (requirementType == null) throw new CommonException("Тип требования с указанным наименованием не существует");
            return requirementType == null ? null : requirementType.getRequirementTypeId();
        } catch (CommonException e) {
            throw new CommonException("Тип требования с указанным наименованием не существует");
        }
    }

    private void fillChoiceBox() throws CommonException {
        if (requirementTypeChoiceBox.getItems() == null || (requirementTypeChoiceBox.getItems().isEmpty())) {
            List<RequirementType> requirementTypes = requirementTypeRepository.getAllRequirementTypes();
            if (requirementTypes == null || requirementTypes.isEmpty())
                throw new CommonException(
                        "Для выбора типа требования необходимо добавить типы требований в соответствующий справочник и перезапустить приложение."
                );
            requirementTypeChoiceBox.getItems().addAll(requirementTypes.stream().map(RequirementType::getRequirementTypeName).toList());
            requirementTypeChoiceBox.getSelectionModel().select(0);
        }
    }

    private void clearFields() {
        requirementIdInputField.setText("");
        projectNameInputField.setText("");
        requirementTypeChoiceBox.getSelectionModel().select(0);
        contentInputField.setText("");
        createDateInputField.setText("");
        lastChangeDateInputField.setText("");
        fileExtensionInputField.setText("");
    }

    private void autoFillFields() {
        requirementIdInputField.setText(selectedAnalytics.getRequirementId().toString());
        projectNameInputField.setText(selectedAnalytics.getProjectName());
        requirementTypeChoiceBox.getSelectionModel().select(selectedAnalytics.getRequirementTypeName());
        contentInputField.setText(selectedAnalytics.getRequirementContent().toString());
        createDateInputField.setText(selectedAnalytics.getCreateDate().toString());
        lastChangeDateInputField.setText(selectedAnalytics.getLastChangeDate().toString());
        fileExtensionInputField.setText(selectedAnalytics.getFileExtension());
    }

    private void setOnChangedSelectedProject() {
        analyticsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedAnalytics = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedAnalytics = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        analyticsTable.getSelectionModel().clearSelection();
        selectedAnalytics = null;
    }

    private UUID getRequirementIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(requirementIdInputField, "ID требования");
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

    private RequirementType getRequirementTypeFromChoiceBox() throws CommonException {
        return requirementTypeRepository.getRequirementTypeByName(
                requirementTypeChoiceBox.getSelectionModel().getSelectedItem()
        );
    }

    private File getContentInputField() throws CommonException {
        String fileExtension = getFileExtensionInputField();
        if (fileExtension == null || fileExtension.isEmpty())
            throw new CommonException("Поле 'Расширение файла' не может быть пустым");
        return ControllerUtils.getFileFromTextField(contentInputField, fileExtensionInputField.getText(), "Контент");
    }

    private LocalDate getCreateDateInputField() throws CommonException {
        return ControllerUtils.getLocalDateFromTextField(createDateInputField, "Дата создания");
    }

    private LocalDate getLastChangeDateInputField() throws CommonException {
        return ControllerUtils.getLocalDateFromTextField(lastChangeDateInputField, "Дата последнего редактирования");
    }

    private String getFileExtensionInputField() throws CommonException {
        return fileExtensionInputField.getText();
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                UUID oldProjectId = selectedAnalytics.getProjectId();
                selectedAnalytics.setProjectId(getProjectIdInputField());
                selectedAnalytics.setRequirementTypeId(getRequirementTypeFromChoiceBox().getRequirementTypeId());
                selectedAnalytics.setRequirementContent(getContentInputField());
                selectedAnalytics.setCreateDate(getCreateDateInputField());
                selectedAnalytics.setLastChangeDate(LocalDate.now());
                selectedAnalytics.setFileExtension(getFileExtensionInputField());
                analyticsRepository.updateAnalytics(
                        selectedAnalytics,
                        oldProjectId
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedAnalytics));
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
                Analytics analytics = new Analytics();
                analytics.setProjectId(getProjectIdInputField());
                analytics.setRequirementTypeId(getRequirementTypeFromChoiceBox().getRequirementTypeId());
                analytics.setRequirementContent(getContentInputField());
                analytics.setCreateDate(LocalDate.now());
                analytics.setLastChangeDate(LocalDate.now());
                analytics.setFileExtension(getFileExtensionInputField());
                analyticsRepository.saveAnalytics(
                        analytics
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", analytics));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
                UUID projectId = analytics.getProjectId();
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
        return !requirementIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty()
                && !contentInputField.getText().isEmpty()
                && !createDateInputField.getText().isEmpty()
                && !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !projectNameInputField.getText().isEmpty()
                //&& !requirementIdInputField.getText().isEmpty()
                && !contentInputField.getText().isEmpty()
                //&& !createDateInputField.getText().isEmpty()
                //&& !lastChangeDateInputField.getText().isEmpty()
                && !fileExtensionInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedAnalytics != null) {
                    analyticsRepository.deleteAnalytics(
                            selectedAnalytics.getRequirementId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedAnalytics));
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
