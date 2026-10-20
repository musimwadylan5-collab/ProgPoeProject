package za.ac.progpoe;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

final class QuickChatApp {
    private QuickChatApp() {
    }

    static void run(boolean authenticated, Scanner scanner, PrintStream output) {
        run(authenticated, scanner, output, Paths.get("messages.json"));
    }

    static void run(boolean authenticated, Scanner scanner, PrintStream output, Path storagePath) {
        if (!authenticated) {
            return;
        }

        Message.clearSessionMessages();
        output.println("Welcome to QuickChat.");
        Integer requestedMessages = readNonNegativeInteger(
                scanner, output, "How many messages would you like to enter? ");
        if (requestedMessages == null) {
            output.println("Input ended. Exiting QuickChat.");
            return;
        }

        int messagesEntered = 0;
        while (true) {
            output.println("1) Send Messages");
            output.println("2) Show recently sent messages");
            output.println("3) Quit");
            Integer option = readInteger(scanner, output, "Choose an option (1-3): ");
            if (option == null) {
                output.println("Input ended. Exiting QuickChat.");
                return;
            }

            if (option == 1) {
                if (messagesEntered == requestedMessages) {
                    output.println("All requested message entries have been used.");
                    continue;
                }
                for (int messageNumber = messagesEntered;
                        messageNumber < requestedMessages; messageNumber++) {
                    Message message = readMessage(scanner, output, messageNumber);
                    if (message == null) {
                        output.println("Input ended. Exiting QuickChat.");
                        return;
                    }
                    sendOrStoreMessage(scanner, output, message, storagePath);
                    messagesEntered++;
                }
                output.println("Total messages sent: " + Message.returnTotalMessagess());
            } else if (option == 2) {
                output.println("Coming Soon.");
            } else if (option == 3) {
                output.println("Goodbye from QuickChat.");
                return;
            } else {
                output.println("Invalid menu option. Please choose 1, 2, or 3.");
            }
        }
    }

    private static Message readMessage(Scanner scanner, PrintStream output, int messageNumber) {
        String recipient;
        while (true) {
            recipient = readLine(scanner, output, "Recipient cell number (+27...): ");
            if (recipient == null) {
                return null;
            }
            Message recipientCheck = new Message("0000000000", messageNumber, recipient, "");
            String recipientResult = recipientCheck.checkRecipientCell();
            if ("Cell phone number successfully captured.".equals(recipientResult)) {
                break;
            }
            output.println(recipientResult);
        }

        String messageText;
        while (true) {
            messageText = readLine(scanner, output, "Message (up to 250 characters): ");
            if (messageText == null) {
                return null;
            }
            Message messageCheck = new Message("0000000000", messageNumber, recipient, messageText);
            String validationResult = messageCheck.validateMessage();
            if ("Message ready to send.".equals(validationResult)) {
                break;
            }
            output.println(validationResult);
        }

        Message message = new Message(recipient, messageText, messageNumber);
        output.println("Message ID generated: " + message.getMessageID());
        return message;
    }

    private static void sendOrStoreMessage(Scanner scanner, PrintStream output,
            Message message, Path storagePath) {
        while (true) {
            output.println("1) Send Message  2) Disregard Message  3) Store Message");
            Integer choice = readInteger(scanner, output, "Choose an option (1-3): ");
            if (choice == null) {
                return;
            }
            if (choice < 1 || choice > 3) {
                output.println("Invalid message option.");
                continue;
            }
            output.println(message.SentMessage(choice, storagePath));
            if (choice == 1) {
                output.println(message.formatDetails());
            }
            return;
        }
    }

    private static Integer readNonNegativeInteger(Scanner scanner, PrintStream output, String prompt) {
        while (true) {
            Integer value = readInteger(scanner, output, prompt);
            if (value == null || value >= 0) {
                return value;
            }
            output.println("Please enter zero or a positive whole number.");
        }
    }

    private static Integer readInteger(Scanner scanner, PrintStream output, String prompt) {
        while (true) {
            output.print(prompt);
            String value = readLine(scanner);
            if (value == null) {
                return null;
            }
            try {
                return Integer.valueOf(value.trim());
            } catch (NumberFormatException exception) {
                output.println("Please enter a whole number.");
            }
        }
    }

    private static String readLine(Scanner scanner, PrintStream output, String prompt) {
        output.print(prompt);
        return readLine(scanner);
    }

    private static String readLine(Scanner scanner) {
        return scanner.hasNextLine() ? scanner.nextLine() : null;
    }
}
