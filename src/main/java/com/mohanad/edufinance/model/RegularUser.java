package com.mohanad.edufinance.model;

/**
 * Concrete class representing a Regular User in the system.
 * Inherits common characteristics from the {@link User} class, demonstrating Inheritance.
 * Regular users have limited system access (restricted from accessing Settings).
 *
 * @author MOHANAD
 */
public class RegularUser extends User {

    /**
     * Default constructor.
     */
    public RegularUser() {
        super();
    }

    /**
     * Parameterized constructor.
     *
     * @param id       The regular user ID
     * @param username The regular user username
     * @param password The regular user password
     */
    public RegularUser(int id, String username, String password) {
        super(id, username, password, "USER");
    }
}
