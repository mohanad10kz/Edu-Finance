package com.mohanad.edufinance.controller;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.Teacher;
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
 * Controller class for the Payroll view in the EduFinance application.
 * Filters teachers dynamically by name search and academic grade.
 * Polymorphically processes payroll using the {@link com.mohanad.edufinance.model.Payable} interface
 * and inserts transaction records into SQLite database.
 *
 * @author MOHANAD
 */
public class PayrollController {

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

    // --- Filter controls ---
    @FXML
    private TextField searchNameField;
    @FXML
    private ComboBox<Object> gradeFilterComboBox; // Holds Grade objects and a virtual String "الكل"

    // --- Action controls ---
    @FXML
    private Label selectedTeacherLabel;
    @FXML
    private Label payTypeLabel;
    @FXML
    private Label rateLabel;
    @FXML
    private Label rateValueLabel;

    private ObservableList<Teacher> allTeachersList = FXCollections.observableArrayList();
    private ObservableList<Teacher> filteredTeachersList = FXCollections.observableArrayList();
    private Teacher selectedTeacher;

    /**
     * Initializes the controller class. Automatically registers listeners for filtering
     * and sets up the table columns.
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
        loadGradesFilterComboBox();
        loadTeachersData();

        // 3. Name Search Listener
        searchNameField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 4. Grade ComboBox Listener
        gradeFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // 5. Table Selection Listener
        teachersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedTeacher = newSelection;
                
                selectedTeacherLabel.setText(selectedTeacher.getName());
                
                boolean isFixed = "FIXED".equalsIgnoreCase(selectedTeacher.getPayType());
                payTypeLabel.setText(isFixed ? "راتب شهري ثابت" : "بالحصة (أجر مقابل حصص)");
                
                rateLabel.setText(isFixed ? "قيمة الراتب الأساسي:" : "سعر الحصة الواحدة:");
                rateValueLabel.setText(String.format("%.2f ر.ل", selectedTeacher.getPayValue()));
            } else {
                resetDetailsCard();
            }
        });
    }

    /**
     * Loads grades from the database and inserts them into the filter ComboBox,
     * prefixing with a virtual "الكل" (All) option.
     */
    private void loadGradesFilterComboBox() {
        ObservableList<Object> filterItems = FXCollections.observableArrayList();
        filterItems.add("الكل"); // Add "All" option in Arabic
        
        String query = "SELECT id, name, total_fees FROM grades";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(query)) {
                while (rs.next()) {
                    filterItems.add(new Grade(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getDouble("total_fees")
                    ));
                }
            }
            gradeFilterComboBox.setItems(filterItems);
            gradeFilterComboBox.setValue("الكل"); // Select "All" by default
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads teacher records, joining grades to resolve academic class names.
     */
    private void loadTeachersData() {
        allTeachersList.clear();
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
                    allTeachersList.add(teacher);
                }
            }
            filteredTeachersList.setAll(allTeachersList);
            teachersTable.setItems(filteredTeachersList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Applies combined filters (name search and grade selection) to the TableView.
     */
    private void applyFilters() {
        String searchText = searchNameField.getText().trim().toLowerCase();
        Object selectedGradeObj = gradeFilterComboBox.getValue();

        filteredTeachersList.clear();

        for (Teacher teacher : allTeachersList) {
            boolean nameMatches = teacher.getName().toLowerCase().contains(searchText);
            boolean gradeMatches = false;

            if (selectedGradeObj == null || "الكل".equals(selectedGradeObj)) {
                gradeMatches = true; // Show all
            } else if (selectedGradeObj instanceof Grade) {
                Grade selectedGrade = (Grade) selectedGradeObj;
                if (teacher.getGradeId() != null && teacher.getGradeId() == selectedGrade.getId()) {
                    gradeMatches = true;
                }
            }

            if (nameMatches && gradeMatches) {
                filteredTeachersList.add(teacher);
            }
        }
    }

    /**
     * Processes payroll for the selected teacher polymorphically.
     * Logs the transaction in the database and triggers receipt PDF generation.
     */
    @FXML
    void handleProcessPayroll(ActionEvent event) {
        if (selectedTeacher == null) {
            CustomAlert.show("تحديد مطلوب", "الرجاء اختيار معلم من الجدول لإتمام صرف مستحقاته.", CustomAlert.AlertType.WARNING);
            return;
        }

        double finalPayAmount = 0.0;
        int sessions = 0;

        // Polymorphic payroll processing
        if ("HOURLY".equalsIgnoreCase(selectedTeacher.getPayType())) {
            // Teacher is HOURLY -> Prompt for sessions count
            Integer sessionsResult = CustomAlert.showNumberInputDialog(
                    "إدخال الحصص",
                    "أدخل عدد الحصص المنجزة للمعلم '" + selectedTeacher.getName() + "':",
                    "عدد الحصص"
            );
            
            if (sessionsResult == null) {
                // Cancelled
                return;
            }
            
            sessions = sessionsResult;
            // Polymorphic call using the calculatePay method
            finalPayAmount = selectedTeacher.calculatePay(sessions);
        } else {
            // Teacher is FIXED -> Confirm salary payout
            boolean confirm = CustomAlert.showConfirmation(
                    "تأكيد صرف الراتب",
                    "هل أنت متأكد من صرف الراتب الثابت بقيمة " + String.format("%.2f", selectedTeacher.getPayValue()) + 
                    " ر.ل للمعلم '" + selectedTeacher.getName() + "'؟"
            );
            
            if (!confirm) {
                return;
            }
            
            // Polymorphic call (sessions parameter is ignored for fixed salaries)
            finalPayAmount = selectedTeacher.calculatePay(0);
        }

        // Database transaction execution
        String insertSQL = "INSERT INTO transactions (type, amount, date, person_id) VALUES ('TEACHER_PAYROLL', ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setDouble(1, finalPayAmount);
                pstmt.setString(2, LocalDate.now().toString());
                pstmt.setInt(3, selectedTeacher.getId());
                pstmt.executeUpdate();
            }

            CustomAlert.show("تم الصرف بنجاح", 
                    String.format("تم صرف المستحقات المالية بقيمة (%.2f ر.ل) للمعلم بنجاح.", finalPayAmount), 
                    CustomAlert.AlertType.INFO);

            // Generate receipt PDF
            PdfReceiptGenerator.generateTeacherReceipt(selectedTeacher, finalPayAmount);

            // Reset selection
            teachersTable.getSelectionModel().clearSelection();
            resetDetailsCard();
            
        } catch (SQLException e) {
            e.printStackTrace();
            CustomAlert.show("خطأ مالي", "فشل قيد المعاملة المالية لصرف الراتب في قاعدة البيانات.", CustomAlert.AlertType.ERROR);
        }
    }

    private void resetDetailsCard() {
        selectedTeacher = null;
        selectedTeacherLabel.setText("الرجاء تحديد معلم");
        payTypeLabel.setText("-");
        rateLabel.setText("القيمة الأساسية:");
        rateValueLabel.setText("0.00 ر.ل");
    }
}
