package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Teacher;
import com.mohanad.edufinance.model.Transaction;
import com.mohanad.edufinance.util.CustomAlert;
import com.mohanad.edufinance.util.PdfReceiptGenerator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Controller class for the Salary History view.
 * Displays a historical log of all paid teacher payroll transactions in a TableView.
 * Enables administrators to re-print PDF receipts for any selected transaction.
 *
 * @author MOHANAD
 */
public class SalaryHistoryController {

    @FXML
    private TableView<Transaction> historyTable;
    @FXML
    private TableColumn<Transaction, Integer> idCol;
    @FXML
    private TableColumn<Transaction, String> teacherNameCol;
    @FXML
    private TableColumn<Transaction, String> payTypeCol;
    @FXML
    private TableColumn<Transaction, Double> amountCol;
    @FXML
    private TableColumn<Transaction, String> detailsCol;
    @FXML
    private TableColumn<Transaction, String> dateCol;

    private ObservableList<Transaction> transactionList = FXCollections.observableArrayList();
    private Transaction selectedTransaction;

    // We also keep dynamic Teacher metadata fetched from the join to reconstruct teacher objects
    private ObservableList<Teacher> associatedTeachers = FXCollections.observableArrayList();

    /**
     * Initializes the controller class. Automatically binds columns and loads database records.
     */
    @FXML
    public void initialize() {
        // 1. Column bindings
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        teacherNameCol.setCellValueFactory(new PropertyValueFactory<>("personName"));
        payTypeCol.setCellValueFactory(new PropertyValueFactory<>("payType"));
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("monthName")); // We will override cell value or set it directly in loading
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        // 2. Load Data
        loadHistoryData();

        // 3. Selection Listener
        historyTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedTransaction = newSel;
            }
        });
    }

    /**
     * Queries paid teacher payroll transactions, joining the teachers and grades tables
     * to resolve names and structural metadata.
     */
    private void loadHistoryData() {
        transactionList.clear();
        associatedTeachers.clear();

        String query = "SELECT t.id, t.amount, t.date, t.sessions, t.month_name, t.person_id, " +
                       "te.name AS teacher_name, te.pay_type, te.pay_value, te.grade_id, g.name AS grade_name " +
                       "FROM transactions t " +
                       "INNER JOIN teachers te ON t.person_id = te.id " +
                       "LEFT JOIN grades g ON te.grade_id = g.id " +
                       "WHERE t.type = 'TEACHER_PAYROLL' " +
                       "ORDER BY t.id DESC";

        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    // Create Transaction
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    tx.setAmount(rs.getDouble("amount"));
                    tx.setDate(rs.getString("date"));
                    tx.setPersonId(rs.getInt("person_id"));
                    
                    int sessionsVal = rs.getInt("sessions");
                    tx.setSessions(rs.wasNull() ? null : sessionsVal);
                    tx.setMonthName(rs.getString("month_name"));

                    // Setup displays
                    tx.setPersonName(rs.getString("teacher_name"));
                    String type = rs.getString("pay_type");
                    tx.setPayType("FIXED".equalsIgnoreCase(type) ? "راتب شهري ثابت" : "بالحصة");
                    
                    // Format Details Column
                    if ("HOURLY".equalsIgnoreCase(type)) {
                        tx.setMonthName("عدد الحصص: " + tx.getSessions()); // Reuse monthName field as generic detail column for simple binding
                    } else {
                        tx.setMonthName("راتب شهر: " + tx.getMonthName());
                    }

                    transactionList.add(tx);

                    // Reconstruct associated Teacher object for PDF printing
                    int gIdVal = rs.getInt("grade_id");
                    Integer gradeId = rs.wasNull() ? null : gIdVal;
                    Teacher teacher = new Teacher(
                            rs.getInt("person_id"),
                            rs.getString("teacher_name"),
                            type,
                            rs.getDouble("pay_value"),
                            gradeId
                    );
                    teacher.setGradeName(rs.getString("grade_name") != null ? rs.getString("grade_name") : "غير مرتبط");
                    associatedTeachers.add(teacher);
                }
            }
            historyTable.setItems(transactionList);
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ في البيانات", "تعذر تحميل سجل الرواتب من قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    /**
     * Re-prints the PDF receipt for the selected transaction using the reconstructed Teacher object.
     *
     * @param event The ActionEvent triggered by the button click
     */
    @FXML
    void handlePrintReceipt(ActionEvent event) {
        if (selectedTransaction == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء تحديد معاملة من الجدول لإعادة طباعة الوصل.", CustomAlert.AlertType.WARNING);
            return;
        }

        // Find the reconstructed Teacher metadata corresponding to the transaction row index
        int selectedIndex = historyTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < associatedTeachers.size()) {
            Teacher teacher = associatedTeachers.get(selectedIndex);
            
            // Extract original raw details from the database values
            Integer sessions = selectedTransaction.getSessions();
            
            // Strip the "راتب شهر: " prefix we added for the UI display
            String rawMonth = selectedTransaction.getMonthName();
            if (rawMonth != null && rawMonth.startsWith("راتب شهر: ")) {
                rawMonth = rawMonth.substring(10);
            } else {
                rawMonth = null;
            }

            // Call PDF Generator
            PdfReceiptGenerator.generateTeacherReceipt(teacher, selectedTransaction.getAmount(), sessions, rawMonth);
            
            String filename = "وصل_صرف_" + teacher.getName().replace(" ", "_") + ".pdf";
            CustomAlert.show("تمت الطباعة", 
                    String.format("تمت إعادة توليد ملف PDF بنجاح باسم: \n[مسير_راتب_%s.pdf]", teacher.getName().replace(" ", "_")), 
                    CustomAlert.AlertType.INFO);
        } else {
            CustomAlert.show("خطأ", "فشل تحديد المعلم المرتبط بالمعاملة المحددة.", CustomAlert.AlertType.ERROR);
        }
    }
}
