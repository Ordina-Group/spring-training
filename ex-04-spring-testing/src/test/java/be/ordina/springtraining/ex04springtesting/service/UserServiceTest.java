package be.ordina.springtraining.ex04springtesting.service;

import be.ordina.springtraining.ex04springtesting.model.User;
import be.ordina.springtraining.ex04springtesting.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        this.userService = new UserService(this.userRepository);
    }

    @AfterEach
    void cleanUp() {
        Mockito.verifyNoMoreInteractions(this.userRepository);
    }

    @Test
    public void given_users_in_the_database_then_findAll_should_return_all_users() {
        // given
        final List<User> users = List.of(
                new User(UUID.randomUUID(), "John", 43),
                new User(UUID.randomUUID(), "James", 29));
        Mockito.when(this.userRepository.findAll())
                .thenReturn(users);

        // when
        final List<User> retrievedUsers = this.userService.findAll();

        // then
        Assertions.assertThat(retrievedUsers)
                .hasSize(users.size())
                .containsAll(users);
        Mockito.verify(this.userRepository).findAll();
    }

    @Test
    public void given_user_then_create_should_persist_a_new_user() {
        // given
        final User newUser = new User();
        newUser.setName("John");
        newUser.setAge(43);
        Mockito.when(this.userRepository.save(newUser))
                .thenReturn(new User(UUID.randomUUID(), newUser.getName(), newUser.getAge()));

        // when
        final User createdUser = this.userService.create(newUser);

        // then
        Assertions.assertThat(createdUser)
                .hasFieldOrPropertyWithValue("name", newUser.getName())
                .hasFieldOrPropertyWithValue("age", newUser.getAge());
        Assertions.assertThat(createdUser.getId()).isNotNull();
        Mockito.verify(this.userRepository).save(newUser);
    }

    @Test
    public void given_existing_user_then_find_should_retrieve_the_existing_user() {
        // given
        final UUID someId = UUID.randomUUID();
        final User existingUser = new User(someId, "John", 43);
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.of(existingUser));

        // when
        final User retrievedUser = this.userService.find(someId);

        // then
        Assertions.assertThat(retrievedUser).isEqualTo(existingUser);
        Mockito.verify(this.userRepository).findById(someId);
    }

    @Test
    public void given_non_existing_user_then_find_should_return_illegal_argument_exception() {
        // given
        final UUID someId = UUID.randomUUID();
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.empty());

        // when then
        Assertions.assertThatThrownBy(() -> this.userService.find(someId))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(this.userRepository).findById(someId);
    }

    @Test
    public void given_existing_user_then_update_should_update_the_existing_user() {
        // given
        final UUID someId = UUID.randomUUID();
        final User existingUser = new User(someId, "John", 43);
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.of(existingUser));
        final User userChanges = new User();
        userChanges.setName("Jeremy");
        userChanges.setAge(65);
        final User updatedUser = new User(someId, userChanges.getName(), userChanges.getAge());
        Mockito.when(this.userRepository.save(updatedUser))
                .thenReturn(updatedUser);

        // when
        final User user = this.userService.update(someId, userChanges);

        // then
        Assertions.assertThat(user)
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("name", userChanges.getName())
                .hasFieldOrPropertyWithValue("age", userChanges.getAge());
        Mockito.verify(this.userRepository).findById(someId);
        Mockito.verify(this.userRepository).save(updatedUser);
    }

    @Test
    public void given_non_existing_user_then_update_should_return_illegal_argument_exception() {
        // given
        final UUID someId = UUID.randomUUID();
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.empty());

        // when then
        Assertions.assertThatThrownBy(() -> this.userService.update(someId, new User()))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(this.userRepository).findById(someId);
    }

    @Test
    public void given_existing_user_then_delete_should_delete_the_existing_user() {
        // given
        final UUID someId = UUID.randomUUID();

        // when
        this.userService.delete(someId);

        // then
        Mockito.verify(this.userRepository).deleteById(someId);
    }

}