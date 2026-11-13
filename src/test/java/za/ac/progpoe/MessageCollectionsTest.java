package za.ac.progpoe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MessageCollectionsTest {
    private static final String LONG_STORED_TEXT =
            "Where are you? You are late! I have asked you to be on time.";
    private static final String SECOND_STORED_HASH = "00:1:WHERETIME";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private MessageCollections messages;
    private Message secondRecord;
    private Path storagePath;

    @Before
    public void populateCollectionsFromTheRequiredRecords() throws Exception {
        Message.clearSessionMessages();
        storagePath = temporaryFolder.newFile("messages.json").toPath();
        messages = new MessageCollections(storagePath);
        Message firstRecord = new Message("0000000001", 0, "+27834557896",
                "Did you get the cake?", "Test sender");
        secondRecord = new Message("0000000002", 1, "+27838884567",
                LONG_STORED_TEXT, "Test sender");
        Message thirdRecord = new Message("0000000003", 2, "+27834484567",
                "Yohoooo, I am at your gate.", "Test sender");
        Message fourthRecord = new Message("0000000004", 3, "0838884567",
                "It is dinner time !", "Test sender");
        Message fifthRecord = new Message("0000000005", 4, "+27838884567",
                "Ok, I am leaving without you.", "Test sender");

        messages.addMessage(firstRecord, MessageCollections.Status.SENT);
        messages.addMessage(secondRecord, MessageCollections.Status.STORED);
        messages.addMessage(thirdRecord, MessageCollections.Status.DISREGARDED);
        messages.addMessage(fourthRecord, MessageCollections.Status.SENT);
        messages.addMessage(fifthRecord, MessageCollections.Status.STORED);
    }

    @Test
    public void sentMessagesContainBothRequiredSentRecords() {
        List<String> sentTexts = new ArrayList<String>();
        for (Message message : messages.getSentMessages()) {
            sentTexts.add(message.getMessageText());
        }
        assertEquals(Arrays.asList("Did you get the cake?", "It is dinner time !"), sentTexts);
        assertEquals(1, messages.getDisregardedMessages().size());
        assertEquals(2, messages.getStoredMessages().size());
        assertEquals(5, messages.getMessageHashes().size());
        assertEquals(5, messages.getMessageIDs().size());
    }

    @Test
    public void longestStoredMessageMatchesTheRequiredRecord() {
        assertEquals(LONG_STORED_TEXT,
                messages.getLongestStoredMessage().get().getMessageText());
    }

    @Test
    public void findsTheFourthRequiredRecordByItsMessageID() {
        assertEquals("It is dinner time !",
                messages.findMessageByID("0000000004")
                        .map(Message::getMessageText).orElse(""));
    }

    @Test
    public void recipientSearchReturnsBothStoredRecords() {
        List<String> storedTexts = new ArrayList<String>();
        for (Message message : messages.findStoredMessagesByRecipient("+27838884567")) {
            storedTexts.add(message.getMessageText());
        }
        assertEquals(Arrays.asList(
                LONG_STORED_TEXT, "Ok, I am leaving without you."), storedTexts);
    }

    @Test
    public void deletingTheSecondRecordRemovesItFromCollectionsAndJSON() throws Exception {
        assertEquals(true, messages.deleteMessageByHash(SECOND_STORED_HASH));
        assertFalse(messages.getStoredMessages().contains(secondRecord));
        assertFalse(messages.getMessageHashes().contains(SECOND_STORED_HASH));
        assertFalse(messages.getMessageIDs().contains("0000000002"));
        assertEquals(1, messages.getStoredMessages().size());

        MessageCollections reloadedMessages = new MessageCollections(storagePath);
        reloadedMessages.loadStoredMessages();
        assertEquals(1, reloadedMessages.getStoredMessages().size());
        assertEquals("Ok, I am leaving without you.",
                reloadedMessages.getStoredMessages().get(0).getMessageText());
    }

    @Test
    public void deletingASentMessageAlsoUpdatesTheLegacySentMessageCount() throws Exception {
        Message sentMessage = messages.getSentMessages().get(0);
        sentMessage.SentMessage(1);
        assertEquals(1, Message.returnTotalMessagess());

        assertEquals(true, messages.deleteMessageByHash(sentMessage.getMessageHash()));
        assertEquals(0, Message.returnTotalMessagess());
        assertFalse(messages.getMessageIDs().contains(sentMessage.getMessageID()));
        assertEquals(1, messages.getSentMessages().size());
    }

    @Test
    public void fullReportIncludesHashRecipientAndMessageForEveryStoredRecord() {
        String report = messages.formatStoredMessageReport();
        assertEquals(true, report.contains(SECOND_STORED_HASH));
        assertEquals(true, report.contains("Recipient: +27838884567"));
        assertEquals(true, report.contains(LONG_STORED_TEXT));
        assertEquals(true, report.contains("00:4:OKYOU"));
        assertEquals(true, report.contains("Ok, I am leaving without you."));
    }

    @Test
    public void storedMessageMenuDisplaysSearchResultsAndHandlesUnknownEntries() throws Exception {
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(capturedOutput, true, "UTF-8");
        try {
            String input = "0\n4\n1\n3\n0000000002\n4\n+27838884567\n"
                    + "5\nUNKNOWN-HASH\n0\n3\n";
            QuickChatApp.run(true, new Scanner(input), output, storagePath, "0838884567");
        } finally {
            output.close();
        }

        String result = capturedOutput.toString("UTF-8");
        assertTrue(result.contains("Sender: Test sender | Recipient: +27838884567"));
        assertTrue(result.contains("Recipient: +27838884567" + System.lineSeparator()
                + "Message: " + LONG_STORED_TEXT));
        assertTrue(result.contains("No message found with hash UNKNOWN-HASH."));
        assertTrue(result.contains("Message: Ok, I am leaving without you."));
    }

    @Test
    public void emptyStoredMessagesAndUnknownIDsAreReportedWithoutFailure() throws Exception {
        Path emptyFile = temporaryFolder.newFile("empty.json").toPath();
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(capturedOutput, true, "UTF-8");
        try {
            QuickChatApp.run(true, new Scanner("0\n4\n1\n2\n3\nmissing\n4\n+27000000000\n0\n3\n"),
                    output, emptyFile, "0838884567");
        } finally {
            output.close();
        }

        String result = capturedOutput.toString("UTF-8");
        assertTrue(result.contains("No stored messages are available."));
        assertTrue(result.contains("No message found with ID missing."));
        assertTrue(result.contains("No stored messages found for recipient +27000000000."));
    }

    @Test
    public void startupReadsStoredMessagesFromJson() throws Exception {
        MessageCollections loadedMessages = new MessageCollections(storagePath);
        loadedMessages.loadStoredMessages();
        assertEquals(2, loadedMessages.getStoredMessages().size());
        assertEquals(LONG_STORED_TEXT, loadedMessages.getLongestStoredMessage().get()
                .getMessageText());
        assertEquals(2, loadedMessages.getMessageHashes().size());
        assertEquals(2, loadedMessages.getMessageIDs().size());
    }
}
