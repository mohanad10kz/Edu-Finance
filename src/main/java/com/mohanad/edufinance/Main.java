package com.mohanad.edufinance;

import com.mohanad.edufinance.database.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Main application class. In Phase 1, it serves as a runner to verify the database
 * setup, schemas, and default seeds. In Phase 3, this will launch the JavaFX UI.
 *
 * @author MOHANAD
 */
public class Main {

    /**
     * Entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        System.out.println("=== EduFinance: بدء تشغيل النظام والتحقق من قاعدة البيانات ===");
        
        try {
            // Get database connection instance (triggers initialization and seeding)
            DatabaseConnection dbConnection = DatabaseConnection.getInstance();
            Connection conn = dbConnection.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("نجاح: تم الاتصال بقاعدة البيانات بنجاح.");
                
                // Print all users in DB
                System.out.println("\n--- قائمة المستخدمين في قاعدة البيانات ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, username, role FROM users")) {
                    while (rs.next()) {
                        System.out.printf("المعرف: %d | اسم المستخدم: %s | الصلاحية: %s%n",
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("role"));
                    }
                }
                
                // Print all grades in DB
                System.out.println("\n--- قائمة الصفوف والرسوم الدراسية ---");
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT id, name, total_fees FROM grades")) {
                    while (rs.next()) {
                        System.out.printf("المعرف: %d | اسم الصف: %s | الرسوم الإجمالية: %.2f%n",
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getDouble("total_fees"));
                    }
                }
                
                System.out.println("\n=======================================================");
                System.out.println("المرحلة الأولى تمت بنجاح! قاعدة البيانات جاهزة ومغذية.");
            } else {
                System.err.println("فشل: اتصال قاعدة البيانات فارغ أو مغلق.");
            }
            
        } catch (Exception e) {
            System.err.println("حدث خطأ أثناء تشغيل النظام والتحقق من قاعدة البيانات.");
            e.printStackTrace();
        }
    }
}