package org.olesya.musicaldevapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.olesya.musicaldevapp.data.entity.User;
import org.olesya.musicaldevapp.data.repository.UserRepository;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.ControllerUtils;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.io.IOException;
import java.sql.SQLException;

public class EnterViewController {
    @FXML
    private Button enterButton;

    @FXML
    private Button goBackButton;

    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordTextField;

    public void initialize() throws CommonException, SQLException {
        UserRepository userRepository = new UserRepository();

        enterButton.setDisable(true);

        enterButton.setOnAction(event -> {
            try {
                User user = userRepository.getUserByUserNameAndPassword(
                        usernameTextField.getText(),
                        passwordTextField.getText()
                );
                if (user == null)
                    ControllerUtils.showCommonWarningAlert("Такого пользователя не существует!");
                else {
                    CurrentUserContainer.setCurrentUser(user);
                    FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
                    Parent root = loader.load();
                    MainViewController mainViewController = loader.getController();

                    Stage stage = (Stage) enterButton.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    stage.show();
                }
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
            enterButton.setDisable(!checkEnterParametersFilled());
            if (!canPasswordOrUsernameFillingContinue(newValue.length()))
                usernameTextField.setText(newValue.substring(0, 50));
        });
        passwordTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            enterButton.setDisable(!checkEnterParametersFilled());
            if (!canPasswordOrUsernameFillingContinue(newValue.length()))
                passwordTextField.setText(newValue.substring(0, 50));
        });
    }

    private boolean canPasswordOrUsernameFillingContinue(int currentFiledTextLength) {
        return currentFiledTextLength <= 50;
    }

    private boolean checkEnterParametersFilled() {
        return usernameTextField.getText() != null && !usernameTextField.getText().isEmpty() && passwordTextField.getText() != null && !passwordTextField.getText().isEmpty();
    }
}
