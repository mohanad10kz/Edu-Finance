package com.mohanad.edufinance.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Singleton class that manages the SQLite database connection and initialization.
 * It provides a single point of access to the database across the application
 * and automatically sets up tables and default seeds if they do not exist.
 *
 * @author MOHANAD
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private static final String DB_URL = "jdbc:sqlite:edufinance.db";

    /**
     * Private constructor to prevent direct instantiation.
     * Establishes the database connection and triggers database initialization.
     */
    private DatabaseConnection() {
        try {
            // Load SQLite JDBC Driver class
            Class.forName("org.sqlite.JDBC");
            
            // Establish Connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Enable Foreign Key support in SQLite
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            
            // Initialize Database Schema
            initializeDatabase();
        } catch (ClassNotFoundException e) {
            System.err.println("خطأ: لم يتم العثور على تعريف مكتبة SQLite JDBC.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("خطأ أثناء الاتصال بقاعدة البيانات SQLite.");
            e.printStackTrace();
        }
    }

    /**
     * Thread-safe method to retrieve the single instance of the database connection.
     *
     * @return The DatabaseConnection singleton instance
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        } else {
            try {
                if (instance.getConnection() == null || instance.getConnection().isClosed()) {
                    instance = new DatabaseConnection();
                }
            } catch (SQLException e) {
                instance = new DatabaseConnection();
            }
        }
        return instance;
    }

    /**
     * Gets the active SQL connection object.
     *
     * @return The JDBC Connection object
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes the active connection to the SQLite database.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("تم إغلاق الاتصال بقاعدة البيانات بنجاح.");
            } catch (SQLException e) {
                System.err.println("خطأ أثناء إغلاق قاعدة البيانات.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes the relational database schema. Creates tables for users,
     * grades, students, teachers, and transactions if they are not already present.
     */
    private void initializeDatabase() {
        try (Statement statement = connection.createStatement()) {
            
            // 1. Create Users Table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL," +
                "  password TEXT NOT NULL," +
                "  role TEXT NOT NULL CHECK(role IN ('ADMIN', 'USER'))" +
                ");"
            );

            // 2. Create Grades Table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS grades (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT UNIQUE NOT NULL," +
                "  total_fees REAL NOT NULL CHECK(total_fees >= 0)" +
                ");"
            );

            // 3. Create Students Table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS students (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  grade_id INTEGER," +
                "  paid_amount REAL DEFAULT 0 CHECK(paid_amount >= 0)," +
                "  FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE SET NULL" +
                ");"
            );

            // 4. Create Teachers Table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS teachers (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT NOT NULL," +
                "  pay_type TEXT NOT NULL CHECK(pay_type IN ('FIXED', 'HOURLY'))," +
                "  pay_value REAL NOT NULL CHECK(pay_value >= 0)," +
                "  grade_id INTEGER," +
                "  FOREIGN KEY (grade_id) REFERENCES grades(id) ON DELETE SET NULL" +
                ");"
            );

            // 5. Create Transactions Table
            statement.execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  type TEXT NOT NULL CHECK(type IN ('STUDENT_PAYMENT', 'TEACHER_PAYROLL'))," +
                "  amount REAL NOT NULL CHECK(amount > 0)," +
                "  date TEXT NOT NULL," +
                "  person_id INTEGER NOT NULL," +
                "  sessions INTEGER," +
                "  month_name TEXT" +
                ");"
            );

            System.out.println("تم إنشاء جداول قاعدة البيانات أو التحقق من وجودها بنجاح.");
            
            // Seed initial data
            seedDatabase();

        } catch (SQLException e) {
            System.err.println("خطأ أثناء إنشاء الجداول بقاعدة البيانات.");
            e.printStackTrace();
        }
    }

    /**
     * Seeds default system records (Users and Grades) if the tables are empty.
     */
    private void seedDatabase() {
        try {
            // Check and Seed Users
            String checkUsers = "SELECT COUNT(*) FROM users";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkUsers)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertUser = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
                        // Admin Account (admin / admin)
                        pstmt.setString(1, "admin");
                        pstmt.setString(2, "admin");
                        pstmt.setString(3, "ADMIN");
                        pstmt.executeUpdate();
                        
                        // Regular User Account (user / user)
                        pstmt.setString(1, "user");
                        pstmt.setString(2, "user");
                        pstmt.setString(3, "USER");
                        pstmt.executeUpdate();
                        
                        System.out.println("تم إدخال حسابات المستخدمين الافتراضية بنجاح (admin/admin, user/user).");
                    }
                }
            }

            // Check and Seed Grades
            String checkGrades = "SELECT COUNT(*) FROM grades";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(checkGrades)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertGrade = "INSERT INTO grades (name, total_fees) VALUES (?, ?)";
                    try (PreparedStatement pstmt = connection.prepareStatement(insertGrade)) {
                        // Grade 1: Primary (ابتدائي)
                        pstmt.setString(1, "ابتدائي");
                        pstmt.setDouble(2, 5000.0);
                        pstmt.executeUpdate();
                        
                        // Grade 2: Preparatory (إعدادي)
                        pstmt.setString(1, "إعدادي");
                        pstmt.setDouble(2, 7000.0);
                        pstmt.executeUpdate();
                        
                        // Grade 3: Secondary (ثانوي)
                        pstmt.setString(1, "ثانوي");
                        pstmt.setDouble(2, 10000.0);
                        pstmt.executeUpdate();
                        
                        System.out.println("تم إدخال المراحل الدراسية الافتراضية بنجاح (ابتدائي، إعدادي، ثانوي).");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("خطأ أثناء تغذية قاعدة البيانات بالبيانات الافتراضية.");
            e.printStackTrace();
        }
    }
}
