package com.bbl.devfest.service;

import com.bbl.devfest.dto.UserRequest;
import com.bbl.devfest.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * In-memory user store (per test requirements — no database).
 * Seeded with sample entries from jsonplaceholder.typicode.com/users.
 */
@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public UserService() {
        save(new User(nextId.getAndIncrement(), "Leanne Graham", "Bret",
                "Sincere@april.biz", "1-770-736-8031 x56442", "hildegard.org"));
        save(new User(nextId.getAndIncrement(), "Ervin Howell", "Antonette",
                "Shanna@melissa.tv", "010-692-6593 x09125", "anastasia.net"));
        save(new User(nextId.getAndIncrement(), "Clementine Bauch", "Samantha",
                "Nathan@yesenia.net", "1-463-123-4447", "ramiro.info"));
    }

    public List<User> findAll() {
        return users.values().stream()
                .sorted(Comparator.comparing(User::id))
                .toList();
    }

    public User findById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw notFound(id);
        }
        return user;
    }

    public User create(UserRequest request) {
        User user = new User(nextId.getAndIncrement(), request.name(), request.username(),
                request.email(), request.phone(), request.website());
        return save(user);
    }

    public User update(Long id, UserRequest request) {
        User updated = new User(id, request.name(), request.username(),
                request.email(), request.phone(), request.website());
        if (users.replace(id, updated) == null) {
            throw notFound(id);
        }
        return updated;
    }

    public void delete(Long id) {
        if (users.remove(id) == null) {
            throw notFound(id);
        }
    }

    private User save(User user) {
        users.put(user.id(), user);
        return user;
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(NOT_FOUND, "User %d not found".formatted(id));
    }
}
