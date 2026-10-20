package za.ac.progpoe;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MessageTest {
    @After
    public void clearSentMessages() {
        Message.clearSessionMessages();
    }

    @Test
    public void createsTenDigitMessageID() {
        Message message = new Message("+27718693002", "Hi tonight", 0);
        assertTrue(message.checkMessageID());
        assertEquals(10, message.getMessageID().length());
    }

    @Test
    public void rejectsMessageIDLongerThanTenDigits() {
        assertFalse(new Message("12345678901", 0, "+27718693002", "Hi").checkMessageID());
    }

    @Test
    public void validatesRecipientInternationalNumber() {
        assertEquals("Cell phone number successfully captured.",
                new Message("0012345678", 0, "+27718693002", "Hi").checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                new Message("0012345678", 0, "08575975889", "Hi").checkRecipientCell());
        assertEquals("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.",
                new Message("0012345678", 0, "+2771869300", "Hi").checkRecipientCell());
    }

    @Test
    public void validatesMessageLengthAtAndOverLimit() {
        assertEquals("Message ready to send.",
                new Message("0012345678", 0, "+27718693002", repeat('a', 250)).validateMessage());
        assertEquals("Message exceeds 250 characters by 3; please reduce the size.",
                new Message("0012345678", 0, "+27718693002", repeat('a', 253)).validateMessage());
    }

    @Test
    public void createsExpectedMessageHash() {
        Message message = new Message("0012345678", 0, "+27718693002",
                "Hi Mike, can you join us for dinner tonight?");
        assertEquals("00:0:HITONIGHT", message.createMessageHash());
    }

    @Test
    public void generatesMessageHashForEachMessageNumber() {
        for (int number = 0; number < 3; number++) {
            Message message = new Message("0012345678", number, "+27718693002", "Hi thanks");
            assertEquals("00:" + number + ":HITHANKS", message.createMessageHash());
        }
    }

    @Test
    public void sendAndDisregardReturnRequiredMessagesAndCountOnlySent() {
        Message sentMessage = new Message("0012345678", 0, "+27718693002", "Hi tonight");
        Message discardedMessage = new Message("0012345679", 1, "+27718693002", "Not sent");

        assertEquals("Message successfully sent.", sentMessage.SentMessage(1));
        assertEquals("Press 0 to delete the message.", discardedMessage.SentMessage(2));
        assertEquals(1, Message.returnTotalMessagess());
        assertTrue(sentMessage.printMessages().contains("Message ID: 0012345678"));
    }

    @Test
    public void storesMessageAsJson() throws IOException {
        Path jsonFile = Files.createTempFile("quickchat-messages", ".json");
        try {
            Files.delete(jsonFile);
            Message message = new Message("0012345678", 0, "+27718693002", "Hi tonight");
            assertEquals("Message successfully stored.", message.SentMessage(3, jsonFile));

            String json = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8);
            Type messageListType = new TypeToken<List<Message>>() { }.getType();
            List<Message> storedMessages = new Gson().fromJson(json, messageListType);
            assertEquals(1, storedMessages.size());
            assertEquals("Hi tonight", storedMessages.get(0).getMessageText());
        } finally {
            Files.deleteIfExists(jsonFile);
        }
    }

    @Test
    public void appendsStoredMessagesToExistingJsonFile() throws IOException {
        Path jsonFile = Files.createTempFile("quickchat-messages", ".json");
        try {
            Message firstMessage = new Message("0012345678", 0, "+27718693002", "First message");
            Message secondMessage = new Message("0012345679", 1, "+27718693002", "Second message");
            assertEquals("Message successfully stored.", firstMessage.storeMessage(jsonFile));
            assertEquals("Message successfully stored.", secondMessage.storeMessage(jsonFile));

            String json = new String(Files.readAllBytes(jsonFile), StandardCharsets.UTF_8);
            Type messageListType = new TypeToken<List<Message>>() { }.getType();
            List<Message> storedMessages = new Gson().fromJson(json, messageListType);
            assertEquals(2, storedMessages.size());
            assertEquals("First message", storedMessages.get(0).getMessageText());
            assertEquals("Second message", storedMessages.get(1).getMessageText());
        } finally {
            Files.deleteIfExists(jsonFile);
        }
    }

    private static String repeat(char character, int count) {
        StringBuilder value = new StringBuilder();
        for (int index = 0; index < count; index++) {
            value.append(character);
        }
        return value.toString();
    }
}