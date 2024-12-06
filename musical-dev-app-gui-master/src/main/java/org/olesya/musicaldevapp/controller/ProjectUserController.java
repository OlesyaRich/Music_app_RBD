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
import org.olesya.musicaldevapp.data.entity.Project;
import org.olesya.musicaldevapp.data.entity.ProjectUser;
import org.olesya.musicaldevapp.data.entity.Role;
import org.olesya.musicaldevapp.data.entity.User;
import org.olesya.musicaldevapp.data.repository.*;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.ControllerUtils;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectUserController {
    @FXML
    private TableView<ProjectUser> projectUsersTable;

    @FXML
    private TableColumn<ProjectUser, UUID> projectUsersNameProjectUsersTable;

    @FXML
    private TableColumn<ProjectUser, String> projectNameProjectUsersTable;

    @FXML
    private TableColumn<ProjectUser, String> userNameProjectUsersTable;

    @FXML
    private Button projectUserFilterButton;

    @FXML
    private TextField projectUserIdProjectUserTextField;

    @FXML
    private TextField projectNameProjectUserTextField;

    @FXML
    private TextField userNameProjectUserTextField;

    @FXML
    private TextField projectUserIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private TextField userNameInputField;

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
    private UserRepository userRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private ProjectUser selectedProjectUser = null;

    public void initialize() throws SQLException, CommonException {
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        roleRepository = new RoleRepository();
        userRepository = new UserRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerProjectUserId();
        setOnChangeListenerProjectName();
        setOnChangeListenerUserName();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedProjectUser();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        projectUsersNameProjectUsersTable.setCellValueFactory(new PropertyValueFactory<>("projectUserId"));
        projectNameProjectUsersTable.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        userNameProjectUsersTable.setCellValueFactory(new PropertyValueFactory<>("userName"));
    }

    private void baseFillTable() throws CommonException {
        projectUsersTable.refresh();
        ObservableList<ProjectUser> projectUserObservableList = FXCollections.observableArrayList();
        List<ProjectUser> projectsUsers = projectUserRepository.getAllProjectUsers();
        projectUserObservableList.addAll(projectsUsers);
        projectUsersTable.setItems(projectUserObservableList);
        projectUsersTable.refresh();
    }

    private void setOnActionFilterButton() {
        projectUserFilterButton.setOnAction(event -> {
            try {
                List<ProjectUser> projectUsers = projectUserRepository.getAllProjectUsers();
                ObservableList<ProjectUser> projectUserObservableList = FXCollections.observableArrayList();
                if (!projectUserIdProjectUserTextField.getText().isEmpty()) {
                    projectUsers = new ArrayList<>();
                    projectUsers.add(projectUserRepository.getProjectUserById(
                            getProjectUserId()
                    ));
                }
                if (projectUserIdProjectUserTextField.getText().isEmpty() && (!userNameProjectUserTextField.getText().isEmpty() || !projectNameProjectUserTextField.getText().isEmpty())) {
                    projectUsers = projectUserRepository.getProjectUsersByProjectIdAndUserId(
                            getProjectId(),
                            getUserId()
                    );
                }
                projectUserObservableList.addAll(projectUsers == null ? List.of() : projectUsers);
                projectUsersTable.setItems(projectUserObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setOnChangeListenerProjectUserId() {
        projectUserIdProjectUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                projectUserFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerProjectName() {
        projectNameProjectUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                projectUserFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerUserName() {
        userNameProjectUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                projectUserFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private boolean checkFilterButtonAvailability() {
        return projectUserIdProjectUserTextField.getText().isEmpty() || (projectNameProjectUserTextField.getText().isEmpty() && userNameProjectUserTextField.getText().isEmpty());
    }

    private UUID getProjectUserId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(projectUserIdProjectUserTextField, "ID записи");
    }

    private UUID getProjectId() throws CommonException {
        try {
            Project project = projectRepository.getProjectByName(projectNameProjectUserTextField.getText());
            //if (project == null) throw new CommonException("Проект с указанным наименованием не существует");
            return project == null ? null : project.getProjectId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private UUID getUserId() throws CommonException {
        try {
            List<User> users = userRepository.getUsersByRoleIdAndUsername(null, userNameProjectUserTextField.getText());
            //if (users == null || users.isEmpty()) throw new CommonException("Пользователь с указанным наименованием не существует");
            return users == null || users.isEmpty() ? null : users.get(0).getUserId();
        } catch (CommonException e) {
            throw new CommonException("Пользователь с указанным наименованием не существует");
        }
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        projectUsersTable.getSelectionModel().clearSelection();
        selectedProjectUser = null;
    }

    private void clearFields() {
        projectUserIdInputField.setText("");
        projectNameInputField.setText("");
        userNameInputField.setText("");
    }

    private void autoFillFields() {
        projectUserIdInputField.setText(selectedProjectUser.getProjectId().toString());
        projectNameInputField.setText(selectedProjectUser.getProjectName());
        userNameInputField.setText(selectedProjectUser.getUserName());
    }

    private void setOnChangedSelectedProjectUser() {
        projectUsersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedProjectUser = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedProjectUser = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getProjectUserIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(projectUserIdInputField, "ID трека");
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

    private UUID getUserIdInputField() throws CommonException {
        try {
            List<User> user = userRepository.getUsersByRoleIdAndUsername(null, userNameInputField.getText());
            if (user == null || user.isEmpty()) throw new CommonException("Пользователь с указанным логином не существует");
            return user.get(0).getUserId();
        } catch (CommonException e) {
            throw new CommonException("Пользователь с указанным логином не существует");
        }
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                selectedProjectUser.setProjectId(getProjectId());
                selectedProjectUser.setUserId(getUserId());
                projectUserRepository.updateProjectUser(
                        selectedProjectUser
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedProjectUser));
                ControllerUtils.showSuccessfulUpdatingDialog("Запись изменена успешно");
                baseFillTable();
                clearFields();
                clearSelection();
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
                ProjectUser projectUser = new ProjectUser();
                projectUser.setProjectId(getProjectIdInputField());
                projectUser.setUserId(getUserIdInputField());
                projectUserRepository.saveProjectUser(
                        projectUser
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", projectUser));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkSaveAvailable() throws CommonException {
        return !projectUserIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty()
                && !userNameInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !projectNameInputField.getText().isEmpty()
                && !userNameInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedProjectUser != null) {
                    projectUserRepository.deleteProjectUser(
                            selectedProjectUser.getProjectUserId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedProjectUser));
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
