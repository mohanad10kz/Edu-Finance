package com.mohanad.edufinance.model;

/**
 * Concrete class representing a Student in the system.
 * Inherits common attributes from the {@link Person} class, demonstrating Inheritance.
 *
 * @author MOHANAD
 */
public class Student extends Person {

    private int gradeId;
    private double paidAmount;

    /**
     * Default constructor.
     */
    public Student() {
        super();
    }

    /**
     * Parameterized constructor to create a Student with ID, name, grade, and paid amount.
     *
     * @param id         The student's unique ID
     * @param name       The student's name
     * @param gradeId    The ID of the grade/class the student is enrolled in
     * @param paidAmount The total amount already paid by the student
     */
    public Student(int id, String name, int gradeId, double paidAmount) {
        super(id, name);
        this.gradeId = gradeId;
        this.paidAmount = paidAmount;
    }

    /**
     * Gets the ID of the grade the student belongs to.
     *
     * @return The grade ID
     */
    public int getGradeId() {
        return gradeId;
    }

    /**
     * Sets the ID of the grade the student belongs to.
     *
     * @param gradeId The new grade ID to set
     */
    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    /**
     * Gets the total amount paid by the student.
     *
     * @return The paid amount
     */
    public double getPaidAmount() {
        return paidAmount;
    }

    /**
     * Sets the total amount paid by the student.
     *
     * @param paidAmount The new paid amount to set
     */
    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", gradeId=" + gradeId +
                ", paidAmount=" + paidAmount +
                '}';
    }
}
