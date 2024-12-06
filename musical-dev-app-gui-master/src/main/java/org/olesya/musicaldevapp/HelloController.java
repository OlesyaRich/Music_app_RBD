package org.olesya.musicaldevapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloController {
    @FXML
    private Button enterButton;

    @FXML
    private Button registerButton;

    public void initialize() {
        enterButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("enter-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) enterButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        registerButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("register-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) registerButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}