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

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class ModerationController {
    @FXML
    private TableView<Moderation> moderationTable;

    @FXML
    private TableColumn<Moderation, UUID> trackIdModerationTable;

    @FXML
    private TableColumn<Moderation, String> projectNameModerationTable;

    @FXML
    private Button moderationFilterButton;

    @FXML
    TextField trackIdModerationTextField;

    @FXML
    TextField projectNameModerationTextField;

    @FXML
    private TextField trackIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    private ModerationRepository moderationRepository;
    private ProjectRepository projectRepository;
    private ProjectUserRepository projectUserRepository;
    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Moderation selectedModeration = null;

    public void initialize() throws SQLException, CommonException {
        moderationRepository = new ModerationRepository();
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedModeration();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        trackIdModerationTable.setCellValueFactory(new PropertyValueFactory<>("trackId"));
        projectNameModerationTable.setCellValueFactory(new PropertyValueFactory<>("projectName"));
    }

    private void baseFillTable() throws CommonException {
        moderationTable.refresh();
        ObservableList<Moderation> moderationObservableList = FXCollections.observableArrayList();
        List<Moderation> moderations = moderationRepository.getAllModerations();
        moderationObservableList.addAll(moderations);
        moderationTable.setItems(moderationObservableList);
        moderationTable.refresh();
    }

    private void setOnActionFilterButton() {
        moderationFilterButton.setOnAction(event -> {
            try {
                List<Moderation> moderations = moderationRepository.getAllModerations();
                ObservableList<Moderation> moderationObservableList = FXCollections.observableArrayList();
                if (!trackIdModerationTextField.getText().isEmpty() || !projectNameModerationTextField.getText().isEmpty()) {
                    moderations = moderationRepository.getModerationListByIds(
                            getTrackId(),
                            getProjectId()
                    );
                }
                moderationObservableList.addAll(moderations == null ? List.of() : moderations);
                moderationTable.setItems(moderationObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private UUID getTrackId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(trackIdModerationTextField, "ID трека");
    }

    private UUID getProjectId() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameModerationTextField.getText());
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
        moderationTable.getSelectionModel().clearSelection();
        selectedModeration = null;
    }

    private void clearFields() {
        trackIdInputField.setText("");
        projectNameInputField.setText("");
    }

    private void autoFillFields() {
        trackIdInputField.setText(selectedModeration.getTrackId().toString());
        projectNameInputField.setText(selectedModeration.getProjectName());
    }

    private void setOnChangedSelectedModeration() {
        moderationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedModeration = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedModeration = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getTrackIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(trackIdInputField, "ID трека");
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

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                Moderation newModeration = new Moderation(
                        getTrackIdInputField(),
                        getProjectIdInputField()
                );
                moderationRepository.updateModeration(
                        selectedModeration,
                        newModeration
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedModeration));
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
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения записи все поля должны быть заполнены"
                    );
                Moderation moderation = new Moderation();
                moderation.setTrackId(getTrackIdInputField());
                moderation.setProjectId(getProjectIdInputField());
                moderationRepository.saveModeration(
                        moderation
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", moderation));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
                UUID projectId = moderation.getProjectId();
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
        return !trackIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedModeration != null) {
                    moderationRepository.deleteModeration(
                            selectedModeration.getTrackId(),
                            selectedModeration.getProjectId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedModeration));
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
