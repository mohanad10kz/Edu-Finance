package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.Main;
import com.mohanad.edufinance.util.CustomAlert;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.NodeOrientation;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller class for the Main Dashboard.
 * Coordinates the right sidebar navigation, handles role-based access control,
 * and dynamically loads specific child views into the central workspace.
 *
 * @author MOHANAD
 */
public class DashboardController {

    @FXML
    private Label userNameLabel;

    @FXML
    private Label userRoleLabel;

    @FXML
    private ToggleButton studentsBtn;

    @FXML
    private ToggleGroup navGroup;

    @FXML
    private ToggleButton teachersBtn;

    @FXML
    private ToggleButton payrollBtn;

    @FXML
    private ToggleButton settingsBtn;

    @FXML
    private StackPane contentArea;

    /**
     * Initializes the dashboard. Displays user info and applies role restrictions.
     * Selects and displays the Students View by default on startup.
     */
    @FXML
    public void initialize() {
        if (Main.currentUser != null) {
            userNameLabel.setText("أهلاً بك، " + Main.currentUser.getUsername());
            
            String roleText = "ADMIN".equalsIgnoreCase(Main.currentUser.getRole()) ? "مدير النظام" : "مستخدم عادي";
            userRoleLabel.setText("الصلاحية: " + roleText);

            // Role-Based Access Control (RBAC)
            if (!"ADMIN".equalsIgnoreCase(Main.currentUser.getRole())) {
                // For Regular User, hide or disable Settings View
                settingsBtn.setVisible(false);
                settingsBtn.setManaged(false);
            }
        }

        // Set default view on load
        studentsBtn.setSelected(true);
        loadView("/fxml/StudentsView.fxml");
    }

    /**
     * Shows the Students View in the central workspace.
     */
    @FXML
    void showStudentsView(ActionEvent event) {
        loadView("/fxml/StudentsView.fxml");
    }

    /**
     * Shows the Teachers View in the central workspace.
     */
    @FXML
    void showTeachersView(ActionEvent event) {
        loadView("/fxml/TeachersView.fxml");
    }

    /**
     * Shows the Payroll View in the central workspace.
     */
    @FXML
    void showPayrollView(ActionEvent event) {
        loadView("/fxml/PayrollView.fxml");
    }

    /**
     * Shows the Settings View in the central workspace (Restricted to Admin).
     */
    @FXML
    void showSettingsView(ActionEvent event) {
        if (Main.currentUser != null && "ADMIN".equalsIgnoreCase(Main.currentUser.getRole())) {
            loadView("/fxml/SettingsView.fxml");
        } else {
            CustomAlert.show("دخول غير مصرح", "لا تمتلك صلاحيات كافية لفتح شاشة الإعدادات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles the Logout action. Clears current session, prompts confirmation,
     * and redirects user to the Login screen.
     *
     * @param event The ActionEvent triggered by the logout button click
     */
    @FXML
    void handleLogout(ActionEvent event) {
        boolean confirm = CustomAlert.showConfirmation("تأكيد تسجيل الخروج", "هل أنت متأكد من رغبتك في تسجيل الخروج من النظام؟");
        if (confirm) {
            try {
                Main.currentUser = null; // Clear session

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
                Parent root = loader.load();
                
                // Set RTL orientation for Login Screen
                root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                Scene scene = new Scene(root, 600, 500);
                scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
                
                stage.setScene(scene);
                stage.setTitle("EduFinance - تسجيل الدخول");
                stage.setResizable(false); // Disallow resizing for Login Screen
                stage.centerOnScreen();
                stage.show();
            } catch (IOException e) {
                System.err.println("خطأ أثناء تسجيل الخروج وتحميل شاشة الدخول.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Dynamic view loading helper. Loads FXML layout into the StackPane content area.
     *
     * @param fxmlPath The path of the FXML file relative to resources
     */
    private void loadView(String fxmlPath) {
        try {
            contentArea.getChildren().clear();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Propagate RTL to the dynamically loaded view
            view.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            System.err.println("خطأ أثناء تحميل الواجهة الفرعية: " + fxmlPath);
            e.printStackTrace();
            
            // Display visual error indicator inside workspace if view fails to load
            Label errorLabel = new Label("عذراً، حدث خطأ أثناء تحميل هذه الواجهة.\nيرجى التأكد من اكتمال التثبيت.");
            errorLabel.setStyle("-fx-text-fill: #E53E3E; -fx-font-size: 16px; -fx-alignment: center; -fx-text-alignment: center;");
            contentArea.getChildren().add(errorLabel);
        }
    }
}
