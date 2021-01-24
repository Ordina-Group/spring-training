package be.ordina.springtraining.ex03springsecurity.repository;

import be.ordina.springtraining.ex03springsecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {
}
