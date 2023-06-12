package genesis.crypto.wallet.performance;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CryptoWalletPerformanceTest {
    @Test
    public void main_ShouldPrintCorrectOutput() {
        // Redirect standard output to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        // Call the main method
        CryptoWalletPerformance.main(null);

        // Capture the output
        String output = outputStream.toString().trim();

        // Verify if the output matches the expected result
        String expectedOutput = "total=16984.62,best_asset=BTC,best_performance=1.51,worst_asset=ETH,worst_performance=1.01";
        assertTrue(output.contains(expectedOutput));
    }
}
