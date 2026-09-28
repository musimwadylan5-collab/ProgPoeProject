# ProgPoeProject

A Java console application for registering an account and verifying login details. It uses the `Login` class to validate a username, password, and South African mobile number before allowing authentication.

## Requirements

- JDK 8 or later
- Apache Maven 3.6 or later

## Run

From this directory, compile and launch the console application with:

```text
mvn package
java -cp target/classes za.ac.progpoe.LoginApp
```

The application asks for first and last name, username, password, and cellphone number, then prompts for login credentials if registration succeeds.

## Validation rules

- Username contains `_` and is at most five characters, as required by the assignment.
- Password is at least eight characters and contains an uppercase letter, a digit, and a non-whitespace special character.
- Cellphone number uses South African international format: `+27` followed by a valid mobile prefix (`6`, `7`, or `8`) and eight digits. The expression is defined in `Login.java` and follows Java's `Pattern` regular-expression syntax.
- Credentials are checked only after registration succeeds.

The username and phone constraints use the supplied assignment test examples (`kyl_1`, `+27838968976`, and `08966553`).

## Tests

Run the JUnit 4 suite with:

```text
mvn test
```

Tests cover valid and invalid inputs, registration messages, successful authentication, and failed authentication.
