package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.Main;
import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Grade;
import com.mohanad.edufinance.model.User;
import com.mohanad.edufinance.model.Admin;
import com.mohanad.edufinance.model.RegularUser;
import com.mohanad.edufinance.util.CustomAlert;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Controller class for the Settings view.
 * Enables Administrators to manage academic Grades (adding, editing, deleting and configuring total fees)
 * and System Users (creating accounts, editing details, assigning roles, and removing access).
 * Employs heavy exception handling to prevent crashes on invalid inputs.
 *
 * @author MOHANAD
 */
public class SettingsController {

    // --- Grade Management Controls ---
    @FXML
    private TableView<Grade> gradesTable;
    @FXML
    private TableColumn<Grade, Integer> gradeIdCol;
    @FXML
    private TableColumn<Grade, String> gradeNameCol;
    @FXML
    private TableColumn<Grade, Double> gradeFeesCol;
    @FXML
    private TextField gradeNameField;
    @FXML
    private TextField gradeFeesField;

    // --- User Management Controls ---
    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> userIdCol;
    @FXML
    private TableColumn<User, String> userUsernameCol;
    @FXML
    private TableColumn<User, String> userRoleCol;
    @FXML
    private TextField userUsernameField;
    @FXML
    private PasswordField userPasswordField;
    @FXML
    private ComboBox<String> userRoleComboBox;

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private ObservableList<User> userList = FXCollections.observableArrayList();

    private Grade selectedGrade;
    private User selectedUser;

    /**
     * Initializes the settings view, sets up tables, populates data, and binds listeners.
     */
    @FXML
    public void initialize() {
        // --- 1. Grades Table Setup ---
        gradeIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        gradeNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        gradeFeesCol.setCellValueFactory(new PropertyValueFactory<>("totalFees"));
        loadGradesData();

        // Listen for Grades selection
        gradesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedGrade = newSelection;
                gradeNameField.setText(selectedGrade.getName());
                gradeFeesField.setText(String.valueOf(selectedGrade.getTotalFees()));
            }
        });

        // --- 2. Users Table Setup ---
        userIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        userUsernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userRoleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        loadUsersData();

        // Listen for Users selection
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                userUsernameField.setText(selectedUser.getUsername());
                userPasswordField.setText(selectedUser.getPassword());
                
                String roleDisplay = "ADMIN".equalsIgnoreCase(selectedUser.getRole()) ? "مدير النظام (ADMIN)" : "مستخدم عادي (USER)";
                userRoleComboBox.setValue(roleDisplay);
            }
        });

        // --- 3. Combo Box Setup ---
        userRoleComboBox.setItems(FXCollections.observableArrayList(
                "مدير النظام (ADMIN)",
                "مستخدم عادي (USER)"
        ));
    }

    // ==========================================
    // GRADES DATABASE OPERATIONS (CRUD)
    // ==========================================

    /**
     * Loads grade records from the SQLite database and populates the TableView.
     */
    private void loadGradesData() {
        gradeList.clear();
        String query = "SELECT id, name, total_fees FROM grades";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    gradeList.add(new Grade(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("total_fees")
                    ));
                }
            }
            gradesTable.setItems(gradeList);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ قاعدة البيانات", "فشل تحميل المراحل الدراسية من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles adding a new Grade. Catches format exceptions on the fees field.
     */
    @FXML
    void handleAddGrade(ActionEvent event) {
        String name = gradeNameField.getText().trim();
        String feesStr = gradeFeesField.getText().trim();

        // Input validation
        if (name.isEmpty() || feesStr.isEmpty()) {
            CustomAlert.show("مدخلات ناقصة", "يرجى ملء كافة حقول المرحلة الدراسية.", CustomAlert.AlertType.WARNING);
            return;
        }

        double fees;
        try {
            fees = Double.parseDouble(feesStr);
            if (fees < 0) {
                throw new NumberFormatException("يجب أن تكون الرسوم رقماً موجباً.");
            }
        } catch (NumberFormatException e) {
            CustomAlert.show("خطأ في المدخلات", "يرجى إدخال قيمة رسوم صحيحة (رقم موجب).", CustomAlert.AlertType.ERROR);
            return;
        }

        String insertSQL = "INSERT INTO grades (name, total_fees) VALUES (?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, fees);
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم بنجاح", "تمت إضافة المرحلة الدراسية بنجاح.", CustomAlert.AlertType.INFO);
            clearGradeFields();
            loadGradesData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ التكرار", "اسم المرحلة الدراسية مسجل مسبقاً في النظام.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles editing the selected Grade.
     */
    @FXML
    void handleEditGrade(ActionEvent event) {
        if (selectedGrade == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى اختيار مرحلة دراسية من الجدول لتعديلها.", CustomAlert.AlertType.WARNING);
            return;
        }

        String name = gradeNameField.getText().trim();
        String feesStr = gradeFeesField.getText().trim();

        if (name.isEmpty() || feesStr.isEmpty()) {
            CustomAlert.show("مدخلات ناقصة", "يرجى ملء كافة الحقول لإتمام التعديل.", CustomAlert.AlertType.WARNING);
            return;
        }

        double fees;
        try {
            fees = Double.parseDouble(feesStr);
            if (fees < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            CustomAlert.show("خطأ في المدخلات", "يرجى إدخال قيمة رسوم صحيحة (رقم موجب).", CustomAlert.AlertType.ERROR);
            return;
        }

        String updateSQL = "UPDATE grades SET name = ?, total_fees = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, name);
                pstmt.setDouble(2, fees);
                pstmt.setInt(3, selectedGrade.getId());
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم التعديل", "تم حفظ التعديلات على المرحلة الدراسية بنجاح.", CustomAlert.AlertType.INFO);
            clearGradeFields();
            loadGradesData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ التكرار", "تعذر حفظ التعديل، قد يكون الاسم مكرراً.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles deleting the selected Grade.
     */
    @FXML
    void handleDeleteGrade(ActionEvent event) {
        if (selectedGrade == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى اختيار مرحلة دراسية لحذفها.", CustomAlert.AlertType.WARNING);
            return;
        }

        boolean confirm = CustomAlert.showConfirmation("تأكيد الحذف", 
                "هل أنت متأكد من حذف مرحلة '" + selectedGrade.getName() + "'؟ قد يؤدي هذا إلى حذف ارتباط الطلاب المسجلين بها.");
        if (confirm) {
            String deleteSQL = "DELETE FROM grades WHERE id = ?";
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, selectedGrade.getId());
                    pstmt.executeUpdate();
                }
                CustomAlert.show("تم الحذف", "تم حذف المرحلة الدراسية بنجاح.", CustomAlert.AlertType.INFO);
                clearGradeFields();
                loadGradesData();
            } catch (SQLException e) {
                e.printStackTrace();
                CustomAlert.show("خطأ في الحذف", "تعذر حذف المرحلة الدراسية بسبب ارتباطات نشطة بقاعدة البيانات.", CustomAlert.AlertType.ERROR);
            }
        }
    }

    private void clearGradeFields() {
        selectedGrade = null;
        gradesTable.getSelectionModel().clearSelection();
        gradeNameField.clear();
        gradeFeesField.clear();
    }


    // ==========================================
    // USERS DATABASE OPERATIONS (CRUD)
    // ==========================================

    /**
     * Loads system users from the database and updates TableView.
     */
    private void loadUsersData() {
        userList.clear();
        String query = "SELECT id, username, password, role FROM users";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String username = rs.getString("username");
                    String password = rs.getString("password");
                    String role = rs.getString("role");

                    // Instantiate concrete user polymorphically
                    if ("ADMIN".equalsIgnoreCase(role)) {
                        userList.add(new Admin(id, username, password));
                    } else {
                        userList.add(new RegularUser(id, username, password));
                    }
                }
            }
            usersTable.setItems(userList);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ قاعدة البيانات", "فشل تحميل قائمة المستخدمين من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles adding a new User account.
     */
    @FXML
    void handleAddUser(ActionEvent event) {
        String username = userUsernameField.getText().trim();
        String password = userPasswordField.getText().trim();
        String roleSelection = userRoleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || roleSelection == null) {
            CustomAlert.show("مدخلات ناقصة", "يرجى ملء كافة حقول المستخدم وتحديد الصلاحية.", CustomAlert.AlertType.WARNING);
            return;
        }

        String role = roleSelection.contains("ADMIN") ? "ADMIN" : "USER";

        String insertSQL = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, role);
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم بنجاح", "تم إنشاء حساب المستخدم بنجاح.", CustomAlert.AlertType.INFO);
            clearUserFields();
            loadUsersData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ التكرار", "اسم المستخدم مسجل مسبقاً، يرجى اختيار اسم مستخدم آخر.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles editing user details.
     */
    @FXML
    void handleEditUser(ActionEvent event) {
        if (selectedUser == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى اختيار مستخدم من الجدول لتعديله.", CustomAlert.AlertType.WARNING);
            return;
        }

        String username = userUsernameField.getText().trim();
        String password = userPasswordField.getText().trim();
        String roleSelection = userRoleComboBox.getValue();

        if (username.isEmpty() || password.isEmpty() || roleSelection == null) {
            CustomAlert.show("مدخلات ناقصة", "يرجى ملء كافة الحقول لإتمام تعديل المستخدم.", CustomAlert.AlertType.WARNING);
            return;
        }

        String role = roleSelection.contains("ADMIN") ? "ADMIN" : "USER";

        String updateSQL = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setString(3, role);
                pstmt.setInt(4, selectedUser.getId());
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم التعديل", "تم تعديل حساب المستخدم وحفظ البيانات.", CustomAlert.AlertType.INFO);
            
            // If editing own session, update static user
            if (Main.currentUser != null && Main.currentUser.getId() == selectedUser.getId()) {
                if ("ADMIN".equalsIgnoreCase(role)) {
                    Main.currentUser = new Admin(selectedUser.getId(), username, password);
                } else {
                    Main.currentUser = new RegularUser(selectedUser.getId(), username, password);
                }
            }

            clearUserFields();
            loadUsersData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ التكرار", "تعذر تعديل البيانات، قد يكون اسم المستخدم مكرراً.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles deleting user accounts. Restricts deleting the currently logged-in account.
     */
    @FXML
    void handleDeleteUser(ActionEvent event) {
        if (selectedUser == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى اختيار حساب مستخدم لحذفه.", CustomAlert.AlertType.WARNING);
            return;
        }

        // Prevent self deletion
        if (Main.currentUser != null && Main.currentUser.getId() == selectedUser.getId()) {
            CustomAlert.show("إجراء غير مسموح", "لا يمكنك حذف حسابك الشخصي الذي تستخدمه حالياً لتسجيل الدخول.", CustomAlert.AlertType.ERROR);
            return;
        }

        boolean confirm = CustomAlert.showConfirmation("تأكيد الحذف", 
                "هل أنت متأكد من حذف الحساب '" + selectedUser.getUsername() + "' نهائياً؟");
        if (confirm) {
            String deleteSQL = "DELETE FROM users WHERE id = ?";
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, selectedUser.getId());
                    pstmt.executeUpdate();
                }
                CustomAlert.show("تم الحذف", "تم إزالة حساب المستخدم بنجاح.", CustomAlert.AlertType.INFO);
                clearUserFields();
                loadUsersData();
            } catch (SQLException e) {
                e.printStackTrace();
                CustomAlert.show("خطأ في الحذف", "تعذر حذف الحساب من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
            }
        }
    }

    private void clearUserFields() {
        selectedUser = null;
        usersTable.getSelectionModel().clearSelection();
        userUsernameField.clear();
        userPasswordField.clear();
        userRoleComboBox.setValue(null);
    }
}
