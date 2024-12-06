package org.olesya.musicaldevapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.olesya.musicaldevapp.utils.CommonException;

import java.io.IOException;

@Slf4j
public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        Thread.setDefaultUncaughtExceptionHandler(HelloApplication::showError);

        stage.setTitle("MusicalDevApp");
        stage.setScene(scene);
        stage.show();
    }

    private static void showError(Thread t, Throwable e) {
        log.info("Default exception handler initialized with exception: ", e);
        if (Platform.isFxApplicationThread()) {
            showErrorDialog(e);
        } else {
            log.info("An unexpected error occurred in {}", t);
        }
    }

    private static void showErrorDialog(Throwable e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText("Произошла ошибка");
        String errorMessage = e.getMessage();
        if (e.getCause() != null && e.getCause() instanceof CommonException)
            errorMessage = e.getCause().getMessage();
        if (e.getCause() != null && e.getCause().getCause() != null && e.getCause().getCause() instanceof CommonException) {
            if (e.getCause().getCause() instanceof CommonException) {
                errorMessage = e.getCause().getCause().getMessage();
            }
        }
        alert.setContentText(String.format("Произошла ошибка. Текст ошибки: %s", errorMessage));
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch();
    }
}