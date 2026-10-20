# ProgPoeProject

A Java console-only application for registering an account, verifying login details, and using the QuickChat message menu after successful login. All user interaction is through text prompts in the terminal; the application does not use GUIs or `JOptionPane`. The `Login` class validates a username, password, and South African mobile number before allowing authentication.

## Requirements

- JDK 8 or later
- Apache Maven 3.6 or later

## Run

From this directory, launch the console application with Maven:

```text
mvn exec:java
```

The application asks for first and last name, username, password, and cellphone number, then prompts for login credentials if registration succeeds. After a successful login, QuickChat asks how many messages to enter and displays a menu for sending messages, viewing the in-development recent-messages feature, or quitting.

To open the project in Apache NetBeans, select **File > Open Project** and choose this repository's root folder (the folder containing `pom.xml`). NetBeans recognizes it as a Maven project. Run `za.ac.progpoe.LoginApp` as the main class.

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
Message tests also cover the 250-character limit, international recipient validation, generated IDs and hashes, send/discard/store choices, JSON persistence, menu flows, and the login gate. JSON serialization is provided by Google's Gson library, declared in `pom.xml`.

The GitHub Actions workflow at `.github/workflows/TestJava.yml` runs the Maven test suite on pushes and pull requests.

## References

Apache Maven Project, n.d. Maven Getting Started Guide. [online] Available at: <https://maven.apache.org/guides/getting-started/> [Accessed 20 October 2026].

GitHub, n.d. Building and testing Java with Maven. [online] Available at: <https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven> [Accessed 20 October 2026].

Google, 2024. Gson User Guide. [online] Available at: <https://github.com/google/gson/blob/main/UserGuide.md> [Accessed 20 October 2026].

Independent Communications Authority of South Africa (ICASA), 2016. Numbering Plan Regulations. [online] Available at: <https://www.icasa.org.za> [Accessed 28 September 2026].

JUnit, n.d. JUnit 4.13.2 API Documentation. [online] Available at: <https://junit.org/junit4/javadoc/4.13.2/> [Accessed 20 October 2026].

Oracle, 2024. Java SE 8 Pattern API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html> [Accessed 28 September 2026].

Oracle, n.d. Java SE 8 Files API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html> [Accessed 20 October 2026].

Oracle, n.d. Java SE 8 Scanner API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html> [Accessed 20 October 2026].
