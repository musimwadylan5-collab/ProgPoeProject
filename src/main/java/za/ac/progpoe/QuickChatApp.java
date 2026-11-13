package za.ac.progpoe;

import java.io.PrintStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import com.google.gson.JsonParseException;

final class QuickChatApp {
    private QuickChatApp() {
    }

    static void run(boolean authenticated, Scanner scanner, PrintStream output) {
        run(authenticated, scanner, output, Paths.get("messages.json"));
    }

    static void run(boolean authenticated, Scanner scanner, PrintStream output, Path storagePath) {
        run(authenticated, scanner, output, storagePath, "Not recorded");
    }

    static void run(boolean authenticated, Scanner scanner, PrintStream output,
            Path storagePath, String senderCell) {
        if (!authenticated) {
            return;
        }

        Message.clearSessionMessages();
        MessageCollections messages = new MessageCollections(storagePath);
        try {
            messages.loadStoredMessages();
        } catch (IOException | JsonParseException exception) {
            output.println("Unable to load stored messages: " + exception.getMessage());
            return;
        }

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
            output.println("4) Stored Messages");
            Integer option = readInteger(scanner, output, "Choose an option (1-4): ");
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
                    Message message = readMessage(scanner, output, messageNumber, senderCell);
                    if (message == null) {
                        output.println("Input ended. Exiting QuickChat.");
                        return;
                    }
                    sendOrStoreMessage(scanner, output, message, messages);
                    messagesEntered++;
                }
                output.println("Total messages sent: " + Message.returnTotalMessagess());
            } else if (option == 2) {
                output.println("Coming Soon.");
            } else if (option == 3) {
                output.println("Goodbye from QuickChat.");
                return;
            } else if (option == 4) {
                runStoredMessagesMenu(scanner, output, messages);
            } else {
                output.println("Invalid menu option. Please choose 1, 2, 3, or 4.");
            }
        }
    }

    private static Message readMessage(Scanner scanner, PrintStream output, int messageNumber,
            String senderCell) {
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

        Message message = new Message(recipient, messageText, messageNumber, senderCell);
        output.println("Message ID generated: " + message.getMessageID());
        return message;
    }

    private static void sendOrStoreMessage(Scanner scanner, PrintStream output,
            Message message, MessageCollections messages) {
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
            try {
                if (choice == 1) {
                    messages.addMessage(message, MessageCollections.Status.SENT);
                    output.println(message.SentMessage(choice));
                    output.println(message.formatDetails());
                } else if (choice == 2) {
                    messages.addMessage(message, MessageCollections.Status.DISREGARDED);
                    output.println(message.SentMessage(choice));
                } else {
                    messages.addMessage(message, MessageCollections.Status.STORED);
                    output.println("Message successfully stored.");
                }
            } catch (IOException exception) {
                output.println("Unable to save the message: " + exception.getMessage());
            }
            return;
        }
    }

    private static void runStoredMessagesMenu(Scanner scanner, PrintStream output,
            MessageCollections messages) {
        while (true) {
            output.println("=== Stored Messages ===");
            output.println("1) Display senders and recipients");
            output.println("2) Display longest stored message");
            output.println("3) Search by message ID");
            output.println("4) Search by recipient");
            output.println("5) Delete by message hash");
            output.println("6) Display full stored-message report");
            output.println("0) Return to main menu");
            Integer option = readInteger(scanner, output, "Choose an option (0-6): ");
            if (option == null) {
                output.println("Input ended. Returning to QuickChat.");
                return;
            }

            if (option == 0) {
                return;
            } else if (option == 1) {
                displaySendersAndRecipients(output, messages);
            } else if (option == 2) {
                displayLongestMessage(output, messages);
            } else if (option == 3) {
                searchStoredMessageByID(scanner, output, messages);
            } else if (option == 4) {
                searchStoredMessagesByRecipient(scanner, output, messages);
            } else if (option == 5) {
                deleteMessageByHash(scanner, output, messages);
            } else if (option == 6) {
                output.println(messages.formatStoredMessageReport());
            } else {
                output.println("Invalid stored-message option. Please choose 0 through 6.");
            }
        }
    }

    private static void displaySendersAndRecipients(PrintStream output, MessageCollections messages) {
        if (messages.getStoredMessages().isEmpty()) {
            output.println("No stored messages are available.");
            return;
        }
        for (Message message : messages.getStoredMessages()) {
            output.println("Sender: " + message.getSenderCell()
                    + " | Recipient: " + message.getRecipientCell());
        }
    }

    private static void displayLongestMessage(PrintStream output, MessageCollections messages) {
        Optional<Message> longestMessage = messages.getLongestStoredMessage();
        if (!longestMessage.isPresent()) {
            output.println("No stored messages are available.");
            return;
        }
        Message message = longestMessage.get();
        output.println("Longest stored message:");
        output.println(message.formatDetails());
    }

    private static void searchStoredMessageByID(Scanner scanner, PrintStream output,
            MessageCollections messages) {
        String messageID = readLine(scanner, output, "Message ID: ");
        if (messageID == null) {
            output.println("Input ended. Returning to the stored-message menu.");
            return;
        }
        Optional<Message> match = messages.findMessageByID(messageID.trim());
        if (!match.isPresent()) {
            output.println("No message found with ID " + messageID.trim() + ".");
            return;
        }
        Message message = match.get();
        output.println("Recipient: " + message.getRecipientCell());
        output.println("Message: " + message.getMessageText());
    }

    private static void searchStoredMessagesByRecipient(Scanner scanner, PrintStream output,
            MessageCollections messages) {
        String recipient = readLine(scanner, output, "Recipient cell number: ");
        if (recipient == null) {
            output.println("Input ended. Returning to the stored-message menu.");
            return;
        }
        List<Message> matches = messages.findStoredMessagesByRecipient(recipient.trim());
        if (matches.isEmpty()) {
            output.println("No stored messages found for recipient " + recipient.trim() + ".");
            return;
        }
        for (Message message : matches) {
            output.println(message.formatDetails());
        }
    }

    private static void deleteMessageByHash(Scanner scanner, PrintStream output,
            MessageCollections messages) {
        String messageHash = readLine(scanner, output, "Message hash: ");
        if (messageHash == null) {
            output.println("Input ended. Returning to the stored-message menu.");
            return;
        }
        try {
            if (messages.deleteMessageByHash(messageHash.trim())) {
                output.println("Message deleted successfully.");
            } else {
                output.println("No message found with hash " + messageHash.trim() + ".");
            }
        } catch (IOException exception) {
            output.println("Unable to delete the message: " + exception.getMessage());
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
