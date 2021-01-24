package be.ordina.springtraining.ex02springdatajpa.service;

import be.ordina.springtraining.ex02springdatajpa.model.User;
import be.ordina.springtraining.ex02springdatajpa.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return this.userRepository.findAll();
    }

    public User create(User user) {
        return this.userRepository.save(user);
    }

    public User find(UUID id) {
        return this.userRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException(String.format("Invalid id %s", id)));
    }

    public User update(UUID id, User userUpdates) {
        final User user = this.find(id);
        user.setName(userUpdates.getName());
        user.setAge(userUpdates.getAge());
        return this.userRepository.save(user);
    }

    public void delete(UUID id) {
        this.userRepository.deleteById(id);
    }

}