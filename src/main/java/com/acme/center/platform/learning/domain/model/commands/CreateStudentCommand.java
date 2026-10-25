package com.acme.center.platform.learning.domain.model.commands;

/**
 * Command to create a student
 * @param firstName the student's first name.
 *                  Cannot be null or blank
 * @param lastName the student last name.
 *                 Cannot be null or blank
 * @param email the student email.
 *              Cannot be null or blank
 * @param street the student street.
 *               Cannot be null or blank
 * @param number the student number.
 *               Cannot be null or blank
 * @param city the student city.
 *            Cannot be null or blank
 * @param postalCode the student postal code.
 *                  Cannot be null or blank
 * @param country the student country.
 *               Cannot be null or blank
 */
public record CreateStudentCommand(String firstName, String lastName, String email, String street, String number, String city, String postalCode, String country) {
    /**
     * Constructor
     * @param firstName the student's first name.
     *                  Cannot be null or blank
     * @param lastName the student last name.
     *                 Cannot be null or blank
     * @param email the student email.
     *              Cannot be null or blank
     * @param street the student street.
     *               Cannot be null or blank
     * @param number the student number.
     *               Cannot be null or blank
     * @param city the student city.
     *           Cannot be null or blank
     * @param postalCode the student postal code.
     *                 Cannot be null or blank
     * @param country the student country.
     *              Cannot be null or blank
     * @throws IllegalArgumentException if the student's firstName is null or blank
     * @throws IllegalArgumentException if the student's lastName is null or blank
     * @throws IllegalArgumentException if the student's email is null or blank
     * @throws IllegalArgumentException if the student's street is null or blank
     * @throws IllegalArgumentException if the student's number is null or blank
     * @throws IllegalArgumentException if the student's city is null or blank
     * @throws IllegalArgumentException if the student's postalCode is null or blank
     * @throws IllegalArgumentException if the student's country is null or blank
     */

    public CreateStudentCommand {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName cannot be null or blank");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName cannot be null or blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("email cannot be null or blank");
        }
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("street cannot be null or blank");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("number cannot be null or blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("city cannot be null or blank");
        }
        if (postalCode == null || postalCode.isBlank()) {
            throw new IllegalArgumentException("postalCode cannot be null or blank");
        }
        if (country == null || country.isBlank()) {
            throw new IllegalArgumentException("country cannot be null or blank");
        }
    }
}
