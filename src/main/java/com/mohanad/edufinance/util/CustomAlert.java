package com.mohanad.edufinance.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class to display custom-styled alert dialogs matching the application's
 * modern flat light theme, avoiding JavaFX's default OS-dependent styling.
 * Supports RTL layout and localized Arabic text.
 *
 * @author MOHANAD
 */
public class CustomAlert {

    /**
     * Enum specifying the visual styling and severity of the alert.
     */
    public enum AlertType {
        INFO, ERROR, WARNING, CONFIRMATION
    }

    private static boolean confirmedResult = false;

    /**
     * Displays a custom-styled modal dialog box.
     *
     * @param title   The title of the dialog window
     * @param message The Arabic message text to display
     * @param type    The severity type of the alert
     */
    public static void show(String title, String message, AlertType type) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UTILITY);
        window.setTitle(title);
        window.setMinWidth(350);
        window.setMinHeight(180);

        // Main layout container
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        layout.setStyle("-fx-background-color: #FFFFFF;");

        // Title label
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        // Message label
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("System", 14));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        // Color branding based on AlertType
        String btnColor = "#3182CE"; // Default Info Blue
        if (type == AlertType.ERROR) {
            titleLabel.setTextFill(Color.web("#E53E3E"));
            btnColor = "#E53E3E";
        } else if (type == AlertType.WARNING) {
            titleLabel.setTextFill(Color.web("#DD6B20"));
            btnColor = "#DD6B20";
        } else {
            titleLabel.setTextFill(Color.web("#3182CE"));
        }

        // Close Button
        Button closeButton = new Button("موافق");
        closeButton.setFont(Font.font("System", FontWeight.BOLD, 12));
        closeButton.setStyle(
            "-fx-background-color: " + btnColor + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        );
        closeButton.setOnAction(e -> window.close());

        // Hover effects
        final String finalBtnColor = btnColor;
        closeButton.setOnMouseEntered(e -> closeButton.setStyle(
            "-fx-background-color: " + darkenColor(finalBtnColor) + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        ));
        closeButton.setOnMouseExited(e -> closeButton.setStyle(
            "-fx-background-color: " + finalBtnColor + ";" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        ));

        layout.getChildren().addAll(titleLabel, msgLabel, closeButton);

        Scene scene = new Scene(layout);
        // Link CSS
        scene.getStylesheets().add(CustomAlert.class.getResource("/css/style.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();
    }

    /**
     * Displays a custom confirmation dialog with "Yes" (نعم) and "No" (لا) options.
     *
     * @param title   The title of the confirmation dialog
     * @param message The confirmation question text
     * @return true if the user clicks "نعم" (Yes), false otherwise
     */
    public static boolean showConfirmation(String title, String message) {
        confirmedResult = false;
        
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UTILITY);
        window.setTitle(title);
        window.setMinWidth(350);
        window.setMinHeight(180);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        layout.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#3182CE"));

        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("System", 14));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);

        // Buttons container
        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        // Yes Button
        Button yesBtn = new Button("نعم");
        yesBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        yesBtn.setStyle(
            "-fx-background-color: #3182CE;" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        );
        yesBtn.setOnAction(e -> {
            confirmedResult = true;
            window.close();
        });

        // No Button
        Button noBtn = new Button("إلغاء");
        noBtn.setFont(Font.font("System", FontWeight.BOLD, 12));
        noBtn.setStyle(
            "-fx-background-color: #E2E8F0;" +
            "-fx-text-fill: #4A5568;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        );
        noBtn.setOnAction(e -> {
            confirmedResult = false;
            window.close();
        });

        buttonsBox.getChildren().addAll(yesBtn, noBtn);
        layout.getChildren().addAll(titleLabel, msgLabel, buttonsBox);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(CustomAlert.class.getResource("/css/style.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();

        return confirmedResult;
    }

    private static Integer intInputResult = null;

    /**
     * Displays a custom input stage prompting for a numeric integer input.
     *
     * @param title  The title of the dialog window
     * @param header The descriptive text for what to input
     * @param prompt Placeholder text inside the input field
     * @return The entered integer value, or null if cancelled
     */
    public static Integer showNumberInputDialog(String title, String header, String prompt) {
        intInputResult = null;

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UTILITY);
        window.setTitle(title);
        window.setMinWidth(350);
        window.setMinHeight(200);

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        layout.setStyle("-fx-background-color: #FFFFFF;");

        Label headerLabel = new Label(header);
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        headerLabel.setTextFill(Color.web("#3182CE"));

        TextField inputField = new TextField();
        inputField.setPromptText(prompt);
        inputField.setStyle(
            "-fx-background-color: #FFFFFF;" +
            "-fx-border-color: #CBD5E0;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8;"
        );

        Label errorMsgLabel = new Label("الرجاء إدخال رقم صحيح موجب!");
        errorMsgLabel.setTextFill(Color.web("#E53E3E"));
        errorMsgLabel.setVisible(false);

        HBox buttonsBox = new HBox(15);
        buttonsBox.setAlignment(Pos.CENTER);

        Button confirmBtn = new Button("تأكيد وصرف");
        confirmBtn.setStyle(
            "-fx-background-color: #3182CE;" +
            "-fx-text-fill: #FFFFFF;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        );
        confirmBtn.setOnAction(e -> {
            try {
                int val = Integer.parseInt(inputField.getText().trim());
                if (val <= 0) {
                    throw new NumberFormatException();
                }
                intInputResult = val;
                window.close();
            } catch (NumberFormatException ex) {
                errorMsgLabel.setVisible(true);
            }
        });

        Button cancelBtn = new Button("إلغاء");
        cancelBtn.setStyle(
            "-fx-background-color: #E2E8F0;" +
            "-fx-text-fill: #4A5568;" +
            "-fx-background-radius: 5;" +
            "-fx-padding: 8 20 8 20;" +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> {
            intInputResult = null;
            window.close();
        });

        buttonsBox.getChildren().addAll(confirmBtn, cancelBtn);
        layout.getChildren().addAll(headerLabel, inputField, errorMsgLabel, buttonsBox);

        Scene scene = new Scene(layout);
        scene.getStylesheets().add(CustomAlert.class.getResource("/css/style.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();

        return intInputResult;
    }

    /**
     * Helper method to generate a darker shade for hover color transitions.
     *
     * @param hexColor The original hex color string
     * @return The darker hex color string
     */
    private static String darkenColor(String hexColor) {
        if ("#3182CE".equals(hexColor)) return "#2B6CB0"; // Blue
        if ("#E53E3E".equals(hexColor)) return "#C53030"; // Red
        if ("#DD6B20".equals(hexColor)) return "#C05621"; // Orange
        return "#1A202C";
    }
}
