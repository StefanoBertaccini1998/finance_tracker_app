package it.finance.sb.service;

import it.finance.sb.exception.MementoException;
import it.finance.sb.mapper.UserMapper;
import it.finance.sb.mapper.UserSnapshot;
import it.finance.sb.model.user.User;
import it.finance.sb.memento.UserMementoManager;
import it.finance.sb.logging.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Memento service.
 */
public class MementoService {

    private static final Logger logger = LoggerFactory.getSafeLogger(MementoService.class);

    /**
     * Save user.
     *
     * @param user the user
     * @throws MementoException the memento exception
     */
    public void saveUser(User user) throws MementoException {
        if (user == null) throw new IllegalArgumentException("Cannot save null user.");
        try {
            UserSnapshot snapshot = UserMapper.toSnapshot(user);
            UserMementoManager.save(snapshot);
            logger.info("[MementoService] User '" + user.getName() + "' saved successfully.");
        } catch (Exception e) {
            throw new MementoException("Could not save user. Internal error.", e);
        }
    }

    /**
     * Load user optional.
     *
     * @param username the username
     * @return the optional
     * @throws MementoException the memento exception
     */
    public Optional<User> loadUser(String username) throws MementoException {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank.");
        }

        try {
            Optional<UserSnapshot> snapshotOpt = UserMementoManager.load(username);
            return snapshotOpt.map(UserMapper::fromSnapshot);
        } catch (Exception e) {
            throw new MementoException("Failed to load user data.", e);
        }
    }

    /**
     * List users list.
     *
     * @return the list
     */
    public List<String> listUsers() {
        return UserMementoManager.listSavedUsers();
    }

    /**
     * Delete user boolean.
     *
     * @param username the username
     * @return the boolean
     */
    public boolean deleteUser(String username) {
        try {
            boolean deleted = UserMementoManager.delete(username);
            logger.info(()->"[MementoService] Deleted user '" + username + "'");
            return deleted;
        } catch (Exception e) {
            logger.log(Level.WARNING, "[MementoService] Failed to delete user '" + username + "'", e);
            return false;
        }
    }
}
