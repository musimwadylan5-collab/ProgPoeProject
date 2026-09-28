package za.ac.progpoe;

import java.util.regex.Pattern;

public final class Login {
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,}$");
    private static final Pattern CELL_PHONE_PATTERN = Pattern.compile("^\\+27[6-8]\\d{8}$");

    private final String username;
    private final String password;
    private final String cellPhoneNumber;
    private final String firstName;
    private final String lastName;
    private boolean registered;
    private boolean loginSuccessful;

    public Login(String username, String password, String cellPhoneNumber,
                 String firstName, String lastName) {
        this.username = username == null ? "" : username;
        this.password = password == null ? "" : password;
        this.cellPhoneNumber = cellPhoneNumber == null ? "" : cellPhoneNumber;
        this.firstName = firstName == null ? "" : firstName;
        this.lastName = lastName == null ? "" : lastName;
    }

    public boolean checkUserName() {
        return username.contains("_") && username.length() <= 5;
    }

    public boolean checkPasswordComplexity() {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean checkCellPhoneNumber() {
        return CELL_PHONE_PATTERN.matcher(cellPhoneNumber).matches();
    }

    public String registerUser() {
        if (!checkUserName()) {
            return "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.";
        }
        if (!checkPasswordComplexity()) {
            return "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.";
        }
        if (!checkCellPhoneNumber()) {
            return "Cell phone number is incorrectly formatted or does not contain an international code.";
        }

        registered = true;
        return "User registered successfully.";
    }

    public boolean loginUser(String enteredUsername, String enteredPassword) {
        loginSuccessful = registered
                && username.equals(enteredUsername)
                && password.equals(enteredPassword);
        return loginSuccessful;
    }

    public String returnLoginStatus() {
        if (!loginSuccessful) {
            return "Username or password incorrect, please try again.";
        }
        return String.format("Welcome %s, %s it is great to see you.", firstName, lastName);
    }
}
