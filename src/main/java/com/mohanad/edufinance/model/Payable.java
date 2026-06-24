package com.mohanad.edufinance.model;

/**
 * Interface representing a payable entity in the EduFinance system.
 * It defines the contract for calculating payments based on work sessions.
 *
 * @author MOHANAD
 */
public interface Payable {

    /**
     * Calculates the total payment amount based on the number of sessions taught.
     *
     * @param sessions The number of sessions/classes conducted
     * @return The calculated payment amount as a double
     */
    double calculatePay(int sessions);
}
