package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Teacher;
import com.mohanad.edufinance.model.Grade;
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
import java.sql.Types;

/**
 * Controller class for the Teachers Management view.
 * Handles Teacher CRUD operations and contains logic to dynamically update form labels 
 * depending on whether the teacher is paid a fixed salary or an hourly rate.
 *
 * @author MOHANAD
 */
public class TeachersController {

    // --- Table controls ---
    @FXML
    private TableView<Teacher> teachersTable;
    @FXML
    private TableColumn<Teacher, Integer> teacherIdCol;
    @FXML
    private TableColumn<Teacher, String> teacherNameCol;
    @FXML
    private TableColumn<Teacher, String> teacherPayTypeCol;
    @FXML
    private TableColumn<Teacher, Double> teacherPayValueCol;
    @FXML
    private TableColumn<Teacher, String> teacherGradeCol;

    // --- Form controls ---
    @FXML
    private TextField teacherNameField;
    @FXML
    private ComboBox<String> payTypeComboBox;
    @FXML
    private Label payValueLabel;
    @FXML
    private TextField payValueField;
    @FXML
    private ComboBox<Grade> teacherGradeComboBox;

    private ObservableList<Teacher> teacherList = FXCollections.observableArrayList();
    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private Teacher selectedTeacher;

    /**
     * Initializes the controller class. Registers selection listeners and configures
     * dynamic label switching on payType changes.
     */
    @FXML
    public void initialize() {
        // 1. Table Columns Setup
        teacherIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        teacherNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        teacherPayTypeCol.setCellValueFactory(new PropertyValueFactory<>("payType"));
        teacherPayValueCol.setCellValueFactory(new PropertyValueFactory<>("payValue"));
        teacherGradeCol.setCellValueFactory(new PropertyValueFactory<>("gradeName"));

        // 2. Load Data
        loadGradesComboBox();
        loadTeachersData();

        // 3. Setup payTypeComboBox choices
        payTypeComboBox.setItems(FXCollections.observableArrayList(
                "راتب ثابت (FIXED)",
                "بالحصة (HOURLY)"
        ));

        // 4. Listener for payTypeComboBox to dynamically switch input labels
        payTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                if (newVal.contains("FIXED")) {
                    payValueLabel.setText("قيمة الراتب الشهري (ر.ل)");
                    payValueField.setPromptText("مثال: 1500");
                } else if (newVal.contains("HOURLY")) {
                    payValueLabel.setText("سعر الحصة الواحدة (ر.ل)");
                    payValueField.setPromptText("مثال: 50");
                }
            }
        });

        // 5. Listener for Table Selection
        teachersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTeacher = newSelection;
                
                // Populate fields
                teacherNameField.setText(selectedTeacher.getName());
                payValueField.setText(String.valueOf(selectedTeacher.getPayValue()));
                
                // Select pay type combo
                String typeDisplay = "FIXED".equalsIgnoreCase(selectedTeacher.getPayType()) ? "راتب ثابت (FIXED)" : "بالحصة (HOURLY)";
                payTypeComboBox.setValue(typeDisplay);
                
                // Select grade combo
                if (selectedTeacher.getGradeId() != null) {
                    for (Grade grade : teacherGradeComboBox.getItems()) {
                        if (grade != null && grade.getId() == selectedTeacher.getGradeId()) {
                            teacherGradeComboBox.setValue(grade);
                            break;
                        }
                    }
                } else {
                    teacherGradeComboBox.setValue(null);
                }
            }
        });
    }

    /**
     * Loads the grades from the database to populate the associated grade ComboBox.
     */
    private void loadGradesComboBox() {
        gradeList.clear();
        // Add a null/empty option at the beginning to allow optional grade association
        gradeList.add(null);
        
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
            teacherGradeComboBox.setItems(gradeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads teacher records, joining grades to resolve associated class names.
     */
    private void loadTeachersData() {
        teacherList.clear();
        String query = "SELECT t.id, t.name, t.pay_type, t.pay_value, t.grade_id, g.name AS grade_name " +
                       "FROM teachers t " +
                       "LEFT JOIN grades g ON t.grade_id = g.id";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String payType = rs.getString("pay_type");
                    double payValue = rs.getDouble("pay_value");
                    int gIdVal = rs.getInt("grade_id");
                    Integer gradeId = rs.wasNull() ? null : gIdVal;
                    String gName = rs.getString("grade_name");

                    Teacher teacher = new Teacher(id, name, payType, payValue, gradeId);
                    teacher.setGradeName(gName != null ? gName : "غير مرتبط");
                    teacherList.add(teacher);
                }
            }
            teachersTable.setItems(teacherList);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في جلب البيانات", "فشل جلب قائمة المعلمين من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles adding a new Teacher.
     */
    @FXML
    void handleAddTeacher(ActionEvent event) {
        String name = teacherNameField.getText().trim();
        String payTypeSelection = payTypeComboBox.getValue();
        String payValueStr = payValueField.getText().trim();
        Grade selectedGrade = teacherGradeComboBox.getValue();

        if (name.isEmpty() || payTypeSelection == null || payValueStr.isEmpty()) {
            CustomAlert.show("مدخلات غير مكتملة", "الرجاء تعبئة اسم المعلم وتحديد طريقة وقيمة الدفع.", CustomAlert.AlertType.WARNING);
            return;
        }

        String payType = payTypeSelection.contains("FIXED") ? "FIXED" : "HOURLY";
        double payValue;
        try {
            payValue = Double.parseDouble(payValueStr);
            if (payValue < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            CustomAlert.show("خطأ في القيمة", "الرجاء إدخال قيمة مالية صالحة وموجبة للراتب أو سعر الحصة.", CustomAlert.AlertType.ERROR);
            return;
        }

        String insertSQL = "INSERT INTO teachers (name, pay_type, pay_value, grade_id) VALUES (?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, payType);
                pstmt.setDouble(3, payValue);
                if (selectedGrade != null) {
                    pstmt.setInt(4, selectedGrade.getId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم الحفظ", "تم تسجيل المعلم الجديد بنجاح.", CustomAlert.AlertType.INFO);
            clearFields();
            loadTeachersData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ قاعدة البيانات", "حدث خطأ أثناء إدخال المعلم بقاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles saving updates on the selected teacher's record.
     */
    @FXML
    void handleEditTeacher(ActionEvent event) {
        if (selectedTeacher == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء تحديد معلم من الجدول لتعديل بياناته.", CustomAlert.AlertType.WARNING);
            return;
        }

        String name = teacherNameField.getText().trim();
        String payTypeSelection = payTypeComboBox.getValue();
        String payValueStr = payValueField.getText().trim();
        Grade selectedGrade = teacherGradeComboBox.getValue();

        if (name.isEmpty() || payTypeSelection == null || payValueStr.isEmpty()) {
            CustomAlert.show("مدخلات غير مكتملة", "الرجاء تعبئة اسم المعلم وتحديد طريقة وقيمة الدفع.", CustomAlert.AlertType.WARNING);
            return;
        }

        String payType = payTypeSelection.contains("FIXED") ? "FIXED" : "HOURLY";
        double payValue;
        try {
            payValue = Double.parseDouble(payValueStr);
            if (payValue < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            CustomAlert.show("خطأ في القيمة", "الرجاء إدخال قيمة مالية صالحة وموجبة للراتب أو سعر الحصة.", CustomAlert.AlertType.ERROR);
            return;
        }

        String updateSQL = "UPDATE teachers SET name = ?, pay_type = ?, pay_value = ?, grade_id = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, name);
                pstmt.setString(2, payType);
                pstmt.setDouble(3, payValue);
                if (selectedGrade != null) {
                    pstmt.setInt(4, selectedGrade.getId());
                } else {
                    pstmt.setNull(4, Types.INTEGER);
                }
                pstmt.setInt(5, selectedTeacher.getId());
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم التحديث", "تم تحديث بيانات المعلم بنجاح.", CustomAlert.AlertType.INFO);
            clearFields();
            loadTeachersData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ قاعدة البيانات", "حدث خطأ أثناء تعديل بيانات المعلم في قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles deleting the selected teacher.
     */
    @FXML
    void handleDeleteTeacher(ActionEvent event) {
        if (selectedTeacher == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء تحديد معلم من الجدول لحذفه.", CustomAlert.AlertType.WARNING);
            return;
        }

        boolean confirm = CustomAlert.showConfirmation("تأكيد الحذف", 
                "هل أنت متأكد من رغبتك في حذف المعلم '" + selectedTeacher.getName() + "' نهائياً؟");
        if (confirm) {
            String deleteSQL = "DELETE FROM teachers WHERE id = ?";
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, selectedTeacher.getId());
                    pstmt.executeUpdate();
                }
                CustomAlert.show("تم الحذف", "تم حذف المعلم بنجاح من النظام.", CustomAlert.AlertType.INFO);
                clearFields();
                loadTeachersData();
            } catch (SQLException e) {
                e.printStackTrace();
                CustomAlert.show("خطأ قاعدة البيانات", "تعذر حذف المعلم لوجود ارتباطات مالية نشطة.", CustomAlert.AlertType.ERROR);
            }
        }
    }

    private void clearFields() {
        selectedTeacher = null;
        teachersTable.getSelectionModel().clearSelection();
        teacherNameField.clear();
        payTypeComboBox.setValue(null);
        payValueField.clear();
        teacherGradeComboBox.setValue(null);
        payValueLabel.setText("قيمة الراتب الشهري (ر.ل)");
    }
}
