package za.ac.progpoe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class QuickChatAppTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @After
    public void clearSentMessages() {
        Message.clearSessionMessages();
    }

    @Test
    public void sendsDiscardsAndStoresMessagesWithIncrementingNumbers() throws Exception {
        Path storagePath = temporaryFolder.newFile("messages.json").toPath();
        String input = "4\n"
                + "1\n"
                + "+27718693002\nHi Mike, can you join us for dinner tonight?\n1\n"
                + "+27718693002\nHi Keegan, did you receive the payment?\n1\n"
                + "+27718693002\nThis message is discarded\n2\n"
                + "+27718693002\nHi stored message\n3\n"
                + "2\n3\n";

        String output = runQuickChat(input, storagePath);

        assertTrue(output.contains("Welcome to QuickChat."));
        assertTrue(output.contains("Message successfully sent."));
        assertTrue(output.contains("Press 0 to delete the message."));
        assertTrue(output.contains("Message successfully stored."));
        assertTrue(output.matches("(?s).*\\d{2}:0:HITONIGHT.*"));
        assertTrue(output.matches("(?s).*\\d{2}:1:HIPAYMENT.*"));
        assertTrue(output.contains("Total messages sent: 2"));
        assertTrue(output.contains("Coming Soon."));
        assertTrue(output.matches("(?s).*Message ID generated: \\d{10}.*"));
        assertEquals(2, Message.returnTotalMessagess());

        int detailsStart = output.indexOf("Message ID: ");
        int hashPosition = output.indexOf("Message Hash: ", detailsStart);
        int recipientPosition = output.indexOf("Recipient: ", hashPosition);
        int messagePosition = output.indexOf("Message: ", recipientPosition);
        assertTrue(detailsStart >= 0);
        assertTrue(detailsStart < hashPosition);
        assertTrue(hashPosition < recipientPosition);
        assertTrue(recipientPosition < messagePosition);

        String storedJson = new String(Files.readAllBytes(storagePath), StandardCharsets.UTF_8);
        assertTrue(storedJson.contains("Hi stored message"));
        assertFalse(storedJson.contains("Hi Keegan"));
    }

    @Test
    public void rejectsInvalidInputAndDoesNotExceedRequestedMessageCount() throws Exception {
        Path storagePath = temporaryFolder.newFile("messages.json").toPath();
        String tooLong = repeat('x', 251);
        String input = "-1\n1\n"
                + "1\n08575975889\n+27718693002\n"
                + tooLong + "\nValid message\n1\n"
                + "1\n3\n";

        String output = runQuickChat(input, storagePath);

        assertTrue(output.contains("Please enter zero or a positive whole number."));
        assertTrue(output.contains("Please correct the number and try again."));
        assertTrue(output.contains("Message exceeds 250 characters by 1; please reduce the size."));
        assertTrue(output.contains("All requested message entries have been used."));
        assertTrue(output.contains("Total messages sent: 1"));
        assertEquals(1, Message.returnTotalMessagess());
    }

    @Test
    public void doesNotStartQuickChatForUnauthenticatedUser() throws Exception {
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(capturedOutput, true, "UTF-8");
        try {
            QuickChatApp.run(false, new Scanner("0\n3\n"), output,
                    temporaryFolder.newFile("messages.json").toPath());
        } finally {
            output.close();
        }

        assertEquals("", capturedOutput.toString("UTF-8"));
    }

    private String runQuickChat(String input, Path storagePath) throws Exception {
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(capturedOutput, true, "UTF-8");
        try {
            QuickChatApp.run(true, new Scanner(input), output, storagePath);
        } finally {
            output.close();
        }
        return capturedOutput.toString("UTF-8");
    }

    private static String repeat(char character, int count) {
        StringBuilder value = new StringBuilder();
        for (int index = 0; index < count; index++) {
            value.append(character);
        }
        return value.toString();
    }
}
