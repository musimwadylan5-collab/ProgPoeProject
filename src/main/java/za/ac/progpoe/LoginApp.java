package za.ac.progpoe;

import java.util.Scanner;

public final class LoginApp {
    private LoginApp() {
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Registration ===");
        System.out.print("First name: ");
        String firstName = scanner.nextLine();
        System.out.print("Last name: ");
        String lastName = scanner.nextLine();
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("South African cell number (+27...): ");
        String cellPhoneNumber = scanner.nextLine();

        Login account = new Login(username, password, cellPhoneNumber, firstName, lastName);
        boolean validUsername = account.checkUserName();
        boolean validPassword = account.checkPasswordComplexity();
        boolean validCellPhoneNumber = account.checkCellPhoneNumber();

        printValidationResult(validUsername, "Username successfully captured.",
            "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
        printValidationResult(validPassword, "Password successfully captured.",
            "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
        printValidationResult(validCellPhoneNumber, "Cell number successfully captured.",
            "Cell phone number is incorrectly formatted or does not contain an international code.");

        if (!validUsername || !validPassword || !validCellPhoneNumber) {
            System.out.println("Registration could not be completed. Please restart and correct the details.");
            return;
        }

        System.out.println(account.registerUser());
        System.out.println("=== Login ===");
        System.out.print("Username: ");
        String enteredUsername = scanner.nextLine();
        System.out.print("Password: ");
        String enteredPassword = scanner.nextLine();

        account.loginUser(enteredUsername, enteredPassword);
        System.out.println(account.returnLoginStatus());
    }

    private static void printValidationResult(boolean valid, String successMessage, String failureMessage) {
        System.out.println(valid ? successMessage : failureMessage);
    }
}
