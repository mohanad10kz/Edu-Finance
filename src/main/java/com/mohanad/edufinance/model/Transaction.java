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
    private Integer sessions; // Number of sessions (only for HOURLY teacher payroll)
    private String monthName; // Name of month (only for FIXED teacher payroll)

    // Transient fields for table display
    private String personName;
    private String payType;

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

    /**
     * Gets the number of sessions for HOURLY teacher payroll.
     *
     * @return The number of sessions
     */
    public Integer getSessions() {
        return sessions;
    }

    /**
     * Sets the number of sessions for HOURLY teacher payroll.
     *
     * @param sessions The number of sessions to set
     */
    public void setSessions(Integer sessions) {
        this.sessions = sessions;
    }

    /**
     * Gets the month name for FIXED teacher payroll.
     *
     * @return The month name
     */
    public String getMonthName() {
        return monthName;
    }

    /**
     * Sets the month name for FIXED teacher payroll.
     *
     * @param monthName The month name to set
     */
    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    /**
     * Gets the name of the teacher/student associated with the transaction.
     *
     * @return The person name
     */
    public String getPersonName() {
        return personName;
    }

    /**
     * Sets the name of the teacher/student associated with the transaction.
     *
     * @param personName The person name to set
     */
    public void setPersonName(String personName) {
        this.personName = personName;
    }

    /**
     * Gets the payment type display (transient field).
     *
     * @return The pay type
     */
    public String getPayType() {
        return payType;
    }

    /**
     * Sets the payment type display (transient field).
     *
     * @param payType The pay type to set
     */
    public void setPayType(String payType) {
        this.payType = payType;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", personId=" + personId +
                ", sessions=" + sessions +
                ", monthName='" + monthName + '\'' +
                ", personName='" + personName + '\'' +
                ", payType='" + payType + '\'' +
                '}';
    }
}
