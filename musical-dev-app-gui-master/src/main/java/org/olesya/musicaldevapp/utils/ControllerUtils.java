package org.olesya.musicaldevapp.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class ControllerUtils {
    public static void showCommonWarningAlert(String description) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Внимание!");
        alert.setHeaderText(String.format("%s", description));
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    public static Integer getIntegerFromTextField(TextField textField, String fieldName) throws CommonException {
        if (textField == null || textField.getText().isEmpty())
            return null;
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            showCommonWarningAlert("Поле '" + fieldName + "' должно быть целым числом");
            throw new CommonException("Ошибка форматов");
        }
    }

    public static Integer getPositiveIntegerFromTextField(TextField textField, String fieldName) throws CommonException {
        Integer number = getIntegerFromTextField(textField, fieldName);
        if (number == null) return null;
        if (number < 0) {
            showCommonWarningAlert("Поле '" + fieldName + "' должно быть положительным целым числом");
            throw new CommonException("Ошибка форматов");
        }
        return number;
    }

    public static Integer getIntegerFromTextFieldWithoutWarning(TextField textField, String fieldName) throws CommonException {
        if (textField == null || textField.getText().isEmpty())
            return null;
        try {
            return Integer.parseInt(textField.getText());
        } catch (NumberFormatException e) {
            throw new CommonException("Поле '" + fieldName + "' должно быть целым числом");
        }
    }

    public static Integer getPositiveIntegerFromTextFieldWithoutWarning(TextField textField, String fieldName) throws CommonException {
        Integer number = getIntegerFromTextFieldWithoutWarning(textField, fieldName);
        if (number == null) return null;
        if (number < 0) {
            throw new CommonException("Поле '" + fieldName + "' должно быть положительным целым числом");
        }
        return number;
    }

    public static UUID getUUIDFromTextField(
            TextField textField,
            String fieldName
    ) throws CommonException {
        if (textField.getText() == null || textField.getText().isEmpty())
            return null;
        try {
            return UUID.fromString(textField.getText());
        } catch (IllegalArgumentException e) {
            throw new CommonException("Поле '" + fieldName + "' должно быть UUID");
        }
    }

    public static File getFileFromTextField(
            TextField textField,
            String fileExtension,
            String fieldName
    ) throws CommonException  {
        if (textField.getText() == null || textField.getText().isEmpty())
            return null;

        if (fileExtension == null || fileExtension.isEmpty())
            return null;

        if (!fileExtension.startsWith("."))
            throw new CommonException(
                    "Указанное расширение '" + fileExtension + "' файла некорректно"
            );

        if (!textField.getText().endsWith(fileExtension))
            throw new CommonException(
                    "Указанное расширение файла не соответствует его пути, указанном в поле '" + fieldName + "'"
            );

        if (textField.getText() == null || textField.getText().isEmpty()) {
            throw new CommonException(
                    "Указанный в поле '" + fieldName + "' путь к файлу некорректен, или файл по данному пути не существует"
            );
        }

        File file = new File(textField.getText());
        if (!file.exists()
                || !file.isFile()
                || !file.canRead()
        ) {
            throw new CommonException(
                    "Указанный в поле '" + fieldName + "' путь к файлу некорректен, или файл по данному пути не существует"
            );
        }

        return file;
    }

    public static LocalDate getLocalDateFromTextField(
            TextField textField,
            String fieldName
    ) throws CommonException  {
        String textFieldValue = textField.getText();
        if (textField.getText() == null || textFieldValue.isEmpty())
            return null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            format.setLenient(false);
            format.parse(textFieldValue);
            DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd")
                    .toFormatter();
            return LocalDate.parse(textFieldValue, dateTimeFormatter);
        } catch (ParseException | DateTimeParseException e) {
            throw new CommonException("Значение в поле '" + fieldName + "' должно быть датой, соответствующей шаблону 'ГГГГ-ММ-ДД', " +
                    "где ГГГГ - год, ММ - месяц, ДД - день");
        }
    }

    public static void showInformationDialog(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Сообщение");
        alert.setHeaderText(header);
        alert.setContentText(content);

        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        alert.showAndWait();
    }

    public static void showSuccessfulEntitySaveDialog(String content) {
        showInformationDialog(
                "Запись успешно сохранена",
                content
        );
    }

    public static void showSuccessfulDeletionDialog(String message) {
        showInformationDialog(
                "Запись успешно удалена",
                message
        );
    }

    public static void showSuccessfulUpdatingDialog(String content) {
        showInformationDialog(
                "Запись успешно обновлена",
                content
        );
    }
}
