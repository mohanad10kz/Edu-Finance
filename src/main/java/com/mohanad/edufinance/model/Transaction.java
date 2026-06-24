package com.mohanad.edufinance.model;

/**
 * Concrete class representing a Financial Transaction in the system.
 * Keeps records of Student Payments and Teacher Payroll distributions.
 *
 * @author MOHANAD
 */
public class Transaction {

    private int id;
    private String type; // 'STUDENT_PAYMENT' or 'TEACHER_PAYROLL'
    private double amount;
    private String date; // LocalDate formatted as a string (YYYY-MM-DD)
    private int personId; // References Student ID or Teacher ID

    /**
     * Default constructor.
     */
    public Transaction() {
    }

    /**
     * Parameterized constructor.
     *
     * @param id       The transaction ID
     * @param type     The transaction type ('STUDENT_PAYMENT' or 'TEACHER_PAYROLL')
     * @param amount   The transaction money amount
     * @param date     The date of the transaction
     * @param personId The associated student or teacher ID
     */
    public Transaction(int id, String type, double amount, String date, int personId) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.personId = personId;
    }

    /**
     * Gets the transaction ID.
     *
     * @return The transaction ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the transaction ID.
     *
     * @param id The new transaction ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the type of transaction.
     *
     * @return The transaction type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of transaction.
     *
     * @param type The new transaction type ('STUDENT_PAYMENT' or 'TEACHER_PAYROLL')
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the transaction amount.
     *
     * @return The transaction amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Sets the transaction amount.
     *
     * @param amount The new transaction amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Gets the date of the transaction.
     *
     * @return The transaction date string
     */
    public String getDate() {
        return date;
    }

    /**
     * Sets the date of the transaction.
     *
     * @param date The transaction date string
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * Gets the associated person ID (student or teacher).
     *
     * @return The person ID
     */
    public int getPersonId() {
        return personId;
    }

    /**
     * Sets the associated person ID.
     *
     * @param personId The new person ID
     */
    public void setPersonId(int personId) {
        this.personId = personId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", personId=" + personId +
                '}';
    }
}
