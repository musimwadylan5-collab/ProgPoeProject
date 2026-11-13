package za.ac.progpoe;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * Represents one QuickChat message and its generated identifier and hash.
 */
public final class Message {
    private static final Pattern MESSAGE_ID_PATTERN = Pattern.compile("\\d{1,10}");
    private static final Pattern RECIPIENT_PATTERN = Pattern.compile("\\+27[6-8]\\d{8}");
    private static final List<Message> SENT_MESSAGES = new ArrayList<Message>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MESSAGE_LIST_TYPE = new TypeToken<List<Message>>() { }.getType();

    private String messageID;
    private int messageNumber;
    private String recipientCell;
    private String messageText;
    private String messageHash;
    private String senderCell;

    private Message() {
    }

    /**
     * Creates a new message with a generated ID and no recorded sender.
     */
    public Message(String recipientCell, String messageText, int messageNumber) {
        this(createMessageID(), messageNumber, recipientCell, messageText, "Not recorded");
    }

    /**
     * Creates a new message with a generated ID and the logged-in sender.
     */
    public Message(String recipientCell, String messageText, int messageNumber,
            String senderCell) {
        this(createMessageID(), messageNumber, recipientCell, messageText, senderCell);
    }

    /**
     * Creates a message with an explicit ID, useful for loading and deterministic tests.
     */
    public Message(String messageID, int messageNumber, String recipientCell, String messageText) {
        this(messageID, messageNumber, recipientCell, messageText, "Not recorded");
    }

    /**
     * Creates a message with explicit identifying and sender details.
     */
    public Message(String messageID, int messageNumber, String recipientCell,
            String messageText, String senderCell) {
        this.messageID = messageID == null ? "" : messageID;
        this.messageNumber = messageNumber;
        this.recipientCell = recipientCell == null ? "" : recipientCell;
        this.messageText = messageText == null ? "" : messageText;
        this.messageHash = createMessageHash();
        this.senderCell = senderCell == null || senderCell.trim().isEmpty()
                ? "Not recorded" : senderCell;
    }

    /** Checks whether the message ID contains between one and ten digits. */
    public boolean checkMessageID() {
        return MESSAGE_ID_PATTERN.matcher(messageID).matches();
    }

    /** Validates the recipient against the application's international mobile format. */
    public String checkRecipientCell() {
        return RECIPIENT_PATTERN.matcher(recipientCell).matches()
                ? "Cell phone number successfully captured."
                : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    /** Returns the result of the 250-character message-length check. */
    public String validateMessage() {
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (messageText.length() - 250)
                + "; please reduce the size.";
    }

    /** Builds the assignment's message hash from the ID prefix, number, and boundary words. */
    public String createMessageHash() {
        String text = getMessageText();
        String id = getMessageID();
        String[] words = text.trim().split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) {
            return id.length() >= 2 ? id.substring(0, 2) + ":" + messageNumber + ":" : "";
        }

        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        String idPrefix = id.length() >= 2 ? id.substring(0, 2) : id;
        return (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    /** Prompts on the console for the legacy send/discard/store action. */
    public String SentMessage() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1) Send Message  2) Disregard Message  3) Store Message");
        return SentMessage(scanner.nextInt());
    }

    /** Applies a legacy send/discard/store action without changing persistence paths. */
    public String SentMessage(int choice) {
        return SentMessage(choice, Paths.get("messages.json"));
    }

    /** Applies the selected action using the supplied JSON storage location. */
    public String SentMessage(int choice, Path storagePath) {
        if (choice == 1) {
            SENT_MESSAGES.add(this);
            return "Message successfully sent.";
        }
        if (choice == 2) {
            return "Press 0 to delete the message.";
        }
        if (choice == 3) {
            return storeMessage(storagePath);
        }
        return "Invalid message option.";
    }

    /** Formats the session's sent messages for console display. */
    public String printMessages() {
        StringBuilder output = new StringBuilder();
        for (Message message : SENT_MESSAGES) {
            if (output.length() > 0) {
                output.append(System.lineSeparator());
            }
            output.append(message.formatDetails());
        }
        return output.toString();
    }

    /** Returns the number of messages marked as sent during the current session. */
    public static int returnTotalMessagess() {
        return SENT_MESSAGES.size();
    }

    /** Persists this message to the default JSON file. */
    public String storeMessage() {
        return storeMessage(Paths.get("messages.json"));
    }

    /** Appends this message to the JSON message array at the supplied path. */
    public String storeMessage(Path storagePath) {
        try {
            List<Message> storedMessages = new ArrayList<Message>();
            if (Files.exists(storagePath)) {
                String existingJson = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8);
                if (!existingJson.trim().isEmpty()) {
                    List<Message> existingMessages = GSON.fromJson(existingJson, MESSAGE_LIST_TYPE);
                    if (existingMessages != null) {
                        storedMessages.addAll(existingMessages);
                    }
                }
            }
            storedMessages.add(this);
            Path parent = storagePath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(storagePath, GSON.toJson(storedMessages).getBytes(StandardCharsets.UTF_8));
            return "Message successfully stored.";
        } catch (IOException | JsonParseException exception) {
            return "Unable to store message.";
        }
    }

    /** Returns all identifying, sender, recipient, and text details for this message. */
    public String formatDetails() {
        return "Message ID: " + getMessageID() + System.lineSeparator()
                + "Message Hash: " + getMessageHash() + System.lineSeparator()
                + "Sender: " + getSenderCell() + System.lineSeparator()
                + "Recipient: " + getRecipientCell() + System.lineSeparator()
                + "Message: " + getMessageText();
    }

    /** Returns the message ID. */
    public String getMessageID() {
        return messageID == null ? "" : messageID;
    }

    /** Returns the zero-based number assigned to the message. */
    public int getMessageNumber() {
        return messageNumber;
    }

    /** Returns the recipient cellphone number. */
    public String getRecipientCell() {
        return recipientCell == null ? "" : recipientCell;
    }

    /** Returns the message text. */
    public String getMessageText() {
        return messageText == null ? "" : messageText;
    }

    /** Returns the generated hash, recomputing it for older JSON entries if needed. */
    public String getMessageHash() {
        return messageHash == null ? createMessageHash() : messageHash;
    }

    /** Returns the sender cellphone number or the legacy-data placeholder. */
    public String getSenderCell() {
        return senderCell == null || senderCell.trim().isEmpty()
                ? "Not recorded" : senderCell;
    }

    static void clearSessionMessages() {
        SENT_MESSAGES.clear();
    }

    static void removeSentMessagesByHash(String messageHash) {
        SENT_MESSAGES.removeIf(message -> message.getMessageHash().equals(messageHash));
    }

    private static String createMessageID() {
        long id = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return Long.toString(id);
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^\\p{Alnum}]", "");
    }
}