package za.ac.progpoe;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Scanner;

public final class LoginApp {
    private LoginApp() {
    }

    public static void main(String[] args) {
        run(new Scanner(System.in), System.out);
    }

    static void run(Scanner scanner, PrintStream output) {
        // Collect the details needed to register the account.
        output.println("=== Registration ===");
        output.print("First name: ");
        String firstName = scanner.nextLine();
        output.print("Last name: ");
        String lastName = scanner.nextLine();
        output.print("Username: ");
        String username = scanner.nextLine();
        output.print("Password: ");
        String password = scanner.nextLine();
        output.print("South African cell number (+27...): ");
        String cellPhoneNumber = scanner.nextLine();

        Login account = new Login(username, password, cellPhoneNumber, firstName, lastName);
        boolean validUsername = account.checkUserName();
        boolean validPassword = account.checkPasswordComplexity();
        boolean validCellPhoneNumber = account.checkCellPhoneNumber();

        printValidationResult(output, validUsername, "Username successfully captured.",
            "Username is not correctly formatted, please ensure that your username contains an underscore and is no more than five characters in length.");
        printValidationResult(output, validPassword, "Password successfully captured.",
            "Password is not correctly formatted; please ensure that the password contains at least eight characters, a capital letter, a number, and a special character.");
        printValidationResult(output, validCellPhoneNumber, "Cell number successfully captured.",
            "Cell phone number is incorrectly formatted or does not contain an international code.");

        // Do not start the login flow while any registration field is invalid.
        if (!validUsername || !validPassword || !validCellPhoneNumber) {
            output.println("Registration could not be completed. Please restart and correct the details.");
            return;
        }

        // Registration succeeded, so prompt for credentials to authenticate.
        output.println(account.registerUser());
        output.println("=== Login ===");
        output.print("Username: ");
        String enteredUsername = scanner.nextLine();
        output.print("Password: ");
        String enteredPassword = scanner.nextLine();

        boolean loginSuccessful = account.loginUser(enteredUsername, enteredPassword);
        output.println(account.returnLoginStatus());
        QuickChatApp.run(loginSuccessful, scanner, output, Paths.get("messages.json"),
                account.getCellPhoneNumber());
    }

    private static void printValidationResult(PrintStream output, boolean valid,
            String successMessage, String failureMessage) {
        output.println(valid ? successMessage : failureMessage);
    }
}
