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

    private Message() {
    }

    public Message(String recipientCell, String messageText, int messageNumber) {
        this(createMessageID(), messageNumber, recipientCell, messageText);
    }

    public Message(String messageID, int messageNumber, String recipientCell, String messageText) {
        this.messageID = messageID == null ? "" : messageID;
        this.messageNumber = messageNumber;
        this.recipientCell = recipientCell == null ? "" : recipientCell;
        this.messageText = messageText == null ? "" : messageText;
        this.messageHash = createMessageHash();
    }

    public boolean checkMessageID() {
        return MESSAGE_ID_PATTERN.matcher(messageID).matches();
    }

    public String checkRecipientCell() {
        return RECIPIENT_PATTERN.matcher(recipientCell).matches()
                ? "Cell phone number successfully captured."
                : "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.";
    }

    public String validateMessage() {
        if (messageText.length() <= 250) {
            return "Message ready to send.";
        }
        return "Message exceeds 250 characters by " + (messageText.length() - 250)
                + "; please reduce the size.";
    }

    public String createMessageHash() {
        String[] words = messageText.trim().split("\\s+");
        if (words.length == 0 || words[0].isEmpty()) {
            return messageID.length() >= 2 ? messageID.substring(0, 2) + ":" + messageNumber + ":" : "";
        }

        String firstWord = cleanWord(words[0]);
        String lastWord = cleanWord(words[words.length - 1]);
        String idPrefix = messageID.length() >= 2 ? messageID.substring(0, 2) : messageID;
        return (idPrefix + ":" + messageNumber + ":" + firstWord + lastWord).toUpperCase();
    }

    public String SentMessage() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("1) Send Message  2) Disregard Message  3) Store Message");
        return SentMessage(scanner.nextInt());
    }

    public String SentMessage(int choice) {
        return SentMessage(choice, Paths.get("messages.json"));
    }

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

    public static int returnTotalMessagess() {
        return SENT_MESSAGES.size();
    }

    public String storeMessage() {
        return storeMessage(Paths.get("messages.json"));
    }

    public String storeMessage(Path storagePath) {
        try {
            List<Message> storedMessages = new ArrayList<Message>();
            if (Files.exists(storagePath)) {
                String existingJson = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8);
                List<Message> existingMessages = GSON.fromJson(existingJson, MESSAGE_LIST_TYPE);
                if (existingMessages != null) {
                    storedMessages.addAll(existingMessages);
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

    public String formatDetails() {
        return "Message ID: " + messageID + System.lineSeparator()
                + "Message Hash: " + messageHash + System.lineSeparator()
                + "Recipient: " + recipientCell + System.lineSeparator()
                + "Message: " + messageText;
    }

    public String getMessageID() {
        return messageID;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    public String getRecipientCell() {
        return recipientCell;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    static void clearSessionMessages() {
        SENT_MESSAGES.clear();
    }

    private static String createMessageID() {
        long id = ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L);
        return Long.toString(id);
    }

    private static String cleanWord(String word) {
        return word.replaceAll("[^\\p{Alnum}]", "");
    }
}