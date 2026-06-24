package com.mohanad.edufinance.model;

/**
 * Concrete class representing an Academic Grade or Class stage in the system.
 * It contains the name of the grade and its associated yearly total fees.
 *
 * @author MOHANAD
 */
public class Grade {

    private int id;
    private String name;
    private double totalFees;

    /**
     * Default constructor.
     */
    public Grade() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id        The unique ID of the grade
     * @param name      The Arabic name of the grade (e.g., ابتدائي)
     * @param totalFees The total fees required for this grade
     */
    public Grade(int id, String name, double totalFees) {
        this.id = id;
        this.name = name;
        this.totalFees = totalFees;
    }

    /**
     * Gets the grade ID.
     *
     * @return The grade ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the grade ID.
     *
     * @param id The new grade ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the grade.
     *
     * @return The grade name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the grade.
     *
     * @param name The new grade name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the total fees of the grade.
     *
     * @return The total fees
     */
    public double getTotalFees() {
        return totalFees;
    }

    /**
     * Sets the total fees of the grade.
     *
     * @param totalFees The new total fees to set
     */
    public void setTotalFees(double totalFees) {
        this.totalFees = totalFees;
    }

    @Override
    public String toString() {
        return name; // Useful for ComboBox displays
    }
}
