
import org.json.JSONObject;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

public class ShamirSecret {

    // Function to convert string in given base to BigInteger decimal
    private static BigInteger convertToDecimal(String value, int base) {
        return new BigInteger(value, base);
    }

    // Function to perform Lagrange interpolation at x=0 (mod p)
    private static BigInteger lagrangeInterpolationAtZero(BigInteger[] x, BigInteger[] y, int k, BigInteger p) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < k; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < k; j++) {
                if (i != j) {
                    numerator = numerator.multiply(x[j].negate()).mod(p);
                    denominator = denominator.multiply(x[i].subtract(x[j])).mod(p);
                }
            }

            // modular inverse of denominator
            BigInteger inv = denominator.modInverse(p);
            BigInteger term = y[i].multiply(numerator).mod(p).multiply(inv).mod(p);

            result = result.add(term).mod(p);
        }

        return result.mod(p);
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java ShamirSecret <json-file-path>");
            return;
        }

        try {
            // Read JSON file content
            String jsonString = new String(Files.readAllBytes(Paths.get(args[0])));
            JSONObject obj = new JSONObject(jsonString);

            int n = obj.getJSONObject("keys").getInt("n");
            int k = obj.getJSONObject("keys").getInt("k");

            BigInteger[] x = new BigInteger[k];
            BigInteger[] y = new BigInteger[k];

            // Pick first k points
            for (int i = 1; i <= k; i++) {
                JSONObject point = obj.getJSONObject(String.valueOf(i));
                int base = Integer.parseInt(point.getString("base"));
                String value = point.getString("value");

                x[i - 1] = BigInteger.valueOf(i); // use JSON key as x
                y[i - 1] = convertToDecimal(value, base);
            }

            // Choose modulus (prime > max(y))
            BigInteger maxY = BigInteger.ZERO;
            for (BigInteger yi : y) {
                if (yi.compareTo(maxY) > 0) maxY = yi;
            }
            BigInteger p = maxY.nextProbablePrime();

            BigInteger secret = lagrangeInterpolationAtZero(x, y, k, p);
            System.out.println("Secret (c) = " + secret);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

