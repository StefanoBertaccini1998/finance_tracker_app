package it.finance.sb.memento;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.ConsoleStyle;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UserMementoManager {
    private static final String SAVE_DIR = "saved_users/";
    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);


    static {
        new File(SAVE_DIR).mkdirs(); // create dir if not exists
    }

    public static void save(User user) throws IOException {
        mapper.writeValue(new File(SAVE_DIR + user.getName() + ".json"), user);
    }

    public static Optional<User> load(String username) throws IOException {
        File file = new File(SAVE_DIR + username + ".json");
        if (!file.exists()) return Optional.empty();
        return Optional.of(mapper.readValue(file, User.class));
    }

    public static List<String> listSavedUsers() {
        File dir = new File(SAVE_DIR);
        String[] names = dir.list((d, name) -> name.endsWith(".json"));
        return names == null ? List.of() : Arrays.stream(names).map(n -> n.replace(".json", "")).toList();
    }

    public static boolean delete(String name) {
        File file = new File(SAVE_DIR + name + ".json");
        return file.exists() && file.delete();
    }

}
