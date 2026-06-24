package com.mohanad.edufinance;

import com.mohanad.edufinance.database.DatabaseConnection;
import com.mohanad.edufinance.model.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.NodeOrientation;
import javafx.stage.Stage;

/**
 * The main bootstrap and JavaFX Application class for the EduFinance system.
 * Initializes the database connection and loads the initial Login screen.
 *
 * @author MOHANAD
 */
public class Main extends Application {

    /**
     * Stores the current logged-in user session polymorphically.
     */
    public static User currentUser;

    /**
     * Initializes the database connection before the GUI starts.
     * This is invoked automatically by the JavaFX runtime.
     */
    @Override
    public void init() {
        System.out.println("إعداد النظام: جاري الاتصال بقاعدة البيانات...");
        // Triggers singleton initialization and seeding
        DatabaseConnection.getInstance();
    }

    /**
     * Sets up and displays the primary stage with the Login screen.
     *
     * @param primaryStage The primary Stage container of the application
     * @throws Exception if FXML loading fails
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("بدء تشغيل واجهة المستخدم...");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();

        // Apply RTL layout orientation globally to the login screen
        root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);

        Scene scene = new Scene(root, 600, 500);
        
        // Load custom style sheet
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("EduFinance - تسجيل الدخول");
        primaryStage.setResizable(false);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Closes resources on application stop.
     */
    @Override
    public void stop() {
        System.out.println("إغلاق النظام: جاري إغلاق الاتصال بقاعدة البيانات...");
        DatabaseConnection.getInstance().closeConnection();
    }

    /**
     * Main entry point.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}