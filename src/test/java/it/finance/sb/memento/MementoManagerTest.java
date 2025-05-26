package it.finance.sb.memento;

import it.finance.sb.mapper.UserMapper;
import it.finance.sb.mapper.UserSnapshot;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MementoManagerTest {

    private static final String TEST_NAME = "TestUserMemento";

    @Test
    @Order(1)
    void testSaveAndLoadUser() throws Exception {
        User user = new User(TEST_NAME, 25, Gender.MALE);
        user.addCategory("FOOD");

        UserMementoManager.save(UserMapper.toSnapshot(user));

        Optional<UserSnapshot> loaded = UserMementoManager.load(TEST_NAME);
        assertTrue(loaded.isPresent());

        User loadedUser = UserMapper.fromSnapshot(loaded.get());
        assertEquals(user.getName(), loadedUser.getName());
        assertEquals(user.getAge(), loadedUser.getAge());
        assertEquals(user.getGender(), loadedUser.getGender());
        assertTrue(loadedUser.isCategoryAllowed("FOOD"));
    }

    @Test
    @Order(2)
    void testListUsersContainsSaved() {
        assertTrue(UserMementoManager.listSavedUsers().contains(TEST_NAME));
    }

    @Test
    @Order(3)
    void testDeleteUser() throws IOException {
        assertTrue(UserMementoManager.delete(TEST_NAME));
        assertFalse(UserMementoManager.load(TEST_NAME).isPresent());
    }
}
