package it.finance.sb.service;


import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;

public class UserService implements InterfaceService<User> {
    //@Override
    public User create(String name, int age, Gender gender) {
        return new User(name, age, gender);
    }

    //@Override
    public User delete() {
        return null;
    }

    //@Override
    public User modify() {
        return null;
    }
}
