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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Maintains message status arrays and aligned stored-message detail arrays.
 */
public final class MessageCollections {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MESSAGE_LIST_TYPE = new TypeToken<List<Message>>() { }.getType();

    private final Path storagePath;
    private Message[] sentMessages = new Message[0];
    private Message[] disregardedMessages = new Message[0];
    private Message[] storedMessages = new Message[0];
    private String[] storedMessageIDs = new String[0];
    private String[] storedMessageHashes = new String[0];
    private String[] storedMessageTexts = new String[0];
    private String[] storedRecipients = new String[0];
    private String[] storedSenders = new String[0];
    private String[] messageHashes = new String[0];
    private String[] messageIDs = new String[0];

    public MessageCollections(Path storagePath) {
        if (storagePath == null) {
            throw new IllegalArgumentException("A stored-message file path is required.");
        }
        this.storagePath = storagePath;
    }

    /**
     * Reads the stored-message JSON file and populates aligned arrays.
     */
    public void loadStoredMessages() throws IOException {
        if (!Files.exists(storagePath)) {
            return;
        }

        String json = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8);
        if (json.trim().isEmpty()) {
            return;
        }

        // Gson's List<T> parsing follows the Gson User Guide's generic collection example.
        List<Message> loadedMessages = GSON.fromJson(json, MESSAGE_LIST_TYPE);
        if (loadedMessages == null) {
            return;
        }
        for (Message message : loadedMessages) {
            if (message == null) {
                throw new JsonParseException("Stored-message JSON must not contain null entries.");
            }
        }
        storedMessages = loadedMessages.toArray(new Message[loadedMessages.size()]);
        rebuildStoredDetails();
        rebuildIndexes();
    }

    /**
     * Adds a message to its status array and persists stored messages.
     */
    public void addMessage(Message message, Status status) throws IOException {
        if (message == null || status == null) {
            throw new IllegalArgumentException("A message and status are required.");
        }

        if (status == Status.STORED) {
            Message[] updatedStoredMessages = append(storedMessages, message);
            writeStoredMessages(Arrays.asList(updatedStoredMessages));
            storedMessages = updatedStoredMessages;
            rebuildStoredDetails();
        } else if (status == Status.SENT) {
            sentMessages = append(sentMessages, message);
        } else {
            disregardedMessages = append(disregardedMessages, message);
        }
        rebuildIndexes();
    }

    /** Returns an immutable snapshot of the sent-message array. */
    public List<Message> getSentMessages() {
        return immutableCopy(Arrays.asList(sentMessages));
    }

    /** Returns an immutable snapshot of the disregarded-message array. */
    public List<Message> getDisregardedMessages() {
        return immutableCopy(Arrays.asList(disregardedMessages));
    }

    /** Returns an immutable snapshot of the stored-message array. */
    public List<Message> getStoredMessages() {
        return immutableCopy(Arrays.asList(storedMessages));
    }

    /** Returns an immutable snapshot of the message-hash array. */
    public List<String> getMessageHashes() {
        return immutableCopy(Arrays.asList(messageHashes));
    }

    /** Returns an immutable snapshot of the message-ID array. */
    public List<String> getMessageIDs() {
        return immutableCopy(Arrays.asList(messageIDs));
    }

    /**
     * Searches message IDs across the parallel status arrays.
     */
    public Optional<Message> findMessageByID(String messageID) {
        Message[] messages = allMessages();
        for (int index = 0; index < messageIDs.length; index++) {
            if (messageIDs[index].equals(messageID)) {
                return Optional.of(messages[index]);
            }
        }
        return Optional.empty();
    }

    /**
     * Searches aligned stored recipient and message arrays.
     */
    public List<Message> findStoredMessagesByRecipient(String recipient) {
        List<Message> matches = new ArrayList<Message>();
        for (int index = 0; index < storedRecipients.length; index++) {
            if (storedRecipients[index].equals(recipient)) {
                matches.add(storedMessages[index]);
            }
        }
        return immutableCopy(matches);
    }

    /**
     * Finds the longest stored text by scanning the aligned stored-message arrays.
     */
    public Optional<Message> getLongestStoredMessage() {
        if (storedMessageTexts.length == 0) {
            return Optional.empty();
        }

        int longestIndex = 0;
        for (int index = 1; index < storedMessageTexts.length; index++) {
            if (storedMessageTexts[index].length()
                    > storedMessageTexts[longestIndex].length()) {
                longestIndex = index;
            }
        }
        return Optional.of(storedMessages[longestIndex]);
    }

    /**
     * Removes matching hashes and the corresponding values from every array.
     */
    public boolean deleteMessageByHash(String messageHash) throws IOException {
        if (messageHash == null || !contains(messageHashes, messageHash)) {
            return false;
        }

        Message[] remainingStoredMessages = withoutHash(storedMessages, messageHash);
        if (remainingStoredMessages.length != storedMessages.length) {
            writeStoredMessages(Arrays.asList(remainingStoredMessages));
        }

        sentMessages = withoutHash(sentMessages, messageHash);
        Message.removeSentMessagesByHash(messageHash);
        disregardedMessages = withoutHash(disregardedMessages, messageHash);
        storedMessages = remainingStoredMessages;
        rebuildStoredDetails();
        rebuildIndexes();
        return true;
    }

    /**
     * Formats every stored message with its full report details.
     */
    public String formatStoredMessageReport() {
        if (storedMessages.length == 0) {
            return "No stored messages are available.";
        }

        StringBuilder report = new StringBuilder("=== Stored Messages Report ===");
        for (int index = 0; index < storedMessages.length; index++) {
            report.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append("Message ID: ").append(storedMessageIDs[index])
                    .append(System.lineSeparator())
                    .append("Message Hash: ").append(storedMessageHashes[index])
                    .append(System.lineSeparator())
                    .append("Sender: ").append(storedSenders[index])
                    .append(System.lineSeparator())
                    .append("Recipient: ").append(storedRecipients[index])
                    .append(System.lineSeparator())
                    .append("Message: ").append(storedMessageTexts[index]);
        }
        return report.toString();
    }

    private void writeStoredMessages(List<Message> messages) throws IOException {
        Path parent = storagePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(storagePath, GSON.toJson(messages).getBytes(StandardCharsets.UTF_8));
    }

    private void rebuildStoredDetails() {
        storedMessageIDs = new String[storedMessages.length];
        storedMessageHashes = new String[storedMessages.length];
        storedMessageTexts = new String[storedMessages.length];
        storedRecipients = new String[storedMessages.length];
        storedSenders = new String[storedMessages.length];
        for (int index = 0; index < storedMessages.length; index++) {
            Message message = storedMessages[index];
            storedMessageIDs[index] = message.getMessageID();
            storedMessageHashes[index] = message.getMessageHash();
            storedMessageTexts[index] = message.getMessageText();
            storedRecipients[index] = message.getRecipientCell();
            storedSenders[index] = message.getSenderCell();
        }
    }

    private void rebuildIndexes() {
        Message[] messages = allMessages();
        messageHashes = new String[messages.length];
        messageIDs = new String[messages.length];
        for (int index = 0; index < messages.length; index++) {
            messageHashes[index] = messages[index].getMessageHash();
            messageIDs[index] = messages[index].getMessageID();
        }
    }

    private Message[] allMessages() {
        Message[] messages = Arrays.copyOf(sentMessages,
                sentMessages.length + disregardedMessages.length + storedMessages.length);
        System.arraycopy(disregardedMessages, 0, messages, sentMessages.length,
                disregardedMessages.length);
        System.arraycopy(storedMessages, 0, messages,
                sentMessages.length + disregardedMessages.length, storedMessages.length);
        return messages;
    }

    private static Message[] withoutHash(Message[] messages, String messageHash) {
        List<Message> remaining = new ArrayList<Message>();
        for (Message message : messages) {
            if (!message.getMessageHash().equals(messageHash)) {
                remaining.add(message);
            }
        }
        return remaining.toArray(new Message[remaining.size()]);
    }

    private static boolean contains(String[] values, String expected) {
        for (String value : values) {
            if (value.equals(expected)) {
                return true;
            }
        }
        return false;
    }

    private static <T> T[] append(T[] values, T value) {
        T[] updated = Arrays.copyOf(values, values.length + 1);
        updated[values.length] = value;
        return updated;
    }

    private static <T> List<T> immutableCopy(List<T> values) {
        return Collections.unmodifiableList(new ArrayList<T>(values));
    }

    public enum Status {
        SENT,
        DISREGARDED,
        STORED
    }
}
