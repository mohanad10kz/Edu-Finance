package com.mohanad.edufinance.model;

/**
 * Abstract class representing a system User in the EduFinance application.
 * Serves as the base class for specific user roles like Admin and RegularUser,
 * demonstrating the OOP concept of Abstraction.
 *
 * @author MOHANAD
 */
public abstract class User {

    private int id;
    private String username;
    private String password;
    private String role; // 'ADMIN' or 'USER'

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id       The unique database identifier for the user
     * @param username The system login username
     * @param password The login password
     * @param role     The system authorization role ('ADMIN' or 'USER')
     */
    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    /**
     * Gets the user ID.
     *
     * @return The user ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the user ID.
     *
     * @param id The new user ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username.
     *
     * @param username The new username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the user password.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the user password.
     *
     * @param password The new password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the system role of the user.
     *
     * @return The user role
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the system role of the user.
     *
     * @param role The new role to set ('ADMIN' or 'USER')
     */
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
