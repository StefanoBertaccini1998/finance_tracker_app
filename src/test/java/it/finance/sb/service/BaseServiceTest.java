package it.finance.sb.service;

import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for BaseService logic.
 */
class BaseServiceTest {

    private BaseService baseService;

    @BeforeEach
    void setup() {
        baseService = new BaseService() {}; // anonymous subclass for testing
    }

    @Test
    void testIsUserLoggedIn_shouldReturnFalseWhenNoUserSet() {
        assertFalse(baseService.isUserLoggedIn());
    }

    @Test
    void testIsUserLoggedIn_shouldReturnTrueWhenUserSet() {
        baseService.setCurrentUser(new User("Test", 30, Gender.OTHER));
        assertTrue(baseService.isUserLoggedIn());
    }

    @Test
    void testRequireLoggedInUser_shouldThrowWhenNoUser() {
        assertThrows(UserLoginException.class, baseService::requireLoggedInUser);
    }

    @Test
    void testRequireLoggedInUser_shouldNotThrowWhenUserPresent() {
        baseService.setCurrentUser(new User("Test", 30, Gender.OTHER));
        assertDoesNotThrow(baseService::requireLoggedInUser);
    }
}
