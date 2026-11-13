# ProgPoeProject

A Java console-only application for registering an account, verifying login details, and using QuickChat after successful login. All user interaction is through text prompts in the terminal; the application does not use GUIs or `JOptionPane`. The `Login` class validates a username, password, and South African mobile number before allowing authentication.

## Requirements

- JDK 8 or later
- Apache Maven 3.6 or later

## Run

From this directory, launch the console application with Maven:

```text
mvn exec:java
```

The application asks for first and last name, username, password, and cellphone number, then prompts for login credentials if registration succeeds. After a successful login, QuickChat asks how many messages to enter and displays a menu to send messages, view the recent-messages placeholder, quit, or open **Stored Messages**. The stored-message menu displays sender/recipient details, finds the longest message, searches messages by ID or recipient, deletes by hash, and prints a full report.

The checked-in `messages.json` file supplies two example stored messages. Stored messages created in the console are appended to that file; deleting a stored message updates both the in-memory collections and the JSON file. Run the application from the repository root so its relative `messages.json` path resolves to this file.

The stored-message manager dynamically maintains the Sent Messages, Disregarded Messages, Stored Messages, Message Hashes, and Message IDs arrays. Stored-message IDs, hashes, senders, recipients, and text are kept in parallel arrays so searching, reporting, and deletion use the matching record at each index. It reads JSON as UTF-8 and uses Gson's `TypeToken<List<Message>>` generic-type pattern to deserialize the JSON array. Imported messages without sender data are displayed with **Not recorded**; messages sent after login record the registered cellphone number as the sender.

To open and run the project in Apache NetBeans:

1. Select **File > Open Project** and choose the repository root (the folder containing `pom.xml`). NetBeans imports it as a Maven project.
2. Set the project JDK to JDK 8 or later.
3. Use **Run Project** (or run the Maven goal `exec:java`). The Maven exec plugin is configured to launch `za.ac.progpoe.LoginApp` from the repository root, so `messages.json` resolves to the checked-in sample file.
4. Enter responses in the NetBeans Output console. All registration, login, and messaging interaction uses terminal input/output; no GUI or dialog is opened.

The same Maven configuration can be run from a terminal with `mvn exec:java`.

## Validation rules

- Username contains `_` and is at most five characters, as required by the assignment.
- Password is at least eight characters and contains an uppercase letter, a digit, and a non-whitespace special character.
- Cellphone number uses South African international format: `+27` followed by a valid mobile prefix (`6`, `7`, or `8`) and eight digits. The expression is defined in `Login.java` and follows Java's `Pattern` regular-expression syntax.
- Credentials are checked only after registration succeeds.

The username and phone constraints use the supplied assignment test examples (`kyl_1`, `+27838968976`, and `08966553`).

## Tests

Run all unit tests with:

```text
mvn --batch-mode test
```

Tests cover registration and login, message validation, generated IDs and hashes, send/discard/store flows, JSON persistence and console display, all five required Part 3 records, longest-message and recipient searches, ID/hash searches, deletion/index synchronization, full report content, and empty/unknown search cases.

The fourth required sample uses the provided ID `0000000004` in the deterministic tests. Its value `0838884567` is treated as the sample recipient because the brief labels it “recipient/developer value”; interactive recipient validation remains the existing international `+27` format.

## GitHub Actions

The GitHub Actions workflow at `.github/workflows/TestJava.yml` checks out the project, configures Temurin JDK 17 with Maven dependency caching, and runs the full Maven verification/test suite on every push and pull request. A failing test causes the workflow to fail.

## References

Apache Maven Project, n.d. Maven Getting Started Guide. [online] Available at: <https://maven.apache.org/guides/getting-started/> [Accessed 20 October 2026].

Apache NetBeans, n.d. Working with Maven. [online] Available at: <https://netbeans.apache.org/tutorial/main/kb/docs/java/maven-quickstart/> [Accessed 13 November 2026]. Reference for opening and running this Maven project in NetBeans.

GitHub, n.d. Building and testing Java with Maven. [online] Available at: <https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven> [Accessed 20 October 2026].

Google, 2024. Gson User Guide, including deserialization into generic types using `TypeToken`. [online] Available at: <https://github.com/google/gson/blob/main/UserGuide.md> [Accessed 13 November 2026]. The stored-message JSON reader follows this generic collection approach.

Independent Communications Authority of South Africa (ICASA), 2016. Numbering Plan Regulations. [online] Available at: <https://www.icasa.org.za> [Accessed 28 September 2026].

JUnit, n.d. JUnit 4.13.2 API Documentation. [online] Available at: <https://junit.org/junit4/javadoc/4.13.2/> [Accessed 13 November 2026].

Oracle, 2024. Java SE 8 Pattern API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html> [Accessed 28 September 2026].

Oracle, 2014. Java SE 8 Arrays API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/Arrays.html> [Accessed 13 November 2026]. Reference for copying and searching the dynamically sized message arrays.

Oracle, n.d. Java SE 8 Files API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html> [Accessed 20 October 2026].

Oracle, n.d. Java SE 8 Optional API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html> [Accessed 13 November 2026]. Reference for safely representing optional message-search results.

Oracle, n.d. Java SE 8 Scanner API. [online] Available at: <https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html> [Accessed 20 October 2026].
