package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.Main;
import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Admin;
import com.mohanad.edufinance.model.RegularUser;
import com.mohanad.edufinance.util.CustomAlert;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Controller class for the Login view in the EduFinance application.
 * Manages user authentication and UI transition to the main dashboard.
 *
 * @author MOHANAD
 */
public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    /**
     * Initializes the controller class. Automatically called after the fxml file has been loaded.
     */
    @FXML
    public void initialize() {
        errorLabel.setVisible(false);
    }

    /**
     * Handles the login action when the user clicks the "تسجيل الدخول" button.
     * Queries the database, verifies credentials, creates the session user object,
     * and loads the Dashboard view.
     *
     * @param event The ActionEvent triggered by the button click
     */
    @FXML
    void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // 1. Validation
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("الرجاء إدخال اسم المستخدم وكلمة المرور.");
            errorLabel.setVisible(true);
            return;
        }

        // 2. Query Database
        String query = "SELECT id, username, password, role FROM users WHERE username = ? AND password = ?";
        try {
            DatabaseConnection dbConn = DatabaseConnection.getInstance();
            Connection conn = dbConn.getConnection();

            if (conn == null) {
                CustomAlert.show("خطأ في الاتصال", "عذراً، فشل الاتصال بقاعدة البيانات. تأكد من إعداد الملفات.", CustomAlert.AlertType.ERROR);
                return;
            }

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // User Authenticated
                        int id = rs.getInt("id");
                        String role = rs.getString("role");

                        // Instantiate polymorphic user session object
                        if ("ADMIN".equalsIgnoreCase(role)) {
                            Main.currentUser = new Admin(id, username, password);
                        } else {
                            Main.currentUser = new RegularUser(id, username, password);
                        }

                        errorLabel.setVisible(false);
                        System.out.println("نجاح تسجيل الدخول: " + Main.currentUser.getUsername() + " (" + Main.currentUser.getRole() + ")");

                        // Load Dashboard View
                        navigateToDashboard(event);

                    } else {
                        // Authentication failed
                        errorLabel.setText("اسم المستخدم أو كلمة المرور غير صحيحة.");
                        errorLabel.setVisible(true);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("خطأ أثناء استعلام تسجيل الدخول.");
            e.printStackTrace();
            CustomAlert.show("خطأ في النظام", "حدث خطأ غير متوقع أثناء فحص الحساب. تفاصيل الخطأ: " + e.getMessage(), CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Navigates the application scene to the main Dashboard.
     *
     * @param event The ActionEvent of the trigger
     */
    private void navigateToDashboard(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            
            // Apply RTL layout orientation globally to the dashboard
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            
            // Add custom style sheet
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("EduFinance - لوحة التحكم الرئيسية");
            stage.setResizable(true); // Allow maximizing/resizing for Dashboard
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            System.err.println("خطأ أثناء الانتقال إلى لوحة التحكم الرئيسية.");
            e.printStackTrace();
            CustomAlert.show("خطأ في التحميل", "تعذر تحميل لوحة التحكم الرئيسية. تفاصيل: " + e.getMessage(), CustomAlert.AlertType.ERROR);
        }
    }
}
