package it.finance.sb.service;

import it.finance.sb.mapper.UserMapper;
import it.finance.sb.mapper.UserSnapshot;
import it.finance.sb.memento.UserMementoManager;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class MementoServiceTest {

    private MementoService mementoService;

    @BeforeEach
    void setup() {
        mementoService = new MementoService();
    }

    @Test
    void testSaveUser_success() {
        User user = new User("John", 30, Gender.MALE);
        UserSnapshot snapshot = new UserSnapshot("John", 30, Gender.MALE, List.of(), List.of(), Map.of());

        try (MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class);
             MockedStatic<UserMementoManager> mementoMock = mockStatic(UserMementoManager.class)) {

            mapperMock.when(() -> UserMapper.toSnapshot(user)).thenReturn(snapshot);

            assertDoesNotThrow(() -> mementoService.saveUser(user));
            mementoMock.verify(() -> UserMementoManager.save(snapshot));
        }
    }

    @Test
    void testSaveUser_failure() {
        assertThrows(IllegalArgumentException.class, () -> mementoService.saveUser(null));
    }

    @Test
    void testLoadUser_success() throws Exception {
        String name = "Alice";
        UserSnapshot snapshot = new UserSnapshot(name, 25, Gender.FEMALE, List.of(), List.of(), Map.of());
        User mockUser = new User(name, 25, Gender.FEMALE);

        try (MockedStatic<UserMementoManager> mementoMock = mockStatic(UserMementoManager.class);
             MockedStatic<UserMapper> mapperMock = mockStatic(UserMapper.class)) {

            mementoMock.when(() -> UserMementoManager.load(name)).thenReturn(Optional.of(snapshot));
            mapperMock.when(() -> UserMapper.fromSnapshot(snapshot)).thenReturn(mockUser);

            Optional<User> result = mementoService.loadUser(name);

            assertTrue(result.isPresent());
            assertEquals(name, result.get().getName());
        }
    }

    @Test
    void testLoadUser_invalidInput() {
        assertThrows(IllegalArgumentException.class, () -> mementoService.loadUser(" "));
    }

    @Test
    void testListUsers_success() {
        try (MockedStatic<UserMementoManager> mementoMock = mockStatic(UserMementoManager.class)) {
            mementoMock.when(UserMementoManager::listSavedUsers).thenReturn(List.of("u1", "u2"));

            List<String> result = mementoService.listUsers();
            assertEquals(2, result.size());
        }
    }

    @Test
    void testDeleteUser_success() {
        try (MockedStatic<UserMementoManager> mementoMock = mockStatic(UserMementoManager.class)) {
            mementoMock.when(() -> UserMementoManager.delete("John")).thenReturn(true);

            boolean deleted = mementoService.deleteUser("John");
            assertTrue(deleted);
        }
    }

    @Test
    void testDeleteUser_failure() {
        try (MockedStatic<UserMementoManager> mementoMock = mockStatic(UserMementoManager.class)) {
            mementoMock.when(() -> UserMementoManager.delete("John")).thenThrow(new RuntimeException("fail"));

            boolean deleted = mementoService.deleteUser("John");
            assertFalse(deleted);
        }
    }
}
