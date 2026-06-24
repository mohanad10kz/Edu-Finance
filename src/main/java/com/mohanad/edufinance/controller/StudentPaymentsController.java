package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Student;
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
 * Controller class for the Student Payments (أقساط الطلبة) view.
 * Handles loading students list, recording installment payments, and reprinting/generating PDF receipts.
 * 
 * @author MOHANAD
 */
public class StudentPaymentsController {

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

    // --- Payment controls ---
    @FXML
    private Label selectedStudentLabel;
    @FXML
    private Label remainingFeesLabel;
    @FXML
    private TextField paymentAmountField;

    private ObservableList<Student> studentList = FXCollections.observableArrayList();
    private Student selectedStudent;

    /**
     * Initializes the controller class. Sets up table columns, registers selection listener, and loads student data.
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
        loadStudentsData();

        // 3. Selection Listener on Table
        studentsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedStudent = newSelection;
                
                // Update payment section info
                selectedStudentLabel.setText(selectedStudent.getName());
                remainingFeesLabel.setText(String.format("%.2f ر.ل", selectedStudent.getRemainingAmount()));
            }
        });
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
     * Process an installment payment for the selected student.
     * Integrates database updates, logs a transaction, and prints a receipt.
     * 
     * @param event The ActionEvent triggered by the button click
     */
    @FXML
    void handlePayInstallment(ActionEvent event) {
        if (selectedStudent == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء اختيار طالب من الجدول لتنفيذ الدفع له.", CustomAlert.AlertType.WARNING);
            return;
        }

        String amountStr = paymentAmountField.getText().trim();
        if (amountStr.isEmpty()) {
            CustomAlert.show("مبلغ فارغ", "الرجاء إدخال قيمة القسط المراد دفعه.", CustomAlert.AlertType.WARNING);
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            CustomAlert.show("خطأ في القيمة", "الرجاء إدخال مبلغ صحيح (رقم موجب أكبر من الصفر).", CustomAlert.AlertType.ERROR);
            return;
        }

        // Check if amount exceeds remaining balance
        double remaining = selectedStudent.getRemainingAmount();
        if (amount > remaining) {
            CustomAlert.show("تجاوز المبلغ", 
                    String.format("المبلغ المدخل (%.2f) يتجاوز الرسوم المتبقية على الطالب (%.2f).", amount, remaining), 
                    CustomAlert.AlertType.WARNING);
            return;
        }

        Connection conn = DatabaseConnection.getInstance().getConnection();
        try {
            // Disable auto commit to support transactions
            conn.setAutoCommit(false);

            // 1. Update Student paid_amount
            String updateStudentSQL = "UPDATE students SET paid_amount = paid_amount + ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateStudentSQL)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, selectedStudent.getId());
                pstmt.executeUpdate();
            }

            // 2. Insert Transaction Record
            String insertTransactionSQL = "INSERT INTO transactions (type, amount, date, person_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(insertTransactionSQL)) {
                pstmt.setString(1, "STUDENT_PAYMENT");
                pstmt.setDouble(2, amount);
                pstmt.setString(3, LocalDate.now().toString()); // Format YYYY-MM-DD
                pstmt.setInt(4, selectedStudent.getId());
                pstmt.executeUpdate();
            }

            // Commit database transaction
            conn.commit();
            conn.setAutoCommit(true);

            // Refresh student model copy locally for receipt display
            selectedStudent.setPaidAmount(selectedStudent.getPaidAmount() + amount);
            selectedStudent.setRemainingAmount(selectedStudent.getRemainingAmount() - amount);

            CustomAlert.show("تم الدفع", "تم سداد القسط بنجاح وتسجيل المعاملة.", CustomAlert.AlertType.INFO);
            
            // 3. Generate Receipt PDF
            PdfReceiptGenerator.generateStudentReceipt(selectedStudent, amount);

            // Reset UI
            clearFields();
            loadStudentsData();

        } catch (SQLException e) {
            try {
                conn.rollback();
                conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            CustomAlert.show("خطأ مالي", "فشل إجراء المعاملة المالية وقيدها في قاعدة البيانات. تم إلغاء العملية.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Clears all fields in the forms.
     */
    private void clearFields() {
        selectedStudent = null;
        studentsTable.getSelectionModel().clearSelection();
        selectedStudentLabel.setText("الرجاء اختيار طالب من الجدول");
        remainingFeesLabel.setText("0.00 ر.ل");
        paymentAmountField.clear();
    }
}
