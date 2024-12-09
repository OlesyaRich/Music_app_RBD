package org.olesya.musicaldevapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;
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

public class UserController {
    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, UUID> userIdUsersTable;

    @FXML
    private TableColumn<User, String> roleNameUsersTable;

    @FXML
    private TableColumn<User, String> usernameUsersTable;

    @FXML
    private TableColumn<User, Integer> userAgeUsersTable;

    @FXML
    private TableColumn<User, String> passwordUsersTable;

    @FXML
    private Button userFilterButton;

    @FXML
    private TextField userIdUserTextField;

    @FXML
    private TextField roleNameUserTextField;

    @FXML
    private TextField usernameUserTextField;

    @FXML
    private TextField userIdInputField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    @FXML
    private TextField userNameInputField;

    @FXML
    private TextField ageInputField;

    @FXML
    private TextField passwordInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    private RoleRepository roleRepository;
    private UserRepository userRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private User selectedUser = null;

    public void initialize() throws SQLException, CommonException {
        roleRepository = new RoleRepository();
        userRepository = new UserRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerUserId();
        setOnChangeListenerRoleName();
        setOnChangeListenerUserName();
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
        setOnActionStopSelectionButton();
        setOnChangedSelectedUser();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
    }

    private void setCellValueFactories() {
        userIdUsersTable.setCellValueFactory(new PropertyValueFactory<>("userId"));
        roleNameUsersTable.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        usernameUsersTable.setCellValueFactory(new PropertyValueFactory<>("userName"));
        userAgeUsersTable.setCellValueFactory(new PropertyValueFactory<>("userAge"));
        passwordUsersTable.setCellValueFactory(new PropertyValueFactory<>("password"));
    }

    private void baseFillTable() throws CommonException {
        usersTable.refresh();
        ObservableList<User> userObservableList = FXCollections.observableArrayList();
        List<User> users = userRepository.getAllUsers();
        userObservableList.addAll(users);
        usersTable.setItems(userObservableList);
        usersTable.refresh();
    }

    private void setOnActionFilterButton() {
        userFilterButton.setOnAction(event -> {
            try {
                List<User> users = userRepository.getAllUsers();
                ObservableList<User> userObservableList = FXCollections.observableArrayList();
                if (!userIdUserTextField.getText().isEmpty()) {
                    users = new ArrayList<>();
                    users.add(userRepository.getUserById(
                            getUserId()
                    ));
                }
                if (userIdUserTextField.getText().isEmpty() && (!roleNameUserTextField.getText().isEmpty() || !usernameUserTextField.getText().isEmpty())) {
                    users = userRepository.getUsersByRoleIdAndUsername(
                            getUserRoleId(),
                            getUserName()
                    );
                }
                userObservableList.addAll(users == null ? List.of() : users);
                usersTable.setItems(userObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setOnChangeListenerUserId() {
        userIdUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                userFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerRoleName() {
        roleNameUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                userFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerUserName() {
        usernameUserTextField.textProperty().addListener((observable, oldValue, newValue) ->
                userFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private boolean checkFilterButtonAvailability() {
        return userIdUserTextField.getText().isEmpty() || (roleNameUserTextField.getText().isEmpty() && usernameUserTextField.getText().isEmpty());
    }

    private UUID getUserId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(userIdUserTextField, "ID пользователя");
    }

    private UUID getUserRoleId() throws CommonException {
        try {
            Role role = roleRepository.getRoleByName(roleNameUserTextField.getText());
            //if (role == null) throw new CommonException("Роль с указанным наименованием не существует");
            return role == null ? null : role.getRoleId();
        } catch (CommonException e) {
            throw new CommonException("Роль с указанным наименованием не существует");
        }
    }

    private String getUserName() throws CommonException {
        return usernameUserTextField.getText();
    }

    private void fillChoiceBox() throws CommonException {
        if (roleChoiceBox.getItems() == null || (roleChoiceBox.getItems().isEmpty())) {
            List<Role> roles = roleRepository.getAllRoles();
            if (roles == null || roles.isEmpty())
                throw new CommonException(
                        "Для выбора роли необходимо добавить новые роли в соответствующий справочник и перезапустить приложение."
                );
            roleChoiceBox.getItems().addAll(roles.stream().map(Role::getRoleName).toList());
            roleChoiceBox.getSelectionModel().select(0);
        }
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        usersTable.getSelectionModel().clearSelection();
        selectedUser = null;
    }

    private void clearFields() {
        userIdInputField.setText("");
        roleChoiceBox.getSelectionModel().select(0);
        userNameInputField.setText("");
        ageInputField.setText("");
        passwordInputField.setText("");
    }

    private void autoFillFields() {
        userIdInputField.setText(selectedUser.getUserId().toString());
        roleChoiceBox.getSelectionModel().select(selectedUser.getRoleName());
        userNameInputField.setText(selectedUser.getUserName());
        ageInputField.setText(selectedUser.getUserAge().toString());
        passwordInputField.setText(selectedUser.getPassword());
    }

    private void setOnChangedSelectedUser() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedUser = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedUser = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getUserIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(userIdInputField, "ID пользователя");
    }

    private UUID getRoleIdInputField() throws CommonException {
        try {
            return roleRepository.getRoleByName(roleChoiceBox.getSelectionModel().getSelectedItem()).getRoleId();
        } catch (CommonException e) {
            throw new CommonException("Проект с указанным наименованием не существует");
        }
    }

    private String getUserNameInputField() throws CommonException {
        return userNameInputField.getText();
    }

    private Integer getAgeInputField() throws CommonException {
        if (ageInputField.getText().isEmpty()) return null;
        return ControllerUtils.getPositiveIntegerFromTextFieldWithoutWarning(ageInputField, "Возраст");
    }

    private String getPasswordInputField() throws CommonException {
        return passwordInputField.getText();
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                selectedUser.setRoleId(getRoleIdInputField());
                selectedUser.setUserName(getUserNameInputField());
                selectedUser.setUserAge(getAgeInputField() == null ? selectedUser.getUserAge() : getAgeInputField());
                selectedUser.setPassword(getPasswordInputField());
                userRepository.updateUser(
                        selectedUser
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedUser));
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
                User user = new User();
                user.setRoleId(getRoleIdInputField());
                user.setUserName(getUserNameInputField());
                user.setUserAge(getAgeInputField() == null ? selectedUser.getUserAge() : getAgeInputField());
                user.setPassword(getPasswordInputField());
                userRepository.saveUser(
                        user
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", user));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkSaveAvailable() throws CommonException {
        return !userIdInputField.getText().isEmpty()
                && !userNameInputField.getText().isEmpty()
                && !passwordInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !userNameInputField.getText().isEmpty()
                && !passwordInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedUser != null) {
                    userRepository.deleteUser(
                            selectedUser.getUserId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedUser));
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
