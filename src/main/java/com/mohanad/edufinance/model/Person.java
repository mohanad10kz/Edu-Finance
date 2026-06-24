package com.mohanad.edufinance.model;

/**
 * Abstract class representing a generic Person in the EduFinance system.
 * Serves as the base class for specific roles such as Student and Teacher,
 * demonstrating the OOP concept of Abstraction.
 *
 * @author MOHANAD
 */
public abstract class Person {

    private int id;
    private String name;

    /**
     * Default constructor.
     */
    public Person() {
    }

    /**
     * Parameterized constructor to initialize a Person with an ID and name.
     *
     * @param id   The unique identifier for the person
     * @param name The full name of the person
     */
    public Person(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the unique ID of the person.
     *
     * @return The person's ID
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the unique ID of the person.
     *
     * @param id The new ID to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the full name of the person.
     *
     * @return The person's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the full name of the person.
     *
     * @param name The new name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
