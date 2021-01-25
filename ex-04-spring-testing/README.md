# Exercise 04 - Spring Testing
In this exercise we will build further upon the application that we created in exercise 03.
Our application now offers webpages, APIs, and it is "secure"!

We did test everything manually...
And after every change we are somewhat forced to manually retest everything to avoid any possible regressions.
This is not ideal, and it costs us a lot of time to do so each time.

Automated tests to the rescue!

## Setting up
We pick up where we left off in exercise 03.

We want to foresee the necessary tests for both the whole E2E flow and to test our business logic.
Let's look at our existing classes and how we want to test them.
The `UserThymeleafController` will be out of scope for now.
* `UserRestController`: The APIs are the entrypoint of our application.
In general, controllers remain pretty dumb.
Their purpose is to funnel requests to our business layer.
They shouldn't contain any business logic besides mapping logic (DTOs to domain models and vice versa).
Because of that, they don't make an ideal candidate for simple unit tests.
It is more efficient to write a complete E2E integration test.
Which is what we will do. 
* `UserRepository`: Repositories typically also don't contain that much business logic.
Most of the work will be done by the persistence framework, in our case Spring Data JPA, with all the free queries it offers us.
We don't want to write tests for these JPA methods.
We only want to write tests for the custom queries and/or other logic that we foresee.
As for the type of tests... writing unit tests where we mock the database would be a waste of time.
Which is why we also want to write integration tests where we test our own queries using an in-memory database.
For now, we didn't add any custom queries yet, so we don't have any tests to write for our `UserRepository.
At the end of the exercise an example of such an integration test can be found.
* `UserService`: Services contain pure business logic.
This makes them ideal for true unit tests where we mock all external components.

## Exercise 1: Unit tests with mock for the service
Let's start off with the unit tests with mocks for our `UserService`.

We need to create a `UserServiceTest` in `be.ordina.springtraining.ex04springtesting.service` in `/src/test/java`.
We retain the same package structure as in `/src/main/java`.

For the content of the test class, you can use the following as basis:
```java
public class UserServiceTest {

    private UserRepository userRepository;

    private UserService userService;

    void setUp() {
        this.userService = new UserService(this.userRepository);
    }
    
    void cleanUp() {
        Mockito.verifyNoMoreInteractions(this.userRepository);
    }

    @Test
    public void given_users_in_the_database_then_findAll_should_return_all_users() {
        // given

        // when

        // then
    }

    @Test
    public void given_user_then_create_should_persist_a_new_user() {
        // given

        // when

        // then
    }

    @Test
    public void given_existing_user_then_find_should_retrieve_the_existing_user() {
        // given

        // when

        // then
    }

    @Test
    public void given_non_existing_user_then_find_should_return_illegal_argument_exception() {
        // given

        // when then
    }

    @Test
    public void given_existing_user_then_update_should_update_the_existing_user() {
        // given

        // when

        // then
    }

    @Test
    public void given_non_existing_user_then_update_should_return_illegal_argument_exception() {
        // given

        // when then
    }

    @Test
    public void given_existing_user_then_delete_should_delete_the_existing_user() {
        // given

        // when

        // then
    }

}
```

The test methods have already been defined.
Notice how long the test methods are.
The importance of writing tests is not only about testing whether things work, but it can also act as documentation.
Our unit test needs to tells us what business criteria it is testing.

We need to pimp our code a bit with the right annotations before we can start writing our tests.

We want to use Mockito for helping us with mocking external components such as the `UserRepository`.
Annotate the test class with the JUnit 5 `@ExtendWith(MockitoExtension.class)` annotation.

The `UserRepository` needs to be mocked so we need to annotate that instance variable with `@Mock`.

Notice that we have a `setUp` method available with some initialization logic.
We want to execute this method before each test.
This is easily done by annotating the method with the JUnit 5 `@BeforeEach` annotation.

After each test we want to invoke a static test method of Mockito to check whether we verified all calls made to the mocks.
This is specified in the `cleanUp` method.
This forces us to verify all calls made to mocks.
If a certain method was called on a mock, and it wasn't verified at the end of the test, the `Mockito.verifyNoMoreInteractions` static method will fail the test to inform us about that.
To have it executed after each test we add the `@AfterEach` annotation to the method.

Now it is up to you to implement the different unit tests. :-)<br/>
First of all, do check that you imported the right `@Test` class.

The right import is:
```java
import org.junit.jupiter.api.Test;
```

Everything JUnit 5 related makes use of the `org.junit.jupiter.api.` package.
Whereas the old JUnit 4 package is `org.junit`.

Let's try to implement the happy path and the exception unit test for the `userService.find` method.

Starting with the happy path:
```java
    @Test
    public void given_existing_user_then_find_should_retrieve_the_existing_user() {
        // given

        // when

        // then
    }
```

We identify the three parts of our test flow:
* `given`: We do the necessary setup work and we prepare the mocks.
* `when`: We invoke the method we want to test
* `then`: We assert the result and verify the calls made to our mocks (or verify that mocks were left alone)

Let's start by filling in the `// given`.
In our case we have to mock the `this.userRepository.findById` call done in the service class and have it return a user based on the ID that we will pass to the service.

We add the following:
```java
        // given
        final UUID someId = UUID.randomUUID();
        final User existingUser = new User(someId, "John", 43);
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.of(existingUser));
```

Now we can invoke our service's `find` method with the same ID (`someId`) that we used in the mock's response.

We add the following:
```java
        // when
        final User retrievedUser = this.userService.find(someId);
```

Finally, we need to assert that the retrieved user is equal to our existing user.
And we also want to verify that the `findById` method of the `userRepository` was invoked during our test scenario.

We add the following:
```java
        // then
        Assertions.assertThat(retrievedUser).isEqualTo(existingUser);
        Mockito.verify(this.userRepository).findById(someId);
```

If all went well, the test should pass!
A good start. :)

Besides testing the happy path, it is also important to test the behaviour of when things go south.
Let's implement the exception test for our `find` method:
```java
    @Test
    public void given_non_existing_user_then_find_should_return_illegal_argument_exception() {
        // given

        // when then
    }
```

This test scenario is actually not too difficult to implement.
We need to simulate that the user cannot be found.
So we will have the `this.userRepository.findById` return an `Optional.empty()`:
```java
        // given
        final UUID someId = UUID.randomUUID();
        Mockito.when(this.userRepository.findById(someId))
                .thenReturn(Optional.empty());
```

As for the remaining parts.
Our test scenario will result in an `IllegalArgumentException.class` being thrown.
Typically, if an exception gets thrown, things break. :-)<br/>
In this case however, we want to capture the exception and assert that it is the exception that we are expecting.
We can use a nifty AssertJ static method for this, called `Assertions.assertThatThrownBy`.
After testing the exception's class, we also want to verify whether the `findById` method of the `userRepository` was also invoked.

Add the following:

```java
        // when then
        Assertions.assertThatThrownBy(() -> this.userService.find(someId))
                .isInstanceOf(IllegalArgumentException.class);
        Mockito.verify(this.userRepository).findById(someId);
    }
```

Take the test class for another test drive!
Two of our tests should now be green.

All warmed up, can you implement our other tests?
Maybe start with the `delete` test method at the end.

In certain cases we will also want to do extensive validation of the user that was created or updated.
You can do this in various ways.
One way is more elegant than the other.
AssertJ offers some nice static methods to help us with this.

For example:
```java
        Assertions.assertThat(user)
                .hasFieldOrPropertyWithValue("id", someId)
                .hasFieldOrPropertyWithValue("name", userChanges.getName())
                .hasFieldOrPropertyWithValue("age", userChanges.getAge());
```

Good luck!<br/>
Feel free to reach out if you need help.

## Exercise 2: Integration tests with Spring Testing for the controller
With our user's business logic in `UserService` all being nicely covered, it is time to foresee an integration test for our controller to make sure that the whole flow is working as expected.
We are not going to use any mocks in our case as we want to test the whole flow.
If we were to have external components being invoked in our component's code, for example a call to an external API, we would mark that as a mock with Spring Testing's `@MockBean` annotation.

Create a new class `UserRestControllerITest` in the `be.ordina.springtraining.ex04springtesting.controller` package in `/src/test/java`.

For the content of the test class, you can use the following as basis:
```java
public class UserRestControllerITest {

    private MockMvc mockMvc;

    
    private WebApplicationContext webApplicationContext;
    
    private ObjectMapper objectMapper;

    
    private UserRepository userRepository;

    
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    
    public void cleanUp() {
        this.userRepository.deleteAll();
    }

    @Test
    public void given_unauthorized_client_then_addUser_return_401() throws Exception {

    }

    @Test
    
    public void given_user_then_addUser_should_persist_the_new_user() throws Exception {

    }

    @Test
    
    public void given_existing_user_then_findUser_should_persist_the_new_user() throws Exception {

    }
}
```

The most important part of this integration test is to test our flow.
Our unit tests in `UserServiceTest` already cover a lot of our business logic.
So, we don't want to waste too much time repeating the same tests here, in the integration test class of the controller.
Of course, if time were not an issue, this wouldn't really be a problem and it would improve our test coverage.

Let's start with setting up our test class with the right annotations.

This integration test has to start up our application completely before it launches all the tests.
In order to do that, we have to mark it as a Spring integration test by annotating the test class with `@SpringBootTest`.

The `MockMvc` class is a useful utility tool to call an endpoint such as our APIs.
We will instantiate it in a bit.

Next up, there are three classes that we will use in our integration tests.
Either for setting up our config, or to help us with the validation of our test scenario.
These being the `WebApplicationContext`, `ObjectMapper` and `UserRepository` instance.
We want Spring to inject the instance for us here, so annotate all three of them with `@Autowired`.

To quickly sum up what we will use them for:
* `WebApplicationContext`: This is the web application context of our application that we let Spring inject for us so that we can use it in order to instantiate the `mockMvc` instance.
* `ObjectMapper`: This is a class from the Jackson library, also used by Spring, for serializing and deserializing JSON to objects.
We will use this class to send a JSON request body based on an object that we prepare in our tests.
* `UserRepository`: Our repository that we will use to insert data into the in-memory database or to look up the records that we created or updated.

In contrary to our `UserServiceTest` , we want to invoke the `setUp` once for our test class instead of once for each of our tests.
In this case we will annotate it with JUnit 5's `@BeforeAll` annotation.
Note that after adding it you will see that the compiler will complain that the method needs to be static.

We however, won't comply with that... :)<br/>
By default, Spring will restart the application after each test before executing the next one.
Obviously this has an impact on the performance of our integration tests (which are already quite a bit slower than pure unit tests).
We can alter this lifecycle approach to have it start up only once and to just do the necessary cleaning in between tests.
This can be done by adding the `@TestInstance(TestInstance.Lifecycle.PER_CLASS)` annotation to the class name.
By doing so, the `@BeforeAll` annotation also no longer demands a static method!
So, our `setUp` method can stay like it was.
Excellent!

Note that in the `setUp` method we also aply something special to correctly prepare our `mockMvc` for Spring Security.

We are almost done with the setup...
Finally, we are going to use an in-memory database in our tests.
We are not doing a complete restart of the application so we want to clean this database in between tests.
This is the role of the `cleanUp` method.
We want to have it executed after each test so we annotate it with `@AfterEach`.

All set up, time to write the integration tests!

Let's do two together to get you introduced.

In the last exercise we have configured Spring Security to secure our application.
Let's write a test to verify that a `401` is returned if our end user is unauthorized:

```java
    @Test
    public void given_unauthorized_client_then_addUser_should_return_401() throws Exception {

    }
```

We can easily trigger this by calling for example the "add user" API.
Which is a `POST` to `/users`.
Let's think about how we will tackle this:
* We are going to invoke the endpoint using `mockMvc`.
* We won't bother specifying a request body, nor the `Content-Type`-header.
* We will specify that we expect a JSON response body by specifying the `Accept`-header.
* We expect to receive a `401` as response HTTP code
* And finally, we want to assert that the response body that we receive doesn't contain any of the user's fields.

Taking all this in mind, we come up with the following code:
```java
    @Test
    public void given_unauthorized_client_then_addUser_return_401() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.post("/users")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").doesNotExist())
                .andExpect(MockMvcResultMatchers.jsonPath("$.age").doesNotExist());
    }
```

As you can see, we chain a lot of calls to the `mockMvc` instance.
It is a pretty handy thing, really!

As for the second example, let's write a happy path test for the "add user" API.
One where we also have an authenticated user.
We will implement the following test:
```java
    @Test
    
    public void given_user_then_addUser_should_persist_the_new_user() throws Exception {

    }
```

Note how there an extra newline between `@Test` and the test method?
We are going to insert another annotation there!
The Spring Security Test dependency offers a couple of utilities to help you write tests when your application has been secured by Spring Security.

We want to execute this test with an authenticated user.
Obtaining credentials and all would not be that practical, so Spring Security offers the `@WithMockUser` annotation to simulate an authenticated user.
Add the annotation to the test method.

Now we can implement our test.

We will take the following steps:
* Our `POST` demands us to specify a user in JSON format in the request body, so we will define a `User` object and set the name and age (we don't specify the ID).
* Using `mockMvc` we will do a `POST` to `/users` and specify the content of our request's body while also specifying the `Content-Type`-header as being `application/json`.
* We expect a JSON response body, so we set the `Accept`-header to `application/json`.
* We expect a HTTP status code `201` (created) in our response
* We expect a `Location` header
* We expect that the created user in our response has an ID specified (any value, seeing as it is generated randomly)
* We expect that the created user in our response has the name and age that we specified in our request body
* Finally, we also expect that we find the user that we had created into the database.
So we will look it up using the `userRepository` and compare both objects.

Taking all this into mind, let's implement the test: 

```java
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
``` 

Reads pretty fluently if you look at it, don't you think...? :)

Can you try to implement the remaining tests?

Feel free to add more tests as you see fit!

This concludes the exercise on Spring Testing.
Obviously, there is a lot more to it but this should already take you pretty far.

Something that we didn't cover here were integration tests for repositories.
A specific annotation, similar to `@SpringBootTest` exists for such integration tests: `@DataJpaTest`.
This annotation will also start up the application but only with the beans related to the repository layer to make it lighter and quicker to run.

To give a small example of such a test class:
```java
@ExtendWith(SpringExtension.class)
@DataJpaTest
public class LoanJpaRepositoryTest {

    @Autowired
    private LoanJpaRepository loanJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void given_loan_with_actorName_then_findByActorName_should_return_loan_with_actorName() {
        final LoanJpaEntity loan1 = new LoanJpaEntity();
        loan1.setActorName("John");
        this.entityManager.persist(loan1);
        final LoanJpaEntity loan2 = new LoanJpaEntity();
        loan2.setActorName("Foo");
        this.entityManager.persist(loan2);
        this.entityManager.flush();

        final List<LoanJpaEntity> loans = this.loanJpaRepository.findByActorNameIgnoreCase("john");

        Assertions.assertThat(loans)
                .hasSize(1)
                .contains(loan1);
    }
}
```

Note the `@DataJpaTest` on the class.

In this example we test the `findByActorNameIgnoreCase` method that was defined in the `LoanJpaRepository` interface.

We utilise an `EntityManager` to insert some data into the in-memory database before we launch our test.

Good job with all the testing.
Onwards to the next part!