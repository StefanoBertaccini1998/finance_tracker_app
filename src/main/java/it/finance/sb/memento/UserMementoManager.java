package it.finance.sb.memento;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.mapper.UserSnapshot;
import it.finance.sb.utility.EncryptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The type User memento manager.
 */
public class UserMementoManager {
    public static final String JSON = ".json";
    private static final String SAVE_DIR = "saved_users";
    private static final ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getSafeLogger(UserMementoManager.class);

    static {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) logger.info("Created directory: " + SAVE_DIR);
        }
    }

    private UserMementoManager() {
        throw new IllegalStateException("Memento Manager class");
    }

    /**
     * Save.
     *
     * @param snapshot the snapshot
     * @throws IOException the io exception
     */
    public static void save(UserSnapshot snapshot) throws IOException {
        Objects.requireNonNull(snapshot, "UserSnapshot cannot be null.");
        String filename = sanitizeFileName(snapshot.name()) + JSON;
        Path filePath = Path.of(SAVE_DIR, filename);

        String json = mapper.writeValueAsString(snapshot);
        try {
            String encrypted = EncryptionUtils.encrypt(json,snapshot.name());
            Files.writeString(filePath, encrypted);
            logger.info(() -> "Encrypted and saved snapshot: " + filePath);
        } catch (GeneralSecurityException e) {
            throw new IOException("Encryption failed", e);
        }
    }


    /**
     * Load optional.
     *
     * @param username the username
     * @return the optional
     * @throws IOException the io exception
     */
    public static Optional<UserSnapshot> load(String username) throws IOException {
        String filename = sanitizeFileName(username) + JSON;
        Path filePath = Path.of(SAVE_DIR, filename);
        File file = filePath.toFile();

        if (!file.exists()) {
            logger.warning(() -> "File not found: " + filePath);
            return Optional.empty();
        }

        try {
            String encrypted = Files.readString(filePath);
            String decrypted = EncryptionUtils.decrypt(encrypted,username);
            logger.info(() -> "Decrypted and loaded snapshot: " + filePath);
            return Optional.of(mapper.readValue(decrypted, UserSnapshot.class));
        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption failed", e);
        }
    }

    /**
     * List saved users list.
     *
     * @return the list
     */
    public static List<String> listSavedUsers() {
        File dir = new File(SAVE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(JSON));
        if (files == null) return List.of();

        return Arrays.stream(files)
                .map(name -> name.replace(JSON, ""))
                .sorted()
                .toList();
    }

    /**
     * Delete boolean.
     *
     * @param username the username
     * @return the boolean
     */
    public static boolean delete(String username) {
        String filename = sanitizeFileName(username) + JSON;
        File file = new File(SAVE_DIR, filename);
        boolean deleted = !(!file.exists() || !file.delete());
        if (deleted) {
            logger.info(()->"Deleted snapshot: " + filename);
        } else {
            logger.warning(()->"Failed to delete or not found: " + filename);
        }
        return deleted;
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_"); // avoid bad file names
    }

}
