package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Student;
import com.mohanad.edufinance.model.Grade;
import com.mohanad.edufinance.util.CustomAlert;
import com.mohanad.edufinance.util.PdfReceiptGenerator;

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
import java.time.LocalDate;

/**
 * Controller class for the Students view in the EduFinance application.
 * Manages Student CRUD operations and student payment transactions, including DB updates,
 * input validation, exception handling, and calling the receipt PDF generator.
 *
 * @author MOHANAD
 */
public class StudentsController {

    // --- Table controls ---
    @FXML
    private TableView<Student> studentsTable;
    @FXML
    private TableColumn<Student, Integer> studentIdCol;
    @FXML
    private TableColumn<Student, String> studentNameCol;
    @FXML
    private TableColumn<Student, String> studentGradeCol;
    @FXML
    private TableColumn<Student, Double> studentTotalFeesCol;
    @FXML
    private TableColumn<Student, Double> studentPaidCol;
    @FXML
    private TableColumn<Student, Double> studentRemainingCol;

    // --- Form controls ---
    @FXML
    private TextField studentNameField;
    @FXML
    private ComboBox<Grade> studentGradeComboBox;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private Student selectedStudent;

    /**
     * Initializes the controller class. Automatically registers listeners and populates TableViews.
     */
    @FXML
    public void initialize() {
        // 1. Table Columns Setup
        studentIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentGradeCol.setCellValueFactory(new PropertyValueFactory<>("gradeName"));
        studentTotalFeesCol.setCellValueFactory(new PropertyValueFactory<>("totalFees"));
        studentPaidCol.setCellValueFactory(new PropertyValueFactory<>("paidAmount"));
        studentRemainingCol.setCellValueFactory(new PropertyValueFactory<>("remainingAmount"));

        // 2. Load Data
        loadGradesComboBox();
        loadStudentsData();

        // 3. Selection Listener on Table
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedStudent = newSelection;
                
                // Populate edit form
                studentNameField.setText(selectedStudent.getName());
                
                // Select grade in ComboBox
                for (Grade grade : studentGradeComboBox.getItems()) {
                    if (grade.getId() == selectedStudent.getGradeId()) {
                        studentGradeComboBox.setValue(grade);
                        break;
                    }
                }
            }
        });
    }

    /**
     * Loads the grades from the database and inserts them into the ComboBox.
     */
    private void loadGradesComboBox() {
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
            studentGradeComboBox.setItems(gradeList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads student records, joining the grades table to fetch fee details, and binds them to the TableView.
     */
    private void loadStudentsData() {
        studentList.clear();
        String query = "SELECT s.id, s.name, s.grade_id, s.paid_amount, g.name AS grade_name, g.total_fees " +
                       "FROM students s " +
                       "LEFT JOIN grades g ON s.grade_id = g.id";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Student student = new Student(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("grade_id"),
                            rs.getDouble("paid_amount")
                    );
                    
                    // Populate transient fields
                    String gName = rs.getString("grade_name");
                    double totalFees = rs.getDouble("total_fees");
                    
                    student.setGradeName(gName != null ? gName : "غير محدد");
                    student.setTotalFees(totalFees);
                    student.setRemainingAmount(totalFees - student.getPaidAmount());
                    
                    studentList.add(student);
                }
            }
            studentsTable.setItems(studentList);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في البيانات", "تعذر جلب بيانات الطلاب من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles adding a new student to the database.
     */
    @FXML
    void handleAddStudent(ActionEvent event) {
        String name = studentNameField.getText().trim();
        Grade selectedGrade = studentGradeComboBox.getValue();

        if (name.isEmpty() || selectedGrade == null) {
            CustomAlert.show("مدخلات غير مكتملة", "الرجاء كتابة اسم الطالب واختيار المرحلة الدراسية.", CustomAlert.AlertType.WARNING);
            return;
        }

        String insertSQL = "INSERT INTO students (name, grade_id, paid_amount) VALUES (?, ?, 0.0)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, selectedGrade.getId());
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم التسجيل", "تمت إضافة الطالب بنجاح إلى النظام.", CustomAlert.AlertType.INFO);
            clearFields();
            loadStudentsData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في قاعدة البيانات", "حدث خطأ أثناء حفظ بيانات الطالب.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles editing the details of the selected student.
     */
    @FXML
    void handleEditStudent(ActionEvent event) {
        if (selectedStudent == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى تحديد طالب من الجدول لتعديل بياناته.", CustomAlert.AlertType.WARNING);
            return;
        }

        String name = studentNameField.getText().trim();
        Grade selectedGrade = studentGradeComboBox.getValue();

        if (name.isEmpty() || selectedGrade == null) {
            CustomAlert.show("مدخلات غير مكتملة", "الرجاء كتابة اسم الطالب واختيار المرحلة الدراسية.", CustomAlert.AlertType.WARNING);
            return;
        }

        String updateSQL = "UPDATE students SET name = ?, grade_id = ? WHERE id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, selectedGrade.getId());
                pstmt.setInt(3, selectedStudent.getId());
                pstmt.executeUpdate();
            }
            CustomAlert.show("تم التعديل", "تم تعديل بيانات الطالب بنجاح.", CustomAlert.AlertType.INFO);
            clearFields();
            loadStudentsData();
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في التعديل", "حدث خطأ أثناء تعديل بيانات الطالب.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Handles deleting the selected student from the database.
     */
    @FXML
    void handleDeleteStudent(ActionEvent event) {
        if (selectedStudent == null) {
            CustomAlert.show("تحديد مطلوب", "يرجى اختيار طالب من الجدول أولاً لحذفه.", CustomAlert.AlertType.WARNING);
            return;
        }

        boolean confirm = CustomAlert.showConfirmation("تأكيد الحذف", 
                "هل أنت متأكد من رغبتك في حذف الطالب '" + selectedStudent.getName() + "'؟");
        if (confirm) {
            String deleteSQL = "DELETE FROM students WHERE id = ?";
            try {
                Connection conn = DatabaseConnection.getInstance().getConnection();
                try (PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {
                    pstmt.setInt(1, selectedStudent.getId());
                    pstmt.executeUpdate();
                }
                CustomAlert.show("تم الحذف", "تم إزالة الطالب بنجاح من قاعدة البيانات.", CustomAlert.AlertType.INFO);
                clearFields();
                loadStudentsData();
            } catch (SQLException e) {
                e.printStackTrace();
                CustomAlert.show("خطأ في الحذف", "تعذر حذف الطالب بسبب ارتباطات نشطة.", CustomAlert.AlertType.ERROR);
            }
        }
    }

    /**
     * Clears all fields in the forms.
     */
    private void clearFields() {
        selectedStudent = null;
        studentsTable.getSelectionModel().clearSelection();
        studentNameField.clear();
        studentGradeComboBox.setValue(null);
    }
}
