import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final String ALGORITHM = "SHA-256";
    private static final String SALT = "RescueAnimal2024"; // Fixed salt for simplicity
    
    public static String encrypt(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            String saltedPassword = password + SALT;
            byte[] hashedBytes = md.digest(saltedPassword.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Password encryption failed", e);
        }
    }
    
    public static boolean verify(String password, String encryptedPassword) {
        String hashedInput = encrypt(password);
        return hashedInput.equals(encryptedPassword);
    }
}