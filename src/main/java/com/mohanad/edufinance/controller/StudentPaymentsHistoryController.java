package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Student;
import com.mohanad.edufinance.model.Transaction;
import com.mohanad.edufinance.util.CustomAlert;
import com.mohanad.edufinance.util.PdfReceiptGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
 * Controller class for the Student Payments History (سجل أقساط الطلبة) view.
 * Displays a historical log of all collected student payments and provides functionality to search by name and reprint receipts.
 * 
 * @author MOHANAD
 */
public class StudentPaymentsHistoryController {

    @FXML
    private TableView<Transaction> historyTable;
    @FXML
    private TableColumn<Transaction, Integer> idCol;
    @FXML
    private TableColumn<Transaction, String> studentNameCol;
    @FXML
    private TableColumn<Transaction, String> gradeCol;
    @FXML
    private TableColumn<Transaction, Double> amountCol;
    @FXML
    private TableColumn<Transaction, String> dateCol;
    @FXML
    private TextField searchField;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private Transaction selectedTransaction;

    /**
     * Initializes the controller. Sets up bindings, loads history data, and implements dynamic search filtering.
     */
    @FXML
    public void initialize() {
        // 1. Column bindings
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentNameCol.setCellValueFactory(new PropertyValueFactory<>("personName"));
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("payType")); // Use payType as a placeholder for grade name
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Load History Data
        loadHistoryData();

        // 3. Setup dynamic search filtering
        FilteredList<Transaction> filteredData = new FilteredList<>(transactionList, p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(transaction -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase();
                if (transaction.getPersonName() != null && transaction.getPersonName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });

        historyTable.setItems(filteredData);

        // 4. Selection Listener on Table
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedTransaction = newSel;
            }
        });
    }

    /**
     * Queries collected student payment transactions and loads them into the table.
     */
    private void loadHistoryData() {
        transactionList.clear();
        String query = "SELECT t.id, t.amount, t.date, t.person_id, s.name AS student_name, g.name AS grade_name " +
                       "FROM transactions t " +
                       "INNER JOIN students s ON t.person_id = s.id " +
                       "LEFT JOIN grades g ON s.grade_id = g.id " +
                       "WHERE t.type = 'STUDENT_PAYMENT' " +
                       "ORDER BY t.id DESC";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setDate(rs.getString("date"));
                    tx.setPersonId(rs.getInt("person_id"));
                    tx.setPersonName(rs.getString("student_name"));
                    tx.setPayType(rs.getString("grade_name") != null ? rs.getString("grade_name") : "غير محدد");
                    
                    transactionList.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في البيانات", "تعذر تحميل سجل أقساط الطلاب من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Reprint the PDF receipt for the selected payment transaction.
     * 
     * @param event The ActionEvent triggered by the reprint button click
     */
    @FXML
    void handlePrintReceipt(ActionEvent event) {
        if (selectedTransaction == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء تحديد معاملة من الجدول لإعادة طباعة الوصل.", CustomAlert.AlertType.WARNING);
            return;
        }

        Student student = fetchStudentById(selectedTransaction.getPersonId());
        if (student != null) {
            // Call PDF Generator
            PdfReceiptGenerator.generateStudentReceipt(student, selectedTransaction.getAmount());
            
            CustomAlert.show("تمت الطباعة", 
                    String.format("تمت إعادة توليد ملف PDF بنجاح باسم: \n[وصل_دفع_%s.pdf]", student.getName().replace(" ", "_")), 
                    CustomAlert.AlertType.INFO);
        } else {
            CustomAlert.show("خطأ", "فشل تحديد الطالب المرتبط بالمعاملة المحددة.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Queries database to reconstruct a Student object by ID.
     * 
     * @param studentId The ID of the student
     * @return The Student object, or null if not found
     */
    private Student fetchStudentById(int studentId) {
        String query = "SELECT s.name, s.grade_id, s.paid_amount, g.name AS grade_name, g.total_fees " +
                       "FROM students s " +
                       "LEFT JOIN grades g ON s.grade_id = g.id " +
                       "WHERE s.id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setInt(1, studentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Student student = new Student(
                                studentId,
                                rs.getString("name"),
                                rs.getInt("grade_id"),
                                rs.getDouble("paid_amount")
                        );
                        String gName = rs.getString("grade_name");
                        double totalFees = rs.getDouble("total_fees");
                        
                        student.setGradeName(gName != null ? gName : "غير محدد");
                        student.setTotalFees(totalFees);
                        student.setRemainingAmount(totalFees - student.getPaidAmount());
                        return student;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
