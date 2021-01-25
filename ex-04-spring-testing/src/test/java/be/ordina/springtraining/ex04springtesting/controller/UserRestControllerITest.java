package be.ordina.springtraining.ex04springtesting.controller;

import be.ordina.springtraining.ex04springtesting.model.User;
import be.ordina.springtraining.ex04springtesting.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserRestControllerITest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeAll
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    public void cleanUp() {
        this.userRepository.deleteAll();
    }

    @Test
    public void given_unauthorized_client_then_addUser_should_return_401() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").doesNotExist());
    }

    @Test
    @WithMockUser
    public void given_user_then_addUser_should_persist_the_new_user() throws Exception {
        final User newUser = new User();
        newUser.setName("John");
        newUser.setAge(43);

        this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .content(this.objectMapper.writeValueAsString(newUser))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists(HttpHeaders.LOCATION))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(newUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(newUser.getAge()));
        Assertions.assertThat(this.userRepository.findAll().get(0))
                .hasFieldOrProperty("id")
                .hasFieldOrPropertyWithValue("name", newUser.getName())
                .hasFieldOrPropertyWithValue("age", newUser.getAge());
    }

    @Test
    @WithMockUser
    public void given_existing_user_then_findUser_should_persist_the_new_user() throws Exception {
        final User newUser = new User();
        newUser.setName("John");
        newUser.setAge(43);
        final User savedUser = this.userRepository.save(newUser);

        this.mockMvc.perform(MockMvcRequestBuilders.get(String.format("/users/%s", savedUser.getId()))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").value(savedUser.getAge()));
    }
}
