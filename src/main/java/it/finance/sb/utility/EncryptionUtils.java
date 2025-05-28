package it.finance.sb.utility;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
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

public class EncryptionUtils {

    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final int AES_KEY_SIZE = 16; // 128 bits
    private static final int IV_SIZE = 16;
    private static final Path DEFAULT_KEY_PATH = Path.of(".secure_keys", "user.key");


    private EncryptionUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static SecretKeySpec getOrCreateSecretKey() throws GeneralSecurityException {
        try {
            if (!Files.exists(DEFAULT_KEY_PATH)) {
                String key = generateRandomKey();
                Files.createDirectories(DEFAULT_KEY_PATH.getParent());
                Files.writeString(DEFAULT_KEY_PATH, key, StandardOpenOption.CREATE_NEW);
            }

            String rawKey = Files.readString(DEFAULT_KEY_PATH).trim();
            if (rawKey.length() != AES_KEY_SIZE) {
                throw new GeneralSecurityException("Invalid key length in user.key");
            }

            return new SecretKeySpec(rawKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        } catch (IOException e) {
            throw new GeneralSecurityException("Failed to retrieve or store encryption key", e);
        }
    }

    private static String generateRandomKey() {
        byte[] keyBytes = new byte[AES_KEY_SIZE];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().encodeToString(keyBytes).substring(0, AES_KEY_SIZE);
    }

    public static String encrypt(String plainText, String username) throws GeneralSecurityException {
        SecretKeySpec key = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);

        String content = username + ":" + plainText;
        byte[] encrypted = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

        byte[] combined = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, combined, IV_SIZE, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public static String decrypt(String encryptedText, String expectedUsername) throws GeneralSecurityException {
        SecretKeySpec key = getOrCreateSecretKey();
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);

        byte[] combined = Base64.getDecoder().decode(encryptedText);
        byte[] iv = Arrays.copyOfRange(combined, 0, IV_SIZE);
        byte[] encrypted = Arrays.copyOfRange(combined, IV_SIZE, combined.length);

        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] decrypted = cipher.doFinal(encrypted);

        String result = new String(decrypted, StandardCharsets.UTF_8);
        if (!result.startsWith(expectedUsername + ":")) {
            throw new GeneralSecurityException("Username mismatch or corrupted data");
        }

        return result.substring(expectedUsername.length() + 1);
    }
}
