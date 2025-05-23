package it.finance.sb.memento;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.mapper.UserSnapshot;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The type User memento manager.
 */
public class UserMementoManager {
    private static final String SAVE_DIR = "saved_users";
    private static final ObjectMapper mapper;
    private static final Logger logger = LoggerFactory.getInstance().getLogger(UserMementoManager.class);

    static {
        mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) logger.info("[UserMementoManager] Created directory: " + SAVE_DIR);
        }
    }

    /**
     * Save.
     *
     * @param snapshot the snapshot
     * @throws IOException the io exception
     */
    public static void save(UserSnapshot snapshot) throws IOException {
        Objects.requireNonNull(snapshot, "UserSnapshot cannot be null.");
        String filename = sanitizeFileName(snapshot.name()) + ".json";
        Path filePath = Path.of(SAVE_DIR, filename);
        mapper.writeValue(filePath.toFile(), snapshot);
        logger.info("[UserMementoManager] Saved snapshot: " + filePath);
    }

    /**
     * Load optional.
     *
     * @param username the username
     * @return the optional
     * @throws IOException the io exception
     */
    public static Optional<UserSnapshot> load(String username) throws IOException {
        String filename = sanitizeFileName(username) + ".json";
        Path filePath = Path.of(SAVE_DIR, filename);
        File file = filePath.toFile();
        if (!file.exists()) {
            logger.warning("[UserMementoManager] File not found: " + filePath);
            return Optional.empty();
        }
        logger.info("[UserMementoManager] Loading snapshot: " + filePath);
        return Optional.of(mapper.readValue(file, UserSnapshot.class));
    }

    /**
     * List saved users list.
     *
     * @return the list
     */
    public static List<String> listSavedUsers() {
        File dir = new File(SAVE_DIR);
        String[] files = dir.list((d, name) -> name.endsWith(".json"));
        if (files == null) return List.of();

        return Arrays.stream(files)
                .map(name -> name.replace(".json", ""))
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
        String filename = sanitizeFileName(username) + ".json";
        File file = new File(SAVE_DIR, filename);
        boolean deleted = file.exists() && file.delete();
        if (deleted) {
            logger.info("[UserMementoManager] Deleted snapshot: " + filename);
        } else {
            logger.warning("[UserMementoManager] Failed to delete or not found: " + filename);
        }
        return deleted;
    }

    private static String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_"); // avoid bad file names
    }

}
