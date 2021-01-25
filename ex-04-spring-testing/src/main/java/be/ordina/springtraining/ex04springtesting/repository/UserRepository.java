package be.ordina.springtraining.ex04springtesting.repository;

import be.ordina.springtraining.ex04springtesting.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {
}
