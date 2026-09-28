package za.ac.progpoe;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginTest {
    // Cover accepted and rejected username formats.
    @Test
    public void acceptsUsernameWithUnderscoreUpToFiveCharacters() {
        Login login = validLogin();
        assertTrue(login.checkUserName());
    }

    // Verify all password complexity requirements.
    @Test
    public void rejectsUsernameWithoutUnderscoreOrOverFiveCharacters() {
        assertFalse(new Login("kyle!!!!!!!", "Ch&sec@ke99!", "+27838968976", "Ada", "Lovelace")
                .checkUserName());
        assertFalse(new Login("kyle1", "Ch&sec@ke99!", "+27838968976", "Ada", "Lovelace")
                .checkUserName());
    }

    // Check the required South African international phone format.
    @Test
    public void acceptsPasswordMeetingAllComplexityRules() {
        assertTrue(validLogin().checkPasswordComplexity());
    }

    // Confirm registration messages distinguish each validation failure.
    @Test
    public void rejectsPasswordMissingComplexityRequirements() {
        assertFalse(new Login("kyl_1", "password", "+27838968976", "Ada", "Lovelace")
                .checkPasswordComplexity());
    }

    // Login is unavailable before registration and requires exact credentials.
    @Test
    public void acceptsSouthAfricanInternationalCellNumber() {
        assertTrue(validLogin().checkCellPhoneNumber());
    }

    @Test
    public void rejectsCellNumberWithoutInternationalCodeOrValidLength() {
        assertFalse(new Login("kyl_1", "Ch&sec@ke99!", "08966553", "Ada", "Lovelace")
                .checkCellPhoneNumber());
    }

    @Test
    public void registersValidAccountAndReturnsSpecificFailureMessages() {
        assertEquals("User registered successfully.", validLogin().registerUser());
        assertEquals(
                "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.",
                new Login("kyle!!!!!!!", "Ch&sec@ke99!", "+27838968976", "Ada", "Lovelace")
                        .registerUser());
        assertEquals(
                "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.",
                new Login("kyl_1", "password", "+27838968976", "Ada", "Lovelace")
                        .registerUser());
        assertEquals(
                "Cell phone number is incorrectly formatted or does not contain an international code.",
                new Login("kyl_1", "Ch&sec@ke99!", "08966553", "Ada", "Lovelace")
                        .registerUser());
    }

    @Test
    public void logsInOnlyAfterSuccessfulRegistration() {
        Login login = validLogin();
        assertFalse(login.loginUser("kyl_1", "Ch&sec@ke99!"));
        assertEquals("User registered successfully.", login.registerUser());
        assertTrue(login.loginUser("kyl_1", "Ch&sec@ke99!"));
        assertEquals("Welcome Ada, Lovelace it is great to see you.", login.returnLoginStatus());
    }

    @Test
    public void rejectsIncorrectLoginCredentials() {
        Login login = validLogin();
        login.registerUser();
        assertFalse(login.loginUser("kyl_1", "wrong-password"));
        assertEquals("Username or password incorrect, please try again.", login.returnLoginStatus());
    }

    private Login validLogin() {
        return new Login("kyl_1", "Ch&sec@ke99!", "+27838968976", "Ada", "Lovelace");
    }
}
