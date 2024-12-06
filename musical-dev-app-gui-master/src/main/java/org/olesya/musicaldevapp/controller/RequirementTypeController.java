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
import lombok.extern.java.Log;
import org.olesya.musicaldevapp.data.entity.*;
import org.olesya.musicaldevapp.data.repository.*;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.ControllerUtils;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Log
public class RequirementTypeController {
    @FXML
    private TableView<RequirementType> requirementTypesTable;

    @FXML
    private TableColumn<RequirementType, UUID> requirementTypeIdRequirementTypesTable;

    @FXML
    private TableColumn<RequirementType, String> requirementTypeNameRequirementTypesTable;

    @FXML
    private Button requirementTypeFilterButton;

    @FXML
    private TextField requirementTypeIdRequirementTypeTextField;

    @FXML
    private TextField requirementTypeNameRequirementTypeTextField;

    @FXML
    private TextField requirementTypeIdInputField;

    @FXML
    private TextField requirementTypeNameInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    private RequirementTypeRepository requirementTypeRepository;
    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private RequirementType selectedRequirementType = null;

    public void initialize() throws SQLException, CommonException {
        requirementTypeRepository = new RequirementTypeRepository();
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerRequirementTypeId();
        setOnChangeListenerRequirementTypeName();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedRequirementType();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
        addButton.setDisable(!checkIfTheCurrentUserIsAdmin());
    }

    private void setCellValueFactories() {
        requirementTypeIdRequirementTypesTable.setCellValueFactory(new PropertyValueFactory<>("requirementTypeId"));
        requirementTypeNameRequirementTypesTable.setCellValueFactory(new PropertyValueFactory<>("requirementTypeName"));
    }

    private void baseFillTable() throws CommonException {
        requirementTypesTable.refresh();
        ObservableList<RequirementType> requirementTypeObservableList = FXCollections.observableArrayList();
        List<RequirementType> requirementTypes = requirementTypeRepository.getAllRequirementTypes();
        requirementTypeObservableList.addAll(requirementTypes);
        requirementTypesTable.setItems(requirementTypeObservableList);
        requirementTypesTable.refresh();
    }

    private void setOnActionFilterButton() {
        requirementTypeFilterButton.setOnAction(event -> {
            try {
                List<RequirementType> requirementTypes = requirementTypeRepository.getAllRequirementTypes();
                ObservableList<RequirementType> requirementTypeObservableList = FXCollections.observableArrayList();
                if (!requirementTypeIdRequirementTypeTextField.getText().isEmpty()) {
                    requirementTypes = new ArrayList<>();
                    requirementTypes.add(requirementTypeRepository.getRequirementTypeById(
                            getRequirementTypeId()
                    ));
                }
                if (!requirementTypeNameRequirementTypeTextField.getText().isEmpty()) {
                    requirementTypes = new ArrayList<>();
                    requirementTypes.add(requirementTypeRepository.getRequirementTypeByName(
                            getRequirementTypeName()
                    ));
                }
                requirementTypeObservableList.addAll(requirementTypes == null ? List.of() : requirementTypes);
                requirementTypesTable.setItems(requirementTypeObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setOnChangeListenerRequirementTypeId() {
        requirementTypeIdRequirementTypeTextField.textProperty().addListener((observable, oldValue, newValue) ->
                requirementTypeFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerRequirementTypeName() {
        requirementTypeNameRequirementTypeTextField.textProperty().addListener((observable, oldValue, newValue) ->
                requirementTypeFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private boolean checkFilterButtonAvailability() {
        return requirementTypeIdRequirementTypeTextField.getText().isEmpty() || requirementTypeNameRequirementTypeTextField.getText().isEmpty();
    }

    private UUID getRequirementTypeId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(requirementTypeIdRequirementTypeTextField, "ID типа требования");
    }

    private String getRequirementTypeName() throws CommonException {
        return requirementTypeNameRequirementTypeTextField.getText();
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        requirementTypesTable.getSelectionModel().clearSelection();
        //selectedRequirementType = null;
    }

    private void clearFields() {
        requirementTypeIdInputField.setText("");
        requirementTypeNameInputField.setText("");
    }

    private void autoFillFields() {
        requirementTypeIdInputField.setText(selectedRequirementType.getRequirementTypeId().toString());
        requirementTypeNameInputField.setText(selectedRequirementType.getRequirementTypeName());
    }

    private void setOnChangedSelectedRequirementType() {
        requirementTypesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedRequirementType = null;
                try {
                    addButton.setDisable(!checkIfTheCurrentUserIsAdmin());
                } catch (CommonException e) {
                    throw new RuntimeException(e);
                }
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                clearFields();
            } else {
                selectedRequirementType = newSelection;
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

    private UUID getRequirementTypeIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(requirementTypeIdInputField, "ID типа требования");
    }

    private String getRequirementTypeNameInputField() throws CommonException {
        return requirementTypeNameInputField.getText();
    }

    private void setSaveChangesButtonOnAction() {
        saveChangesButton.setOnAction(event -> {
            try {
                if (!checkSaveAvailable())
                    throw new CommonException(
                            "Для сохранения изменений все поля должны быть заполнены"
                    );
                selectedRequirementType.setRequirementTypeName(getRequirementTypeNameInputField());
                requirementTypeRepository.updateRequirementType(selectedRequirementType);
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedRequirementType));
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
                RequirementType requirementType = new RequirementType();
                requirementType.setRequirementTypeName(getRequirementTypeNameInputField());
                requirementTypeRepository.saveRequirementType(
                        requirementType
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", requirementType));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkSaveAvailable() throws CommonException {
        return !requirementTypeIdInputField.getText().isEmpty()
                && !requirementTypeNameInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !requirementTypeNameInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedRequirementType != null) {
                    requirementTypeRepository.deleteRequirementType(
                            selectedRequirementType.getRequirementTypeId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedRequirementType));
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
