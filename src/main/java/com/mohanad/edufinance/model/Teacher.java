package com.mohanad.edufinance.model;

/**
 * Concrete class representing a Teacher in the EduFinance system.
 * Inherits common attributes from {@link Person} (Inheritance) and implements 
 * {@link Payable} to calculate monthly earnings polymorphically (Polymorphism).
 *
 * @author MOHANAD
 */
public class Teacher extends Person implements Payable {

    private String payType; // 'FIXED' or 'HOURLY'
    private double payValue; // fixed monthly salary OR hourly session rate
    private Integer gradeId; // Optional: Grade/Class associated with the teacher
    
    // Transient field for table display
    private String gradeName;

    /**
     * Default constructor.
     */
    public Teacher() {
        super();
    }

    /**
     * Parameterized constructor.
     *
     * @param id       The teacher's unique ID
     * @param name     The teacher's name
     * @param payType  The type of payment: 'FIXED' or 'HOURLY'
     * @param payValue The salary value (fixed salary or hourly rate)
     * @param gradeId  The ID of the grade/class taught (can be null)
     */
    public Teacher(int id, String name, String payType, double payValue, Integer gradeId) {
        super(id, name);
        this.payType = payType;
        this.payValue = payValue;
        this.gradeId = gradeId;
    }

    /**
     * Gets the payment type of the teacher.
     *
     * @return The payment type: 'FIXED' or 'HOURLY'
     */
    public String getPayType() {
        return payType;
    }

    /**
     * Sets the payment type of the teacher.
     *
     * @param payType The payment type to set ('FIXED' or 'HOURLY')
     */
    public void setPayType(String payType) {
        this.payType = payType;
    }

    /**
     * Gets the salary or session rate value.
     *
     * @return The payment value
     */
    public double getPayValue() {
        return payValue;
    }

    /**
     * Sets the salary or session rate value.
     *
     * @param payValue The payment value to set
     */
    public void setPayValue(double payValue) {
        this.payValue = payValue;
    }

    /**
     * Gets the ID of the grade taught by the teacher.
     *
     * @return The grade ID (can be null)
     */
    public Integer getGradeId() {
        return gradeId;
    }

    /**
     * Sets the ID of the grade taught by the teacher.
     *
     * @param gradeId The new grade ID to set (can be null)
     */
    public void setGradeId(Integer gradeId) {
        this.gradeId = gradeId;
    }

    /**
     * Implementation of {@link Payable#calculatePay(int)}.
     * Polymorphically computes the pay based on the teacher's payType:
     * - FIXED: Returns the fixed payValue directly (sessions argument is ignored).
     * - HOURLY: Returns the product of the sessions and the hourly payValue rate.
     *
     * @param sessions The number of sessions taught during the period
     * @return The total computed salary payment
     */
    @Override
    public double calculatePay(int sessions) {
        if ("HOURLY".equalsIgnoreCase(payType)) {
            return sessions * payValue;
        } else {
            return payValue; // FIXED salary
        }
    }

    /**
     * Gets the name of the grade taught by the teacher.
     *
     * @return The grade name
     */
    public String getGradeName() {
        return gradeName;
    }

    /**
     * Sets the name of the grade taught by the teacher.
     *
     * @param gradeName The grade name to set
     */
    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", payType='" + payType + '\'' +
                ", payValue=" + payValue +
                ", gradeId=" + gradeId +
                ", gradeName='" + gradeName + '\'' +
                '}';
    }
}
