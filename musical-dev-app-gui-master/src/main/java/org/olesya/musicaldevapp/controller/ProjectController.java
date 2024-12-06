package org.olesya.musicaldevapp.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import lombok.Setter;
import org.olesya.musicaldevapp.HelloApplication;
import org.olesya.musicaldevapp.data.entity.*;
import org.olesya.musicaldevapp.data.repository.AnalyticsRepository;
import org.olesya.musicaldevapp.data.repository.ProjectRepository;
import org.olesya.musicaldevapp.data.repository.ProjectUserRepository;
import org.olesya.musicaldevapp.data.repository.RoleRepository;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.ControllerUtils;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProjectController {
    @FXML
    private TableView<Project> projectTable;

    @FXML
    private TableColumn<Project, UUID> projectIdProjectTable;

    @FXML
    private TableColumn<Project, String> projectNameProjectTable;

    @FXML
    private TableColumn<Project, LocalDate> createDateProjectTable;

    @FXML
    private TableColumn<Project, LocalDate> lastChangeDateProjectTable;

    @FXML
    private TableColumn<Project, String> versionProjectTable;

    @FXML
    private Button projectFilterButton;

    @FXML
    private TextField projectIdProjectTextField;

    @FXML
    private TextField projectNameProjectTextField;

    @FXML
    private TextField projectIdInputField;

    @FXML
    private TextField projectNameInputField;

    @FXML
    private TextField versionInputField;

    @FXML
    private TextField createDateInputField;

    @FXML
    private TextField lastChangeDateInputField;

    @FXML
    private Button saveChangesButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addButton;

    @FXML
    private Button stopSelectionButton;

    @FXML
    private Button checkProjectAggregatedInfoButton;

    private AnalyticsRepository analyticsRepository;
    private ProjectRepository projectRepository;
    private ProjectUserRepository projectUserRepository;
    private RoleRepository roleRepository;

    @Setter
    private User currentUser = CurrentUserContainer.getCurrentUser();

    private Project selectedProject = null;

    public void initialize() throws SQLException, CommonException {
        analyticsRepository = new AnalyticsRepository();
        projectRepository = new ProjectRepository();
        projectUserRepository = new ProjectUserRepository();
        roleRepository = new RoleRepository();
        setCellValueFactories();
        baseFillTable();
        setOnChangeListenerProjectId();
        setOnChangeListenerProjectName();
        setOnActionFilterButton();
        setOnActionStopSelectionButton();
        setOnChangedSelectedProject();
        setSaveChangesButtonOnAction();
        setDeleteButtonOnAction();
        setAddButtonOnAction();
        setCheckProjectAggregatedInfoButtonOnAction();
    }

    private void setCellValueFactories() {
        projectIdProjectTable.setCellValueFactory(new PropertyValueFactory<>("projectId"));
        projectNameProjectTable.setCellValueFactory(new PropertyValueFactory<>("projectName"));
        createDateProjectTable.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        lastChangeDateProjectTable.setCellValueFactory(new PropertyValueFactory<>("lastChangeDate"));
        versionProjectTable.setCellValueFactory(new PropertyValueFactory<>("version"));
    }

    private void baseFillTable() throws CommonException {
        projectTable.refresh();
        ObservableList<Project> projectObservableList = FXCollections.observableArrayList();
        List<Project> projects = projectRepository.getAllProjects();
        projectObservableList.addAll(projects);
        projectTable.setItems(projectObservableList);
        projectTable.refresh();
    }

    private void setOnChangeListenerProjectId() {
        projectIdProjectTextField.textProperty().addListener((observable, oldValue, newValue) ->
                projectFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnChangeListenerProjectName() {
        projectNameProjectTextField.textProperty().addListener((observable, oldValue, newValue) ->
                projectFilterButton.setDisable(!checkFilterButtonAvailability())
        );
    }

    private void setOnActionFilterButton() {
        projectFilterButton.setOnAction(event -> {
            try {
                List<Project> projects = projectRepository.getAllProjects();
                ObservableList<Project> projectObservableList = FXCollections.observableArrayList();
                if (!projectIdProjectTextField.getText().isEmpty()) {
                    projects = new ArrayList<>();
                    projects.add(projectRepository.getProjectById(
                            getProjectId()
                    ));
                }
                if (!projectNameProjectTextField.getText().isEmpty()) {
                    projects = new ArrayList<>();
                    projects.add(projectRepository.getProjectByName(
                            getProjectName()
                    ));
                }
                projectObservableList.addAll(projects == null ? List.of() : projects);
                projectTable.setItems(projectObservableList);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean checkFilterButtonAvailability() {
        return projectIdProjectTextField.getText().isEmpty() || projectNameProjectTextField.getText().isEmpty();
    }

    private UUID getProjectId() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(projectIdProjectTextField, "ID проекта");
    }

    private String getProjectName() {
        return projectNameProjectTextField.getText();
    }

    private void setOnActionStopSelectionButton() {
        stopSelectionButton.setOnAction(event -> clearSelection());
    }

    private void clearSelection() {
        projectTable.getSelectionModel().clearSelection();
        selectedProject = null;
    }

    private void clearFields() {
        projectIdInputField.setText("");
        projectNameInputField.setText("");
        versionInputField.setText("");
        createDateInputField.setText("");
        lastChangeDateInputField.setText("");
    }

    private void autoFillFields() {
        projectIdInputField.setText(selectedProject.getProjectId().toString());
        projectNameInputField.setText(selectedProject.getProjectName());
        versionInputField.setText(selectedProject.getVersion());
        createDateInputField.setText(selectedProject.getCreateDate().toString());
        lastChangeDateInputField.setText(selectedProject.getLastChangeDate().toString());
    }

    private void setOnChangedSelectedProject() {
        projectTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection == null) {
                selectedProject = null;
                addButton.setDisable(false);
                saveChangesButton.setDisable(true);
                deleteButton.setDisable(true);
                checkProjectAggregatedInfoButton.setDisable(true);
                clearFields();
            } else {
                selectedProject = newSelection;
                addButton.setDisable(true);
                saveChangesButton.setDisable(false);
                deleteButton.setDisable(false);
                checkProjectAggregatedInfoButton.setDisable(false);
                autoFillFields();
            }
        });
    }

    private UUID getProjectIdInputField() throws CommonException {
        return ControllerUtils.getUUIDFromTextField(projectIdInputField, "ID проекта");
    }

    private String getProjectNameInputField() throws CommonException {
        return projectNameInputField.getText();
    }

    private String getVersionInputField() throws CommonException {
        return versionInputField.getText();
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
                selectedProject.setProjectName(getProjectNameInputField());
                selectedProject.setVersion(getVersionInputField());
                selectedProject.setCreateDate(LocalDate.now());
                selectedProject.setLastChangeDate(LocalDate.now());
                projectRepository.updateProject(
                        selectedProject
                );
                //ControllerUtils.showSuccessfulUpdatingDialog(String.format("Запись '{%s}' изменена успешно", selectedProject));
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
                Project project = new Project();
                project.setProjectName(getProjectNameInputField());
                project.setVersion(getVersionInputField());
                project.setCreateDate(LocalDate.now());
                project.setLastChangeDate(LocalDate.now());
                UUID projectId = projectRepository.saveProject(
                        project
                );
                //ControllerUtils.showSuccessfulEntitySaveDialog(String.format("Запись '{%s}' сохранена успешно", project));
                ControllerUtils.showSuccessfulEntitySaveDialog("Запись сохранена успешно");
                baseFillTable();
                clearFields();
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
        return !projectIdInputField.getText().isEmpty()
                && !projectNameInputField.getText().isEmpty()
                && !versionInputField.getText().isEmpty()
                && !createDateInputField.getText().isEmpty()
                && !lastChangeDateInputField.getText().isEmpty();
    }

    private boolean checkAddAvailable() throws CommonException {
        return !projectNameInputField.getText().isEmpty()
                && !versionInputField.getText().isEmpty();
                //&& !createDateInputField.getText().isEmpty()
                //&& !lastChangeDateInputField.getText().isEmpty();
    }

    private void setDeleteButtonOnAction() {
        deleteButton.setOnAction(event -> {
            try {
                if (selectedProject != null) {
                    projectRepository.deleteProject(
                            selectedProject.getProjectId()
                    );
                    //ControllerUtils.showSuccessfulDeletionDialog(String.format("Запись '{%s}' удалена успешно", selectedProject));
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

    private void setCheckProjectAggregatedInfoButtonOnAction() {
        checkProjectAggregatedInfoButton.setOnAction(event -> {
            try {
                if (selectedProject == null)
                    throw new CommonException(
                            "Для просмотра агрегированных данных по проекту выберете проект"
                    );
                ProjectAggregatedInfo projectAggregatedInfo = projectRepository.getProjectAggregatedInfo(
                        selectedProject.getProjectId()
                );
                projectAggregatedInfo.setProjectRequirementsAmount(
                        analyticsRepository.getProjectRequirementsCount(
                                selectedProject.getProjectId()
                        )
                );
                showProjectAggregatedInfoViewingDialog(projectAggregatedInfo);
            } catch (CommonException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void showProjectAggregatedInfoViewingDialog(ProjectAggregatedInfo projectAggregatedInfo) throws CommonException {
        if (projectAggregatedInfo == null)
            throw new CommonException(
                    "Ошибка отображения формы просмотра агрегированной информации по проекту - информация не найдена"
            );

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Агрегированная информация по проекту");
        dialog.setHeaderText("Просмотр агрегированной информации по проекту");

        dialog.setResizable(true);
        dialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        dialog.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        dialog.setGraphic(new ImageView(HelloApplication.class.getResource("images/project-aggregated-info-view-image.jpg").toString()));

        ButtonType stopViewingButtonType = new ButtonType("Завершить просмотр", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(stopViewingButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Text text;

        TextField projectIdTextField = new TextField();
        projectIdTextField.setText(projectAggregatedInfo.getProjectId().toString());
        text = new Text(projectIdTextField.getText());
        projectIdTextField.setPrefWidth(text.getLayoutBounds().getWidth() + 10);
        projectIdTextField.setMinHeight(Region.USE_PREF_SIZE);
        projectIdTextField.setEditable(false);

        TextField projectNameTextField = new TextField();
        projectNameTextField.setText(projectAggregatedInfo.getProjectName());
        text = new Text(projectNameTextField.getText());
        projectNameTextField.setPrefWidth(text.getLayoutBounds().getWidth() + 10);
        projectNameTextField.setMinHeight(Region.USE_PREF_SIZE);
        projectNameTextField.setEditable(false);

        TextField projectCreateDateTextField = new TextField();
        projectCreateDateTextField.setText(projectAggregatedInfo.getCreateDate().toString());
        text = new Text(projectCreateDateTextField.getText());
        projectCreateDateTextField.setPrefWidth(text.getLayoutBounds().getWidth() + 10);
        projectCreateDateTextField.setMinHeight(Region.USE_PREF_SIZE);
        projectCreateDateTextField.setEditable(false);

        TextField projectLocalDateTextField = new TextField();
        projectLocalDateTextField.setText(projectAggregatedInfo.getLastChangeDate().toString());
        text = new Text(projectLocalDateTextField.getText());
        projectLocalDateTextField.setPrefWidth(text.getLayoutBounds().getWidth() + 10);
        projectLocalDateTextField.setMinHeight(Region.USE_PREF_SIZE);
        projectLocalDateTextField.setEditable(false);

        TextField projectAnalyticsAmountTextField = new TextField();
        projectAnalyticsAmountTextField.setText(projectAggregatedInfo.getProjectRequirementsAmount().toString());
        text = new Text(projectAnalyticsAmountTextField.getText());
        projectAnalyticsAmountTextField.setPrefWidth(text.getLayoutBounds().getWidth() + 10);
        projectAnalyticsAmountTextField.setMinHeight(Region.USE_PREF_SIZE);
        projectAnalyticsAmountTextField.setEditable(false);

        grid.add(new Label("ID проекта:"), 0, 0);
        grid.add(projectIdTextField, 1, 0);
        grid.add(new Label("Имя проекта:"), 0, 1);
        grid.add(projectNameTextField, 1, 1);
        grid.add(new Label("Дата создания проекта:"), 0, 2);
        grid.add(projectCreateDateTextField, 1, 2);
        grid.add(new Label("Дата последнего внесения изменений в проект:"), 0, 3);
        grid.add(projectLocalDateTextField, 1, 3);
        grid.add(new Label("Количество требований по проекту:"), 0, 4);
        grid.add(projectAnalyticsAmountTextField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> dialogButton == stopViewingButtonType);
    }

    private boolean checkIfTheCurrentUserIsAdmin() throws CommonException {
        Role userRole = roleRepository.getRoleById(
                currentUser.getRoleId()
        );
        return userRole.getRoleName().equals("ADMIN");
    }
}
