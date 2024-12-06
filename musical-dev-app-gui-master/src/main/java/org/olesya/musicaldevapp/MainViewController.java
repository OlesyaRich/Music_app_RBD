package org.olesya.musicaldevapp;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.stage.Stage;
import org.olesya.musicaldevapp.data.entity.Role;
import org.olesya.musicaldevapp.data.entity.User;
import org.olesya.musicaldevapp.data.repository.RoleRepository;
import org.olesya.musicaldevapp.utils.CommonException;
import org.olesya.musicaldevapp.utils.CurrentUserContainer;

import java.io.IOException;
import java.sql.SQLException;

public class MainViewController {
    @FXML
    private Label currentUserLabel;

    @FXML
    private Button exitButton;

    @FXML
    private Tab usersTab;

    private final User currentUser = CurrentUserContainer.getCurrentUser();

    private RoleRepository roleRepository;

    public void initialize() throws SQLException, CommonException {
        exitButton.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) exitButton.getScene().getWindow();
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        setCurrentUser();
        roleRepository = new RoleRepository();
        if (!checkIfTheCurrentUserIsAdmin())
            usersTab.setDisable(true);
    }

    private void setCurrentUser() {
        currentUserLabel.setText(currentUser.getUserName());
    }

    private boolean checkIfTheCurrentUserIsAdmin() throws CommonException {
        Role userRole = roleRepository.getRoleById(
                currentUser.getRoleId()
        );
        return userRole.getRoleName().equals("ADMIN");
    }
}
