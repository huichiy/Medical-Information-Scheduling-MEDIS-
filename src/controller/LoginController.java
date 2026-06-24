package controller;

import dao.UserDAO;
import model.Result;
import model.User;
import util.PasswordHasher;
import util.Validator;
import java.util.Optional;

public class LoginController {
    private final UserDAO userDAO;

    public LoginController(UserDAO userDAO) { this.userDAO = userDAO; }

    public Result authenticate(String username, String password) {
        if (!Validator.isNotBlank(username)) return Result.fail("Username is required");
        if (!Validator.isNotBlank(password)) return Result.fail("Password is required");

        Optional<User> u = userDAO.findByUsername(username);
        if (u.isEmpty()) return Result.fail("Invalid username or password");

        if (!PasswordHasher.verify(password, u.get().getPasswordHash()))
            return Result.fail("Invalid username or password");

        return Result.ok(u.get());
    }
}
