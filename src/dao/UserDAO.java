package dao;

import model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);
    List<User>     findAll();
}
