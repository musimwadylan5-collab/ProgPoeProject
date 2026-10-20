package za.ac.progpoe;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginAppTest {
    @Test
    public void successfulLoginStartsQuickChatMenu() throws Exception {
        String input = "Ada\nLovelace\nkyl_1\nCh&sec@ke99!\n+27838968976\n"
                + "kyl_1\nCh&sec@ke99!\n0\n3\n";

        String output = runLoginApp(input);

        assertTrue(output.contains("User registered successfully."));
        assertTrue(output.contains("Welcome Ada, Lovelace it is great to see you."));
        assertTrue(output.contains("Welcome to QuickChat."));
        assertTrue(output.contains("1) Send Messages"));
        assertTrue(output.contains("2) Show recently sent messages"));
        assertTrue(output.contains("3) Quit"));
    }

    @Test
    public void failedLoginDoesNotStartQuickChat() throws Exception {
        String input = "Ada\nLovelace\nkyl_1\nCh&sec@ke99!\n+27838968976\n"
                + "kyl_1\nwrong-password\n";

        String output = runLoginApp(input);

        assertTrue(output.contains("Username or password incorrect, please try again."));
        assertFalse(output.contains("Welcome to QuickChat."));
    }

    private String runLoginApp(String input) throws Exception {
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream output = new PrintStream(capturedOutput, true, "UTF-8");
        try {
            LoginApp.run(new Scanner(input), output);
        } finally {
            output.close();
        }
        return capturedOutput.toString("UTF-8");
    }
}
