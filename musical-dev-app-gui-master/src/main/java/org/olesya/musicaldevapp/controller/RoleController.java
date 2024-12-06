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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RoleController {
    @FXML
    private TableView<Role> rolesTable;

    @FXML
    private TableColumn<Role, UUID> roleIdRolesTable;

    @FXML
    private TableColumn<Role, String> roleNameRolesTable;

    @FXML
    private Button roleFilterButton;

    @FXML
    private TextField roleIdRoleTextField;

    @FXML
    private TextField roleNameRoleTextField;

    @FXML
    private TextField roleIdInputField;

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

    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Role selectedRole = null;

    public void initialize() throws SQLException, CommonException {
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerRequirementTypeId();
        setOnChangeListenerRequirementTypeName();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedRole();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
        addButton.setDisable(!checkIfTheCurrentUserIsAdmin());
    }

    private void setCellValueFactories() {
        roleIdRolesTable.setCellValueFactory(new PropertyValueFactory<>("roleId"));
        roleNameRolesTable.setCellValueFactory(new PropertyValueFactory<>("roleName"));
    }

    private void baseFillTable() throws CommonException {
        rolesTable.refresh();
        ObservableList<Role> roleObservableList = FXCollections.observableArrayList();
        List<Role> roles = roleRepository.getAllRoles();
        roleObservableList.addAll(roles);
        rolesTable.setItems(roleObservableList);
        rolesTable.refresh();
    }

    private void setOnActionFilterButton() {
        roleFilterButton.setOnAction(event -> {
            try {
                List<Role> roles = roleRepository.getAllRoles();
                ObservableList<Role> roleObservableList = FXCollections.observableArrayList();
                if (!roleIdRoleTextField.getText().isEmpty()) {
                    roles = new ArrayList<>();
                    roles.add(roleRepository.getRoleById(
                            getRoleId()
                    ));
                }
                if (!roleNameRoleTextField.getText().isEmpty()) {
                    roles = new ArrayList<>();
                    roles.add(roleRepository.getRoleByName(
                            getRoleName()
                    ));
                }
                roleObservableList.addAll(roles == null ? List.of() : roles);
                rolesTable.setItems(roleObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setOnChangeListenerRequirementTypeId() {
        roleIdRoleTextField.textProperty().addListener((observable, oldValue, newValue) ->
                roleFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerRequirementTypeName() {
        roleNameRoleTextField.textProperty().addListener((observable, oldValue, newValue) ->
                roleFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private boolean checkFilterButtonAvailability() {
        return roleIdRoleTextField.getText().isEmpty() || roleNameRoleTextField.getText().isEmpty();
    }

    private UUID getRoleId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(roleIdRoleTextField, "ID роли");
    }

    private String getRoleName() throws CommonException {
        return roleNameRoleTextField.getText();
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        rolesTable.getSelectionModel().clearSelection();
        selectedRole = null;
    }

    private void clearFields() {
        roleIdInputField.setText("");
        roleNameInputField.setText("");
    }

    private void autoFillFields() {
        roleIdInputField.setText(selectedRole.getRoleId().toString());
        roleNameInputField.setText(selectedRole.getRoleName());
    }

    private void setOnChangedSelectedRole() {
        rolesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedRole = null;
                try {
                    addButton.setDisable(!checkIfTheCurrentUserIsAdmin());
                } catch (CommonException e) {
                    throw new RuntimeException(e);
                }
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedRole = newSelection;
                addButton.setDisable(true);
                try {
                    saveChangesButton.setDisable(!checkIfTheCurrentUserIsAdmin());
                    deleteButton.setDisable(!checkIfTheCurrentUserIsAdmin());
                } catch (CommonException e) {
                    throw new RuntimeException(e);
                }
                autoFillFields();
            }
        });
    }

    private UUID getRoleIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(roleIdInputField, "ID роли");
    }

    private String getRoleNameInputField() throws CommonException {
        return roleNameInputField.getText();
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                selectedRole.setRoleName(getRoleNameInputField());
                roleRepository.updateRole(selectedRole);
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedRole));
                ControllerUtils.showSuccessfulUpdatingDialog("Запись изменена успешно");
                baseFillTable();
                clearFields();
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
                Role role = new Role();
                role.setRoleName(getRoleNameInputField());
                roleRepository.saveRole(
                        role
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", role));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkSaveAvailable() throws CommonException {
        return !roleIdInputField.getText().isEmpty()
                && !roleNameInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !roleNameInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedRole != null) {
                    roleRepository.deleteRole(
                            selectedRole.getRoleId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedRole));
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
