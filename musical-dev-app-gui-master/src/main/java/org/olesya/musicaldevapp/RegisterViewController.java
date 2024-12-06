package org.olesya.musicaldevapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.java.Log;
import org.olesya.musicaldevapp.data.entity.Role;
import org.olesya.musicaldevapp.data.entity.User;
import org.olesya.musicaldevapp.data.repository.RoleRepository;
import org.olesya.musicaldevapp.data.repository.UserRepository;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.olesya.musicaldevapp.utils.ControllerUtils.getPositiveIntegerFromTextField;
import static org.olesya.musicaldevapp.utils.ControllerUtils.showCommonWarningAlert;

@Log
public class RegisterViewController {
    @FXML
    private Button registerButton;

    @FXML
    private Button goBackButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private TextField ageTextField;

    @FXML
    private ChoiceBox<String> roleChoiceBox;

    private RoleRepository roleRepository;
    private UserRepository userRepository;

    public void initialize() throws CommonException, SQLException {
        userRepository = new UserRepository();
        roleRepository = new RoleRepository();

        fillRoleChoiceBox();

        registerButton.setDisable(true);

        registerButton.setOnAction(event -> {
            try {
                Integer userAge = null;

                try {
                    if (ageTextField.getText() != null && !ageTextField.getText().isEmpty())
                        userAge = getPositiveIntegerFromTextField(ageTextField, "Возраст");
                } catch (CommonException e) {
                    return;
                }

                List<User> existingUsers = userRepository.getAllUsers();
                if (existingUsers != null
                        && !existingUsers.isEmpty()
                        && existingUsers.stream().anyMatch(x -> x.getUserName().equals(usernameTextField.getText()))) {
                    showCommonWarningAlert(
                            "Пользователь с указанным логином уже существует!"
                    );
                    return;
                }

                User user = new User(
                        null,
                        getSelectedRole().getRoleId(),
                        usernameTextField.getText(),
                        passwordTextField.getText(),
                        userAge
                );

                UUID savedUserId = userRepository.saveUser(user);
                user.setUserId(savedUserId);

                CurrentUserContainer.setCurrentUser(user);
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
                Parent root = loader.load();
                MainViewController mainViewController = loader.getController();

                Stage stage = (Stage) registerButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (CommonException | IOException e) {
                throw new RuntimeException(e);
            }
        });

        goBackButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) goBackButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        usernameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            registerButton.setDisable(!checkEnterParametersFilled());
            if (!canPasswordOrUsernameFillingContinue(newValue.length()))
                usernameTextField.setText(newValue.substring(0, 50));
        });
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            registerButton.setDisable(!checkEnterParametersFilled());
            if (!canPasswordOrUsernameFillingContinue(newValue.length()))
                passwordTextField.setText(newValue.substring(0, 50));
        });
        roleChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            registerButton.setDisable(!checkEnterParametersFilled());
        });
    }

    private void fillRoleChoiceBox() throws CommonException, SQLException {
        List<Role> roles = roleRepository.getAllRoles();
        if (roles == null || roles.isEmpty())
            throw new CommonException(
                    "Ошибка! Для продолжения работы в БД необходимо добавить роли."
            );
        roleChoiceBox.getItems().addAll(roles.stream().map(Role::getRoleName).toList());
    }

    private Role getSelectedRole() throws CommonException {
        return roleRepository.getRoleByName(roleChoiceBox.getValue());
    }

    private boolean canPasswordOrUsernameFillingContinue(int currentFiledTextLength) {
        return currentFiledTextLength <= 50;
    }

    private boolean checkEnterParametersFilled() {
        return usernameTextField.getText() != null
                && !usernameTextField.getText().isEmpty()
                && passwordTextField.getText() != null
                && !passwordTextField.getText().isEmpty()
                && roleChoiceBox.getSelectionModel().getSelectedItem() != null;
    }
}
