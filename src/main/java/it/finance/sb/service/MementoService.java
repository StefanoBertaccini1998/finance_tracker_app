package it.finance.sb.service;

import it.finance.sb.exception.MementoException;
import it.finance.sb.model.user.User;
import it.finance.sb.memento.UserMementoManager;
import it.finance.sb.logging.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class MementoService {

    private static final Logger logger = LoggerFactory.getLogger(MementoService.class);

    public void saveUser(User user) throws MementoException {
        try {
            UserMementoManager.save(user);
            logger.info("[MementoService] User '" + user.getName() + "' saved successfully.");
        } catch (Exception e) {
            logger.severe("[MementoService] Failed to save user: " + e.getMessage());
            throw new MementoException("Saving failed", e);
        }
    }

    public Optional<User> loadUser(String username) throws MementoException {
        try {
            Optional<User> result = UserMementoManager.load(username);
            if (result.isEmpty()) logger.warning("[MementoService] No user found with name: " + username);
            return result;
        } catch (Exception e) {
            logger.severe("[MementoService] Error loading user '" + username + "': " + e.getMessage());
            throw new MementoException("Load failed", e);
        }
    }

    public List<String> listUsers() {
        return UserMementoManager.listSavedUsers();
    }

    public boolean deleteUser(String username) {
        try {
            boolean deleted = UserMementoManager.delete(username);
            logger.info("[MementoService] Deleted user '" + username + "': " + deleted);
            return deleted;
        } catch (Exception e) {
            logger.warning("[MementoService] Failed to delete user '" + username + "': " + e.getMessage());
            return false;
        }
    }
}
