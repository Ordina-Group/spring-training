package be.ordina.springtraining.ex01springmvc.controller;

import be.ordina.springtraining.ex01springmvc.cache.UsersCache;
import be.ordina.springtraining.ex01springmvc.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UserRestController {

    private final UsersCache usersCache;

    public UserRestController(UsersCache usersCache) {
        this.usersCache = usersCache;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers() {
        final List<User> users = this.usersCache.getAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        final User user = this.usersCache.get(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ID specified"));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<User> addUser(@RequestBody User user,
                                        UriComponentsBuilder uriComponentsBuilder) {
        user.setId(UUID.randomUUID());
        this.usersCache.add(user);
        UriComponents uriComponents =
                uriComponentsBuilder.path("/users/{id}").buildAndExpand(user.getId());
        return ResponseEntity.created(uriComponents.toUri())
                .body(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        this.usersCache.remove(id);
        return ResponseEntity.noContent().build();
    }

}
