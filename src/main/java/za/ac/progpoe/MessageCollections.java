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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/**
 * Keeps message status collections and their ID/hash indexes in sync.
 */
public final class MessageCollections {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MESSAGE_LIST_TYPE = new TypeToken<List<Message>>() { }.getType();

    private final Path storagePath;
    private final List<Message> sentMessages = new ArrayList<Message>();
    private final List<Message> disregardedMessages = new ArrayList<Message>();
    private final List<Message> storedMessages = new ArrayList<Message>();
    private final List<String> messageHashes = new ArrayList<String>();
    private final List<String> messageIDs = new ArrayList<String>();

    public MessageCollections(Path storagePath) {
        if (storagePath == null) {
            throw new IllegalArgumentException("A stored-message file path is required.");
        }
        this.storagePath = storagePath;
    }

    /**
     * Loads the stored-message JSON file and indexes each loaded message.
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
        List<Message> validatedMessages = new ArrayList<Message>();
        for (Message message : loadedMessages) {
            if (message == null) {
                throw new JsonParseException("Stored-message JSON must not contain null entries.");
            }
            validatedMessages.add(message);
        }
        storedMessages.clear();
        storedMessages.addAll(validatedMessages);
        rebuildIndexes();
    }

    /**
     * Adds a message to the selected status and persists stored messages.
     */
    public void addMessage(Message message, Status status) throws IOException {
        if (message == null || status == null) {
            throw new IllegalArgumentException("A message and status are required.");
        }

        if (status == Status.STORED) {
            List<Message> updatedMessages = new ArrayList<Message>(storedMessages);
            updatedMessages.add(message);
            writeStoredMessages(updatedMessages);
            storedMessages.add(message);
        } else if (status == Status.SENT) {
            sentMessages.add(message);
        } else {
            disregardedMessages.add(message);
        }
        indexMessage(message);
    }

    /**
     * Returns an immutable snapshot of the sent messages.
     */
    public List<Message> getSentMessages() {
        return immutableCopy(sentMessages);
    }

    /**
     * Returns an immutable snapshot of the disregarded messages.
     */
    public List<Message> getDisregardedMessages() {
        return immutableCopy(disregardedMessages);
    }

    /**
     * Returns an immutable snapshot of the stored messages.
     */
    public List<Message> getStoredMessages() {
        return immutableCopy(storedMessages);
    }

    /**
     * Returns an immutable snapshot of all indexed message hashes.
     */
    public List<String> getMessageHashes() {
        return immutableCopy(messageHashes);
    }

    /**
     * Returns an immutable snapshot of all indexed message IDs.
     */
    public List<String> getMessageIDs() {
        return immutableCopy(messageIDs);
    }

    /**
     * Finds a message in any status collection by its ID.
     */
    public Optional<Message> findMessageByID(String messageID) {
        for (Message message : allMessages()) {
            if (message.getMessageID().equals(messageID)) {
                return Optional.of(message);
            }
        }
        return Optional.empty();
    }

    /**
     * Finds every stored message addressed to the requested recipient.
     */
    public List<Message> findStoredMessagesByRecipient(String recipient) {
        List<Message> matches = new ArrayList<Message>();
        for (Message message : storedMessages) {
            if (message.getRecipientCell().equals(recipient)) {
                matches.add(message);
            }
        }
        return immutableCopy(matches);
    }

    /**
     * Returns the longest stored message, if the collection is not empty.
     */
    public Optional<Message> getLongestStoredMessage() {
        Message longest = null;
        for (Message message : storedMessages) {
            if (longest == null
                    || message.getMessageText().length() > longest.getMessageText().length()) {
                longest = message;
            }
        }
        return Optional.ofNullable(longest);
    }

    /**
     * Deletes every matching hash from status collections and their indexes.
     */
    public boolean deleteMessageByHash(String messageHash) throws IOException {
        if (messageHash == null || !messageHashes.contains(messageHash)) {
            return false;
        }

        List<Message> updatedStoredMessages = new ArrayList<Message>();
        for (Message message : storedMessages) {
            if (!message.getMessageHash().equals(messageHash)) {
                updatedStoredMessages.add(message);
            }
        }
        if (updatedStoredMessages.size() != storedMessages.size()) {
            writeStoredMessages(updatedStoredMessages);
        }

        removeByHash(sentMessages, messageHash);
        Message.removeSentMessagesByHash(messageHash);
        removeByHash(disregardedMessages, messageHash);
        removeByHash(storedMessages, messageHash);
        rebuildIndexes();
        return true;
    }

    /**
     * Formats every stored message with its full report details.
     */
    public String formatStoredMessageReport() {
        if (storedMessages.isEmpty()) {
            return "No stored messages are available.";
        }

        StringBuilder report = new StringBuilder("=== Stored Messages Report ===");
        for (Message message : storedMessages) {
            report.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(message.formatDetails());
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

    private void indexMessage(Message message) {
        messageHashes.add(message.getMessageHash());
        messageIDs.add(message.getMessageID());
    }

    private void rebuildIndexes() {
        messageHashes.clear();
        messageIDs.clear();
        for (Message message : allMessages()) {
            indexMessage(message);
        }
    }

    private List<Message> allMessages() {
        List<Message> messages = new ArrayList<Message>(sentMessages);
        messages.addAll(disregardedMessages);
        messages.addAll(storedMessages);
        return messages;
    }

    private static void removeByHash(List<Message> messages, String messageHash) {
        Iterator<Message> messageIterator = messages.iterator();
        while (messageIterator.hasNext()) {
            if (messageIterator.next().getMessageHash().equals(messageHash)) {
                messageIterator.remove();
            }
        }
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
