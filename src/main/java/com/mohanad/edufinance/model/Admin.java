package com.mohanad.edufinance.model;

/**
 * Concrete class representing an Administrator in the system.
 * Inherits common characteristics from the {@link User} class, demonstrating Inheritance.
 * Administrators have full access to all system features including Settings.
 *
 * @author MOHANAD
 */
public class Admin extends User {

    /**
     * Default constructor.
     */
    public Admin() {
        super();
    }

    /**
     * Parameterized constructor.
     *
     * @param id       The admin user ID
     * @param username The admin username
     * @param password The admin password
     */
    public Admin(int id, String username, String password) {
        super(id, username, password, "ADMIN");
    }
}
