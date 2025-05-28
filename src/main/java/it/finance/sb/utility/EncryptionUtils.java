package it.finance.sb.utility;

import it.finance.sb.logging.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for AES encryption and decryption with GCM mode.
 * Provides methods to encrypt and decrypt strings using a securely stored key.
 * Includes username binding for simple integrity validation.
 */
public class EncryptionUtils {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String ALGORITHM = "AES";

    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int AES_KEY_SIZE = 16; // 128 bits
    private static final int IV_SIZE = 16;
    private static final Path DEFAULT_KEY_PATH = Path.of(".secure_keys", "user.key");

    private static final Logger logger = LoggerFactory.getSafeLogger(EncryptionUtils.class);


    private EncryptionUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves or generates a secret key stored at DEFAULT_KEY_PATH.
     * Logs relevant actions and errors.
     *
     * @return a SecretKeySpec for AES encryption
     * @throws GeneralSecurityException if any I/O or security issue occurs
     */
    private static SecretKeySpec getOrCreateSecretKey() throws GeneralSecurityException {
        try {
            if (!Files.exists(DEFAULT_KEY_PATH)) {
                logger.info("Key file not found. Generating new AES key.");
                String key = generateRandomKey();
                Files.createDirectories(DEFAULT_KEY_PATH.getParent());
                Files.writeString(DEFAULT_KEY_PATH, key, StandardOpenOption.CREATE_NEW);
                logger.info("AES key generated and stored at: " + DEFAULT_KEY_PATH);
            }

            String rawKey = Files.readString(DEFAULT_KEY_PATH).trim();
            if (rawKey.length() != AES_KEY_SIZE) {
                logger.severe("Invalid AES key length found in file.");
                throw new GeneralSecurityException("Invalid key length in user.key");
            }

            return new SecretKeySpec(rawKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to retrieve or store encryption key", e);
        }
    }

    /**
     * Generates a random 16-byte AES key encoded in Base64.
     *
     * @return a base64-encoded AES key truncated to 16 characters
     */
    private static String generateRandomKey() {
        byte[] keyBytes = new byte[AES_KEY_SIZE];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes).substring(0, AES_KEY_SIZE);
    }

    /**
     * Encrypts plain text using AES-GCM.
     * Appends username as a prefix to ensure data binding.
     *
     * @param plainText the data to encrypt
     * @param username the associated username to bind with the encrypted content
     * @return a Base64-encoded encrypted string
     * @throws GeneralSecurityException if encryption fails
     */
    public static String encrypt(String plainText, String username) throws GeneralSecurityException {
        SecretKeySpec key = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        String content = username + ":" + plainText;
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);

        logger.fine(()->"Content encrypted for user: " + username);
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypts encrypted Base64-encoded string and validates the embedded username.
     *
     * @param encryptedText the encrypted string
     * @param expectedUsername the username expected to be embedded
     * @return the decrypted original content (excluding username)
     * @throws GeneralSecurityException if decryption fails or integrity check fails
     */
    public static String decrypt(String encryptedText, String expectedUsername) throws GeneralSecurityException {
        SecretKeySpec key = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] combined = Base64.getDecoder().decode(encryptedText);
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_SIZE);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_SIZE, combined.length);

        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        String result = new String(decrypted, StandardCharsets.UTF_8);
        if (!result.startsWith(expectedUsername + ":")) {
            logger.warning(()->"Username mismatch during decryption for user: " + expectedUsername);
            throw new GeneralSecurityException("Username mismatch or corrupted data");
        }

        logger.fine(()->"Decryption successful for user: " + expectedUsername);
        return result.substring(expectedUsername.length() + 1);
    }
}
