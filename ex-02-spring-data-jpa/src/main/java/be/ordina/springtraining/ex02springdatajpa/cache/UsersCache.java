package be.ordina.springtraining.ex02springdatajpa.cache;

import be.ordina.springtraining.ex02springdatajpa.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple cache to hold users. NOT PRODUCTION SAFE! :-)
 */
public final class UsersCache {
    private final List<User> usersCache = new ArrayList<>();

    public synchronized Optional<User> get(UUID userId) {
        return this.usersCache.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst();
    }

    public synchronized void add(User user) {
        this.usersCache.add(user);
    }

    public synchronized void remove(UUID userId) {
        get(userId).map(user -> this.usersCache.remove(user));

    }

    public synchronized List<User> getAll() {
        return Collections.unmodifiableList(this.usersCache);
    }
}
