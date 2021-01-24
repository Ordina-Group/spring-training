package be.ordina.springtraining.ex02springdatajpa.repository;

import be.ordina.springtraining.ex02springdatajpa.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository
        extends JpaRepository<User, UUID> {
}
