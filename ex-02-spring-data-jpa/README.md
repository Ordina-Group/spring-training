# Exercise 02 - Spring Data JPA
In this exercise we will build further upon the application that we created in exercise 01.
We're going to ditch the cache and persist our users into an actual database!

## Setting up
We pick up where we left off in the previous exercise.

We need to add Spring Data JPA and H2, an in-memory database, to our project. 
Adapt the `pom.xml` of the project, adding the following two dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

Anything else to configure?
Well, no!
Thanks to the auto-configuring of Spring Boot that is all we need to do!

## Exercise 1: Replacing the users cache by an actual persistence layer
Import the generated project into your IDE.

Execute a `$ mvn clean install` to make sure that the project builds correctly.

Is the project building well...?
Good! Time to break things. :)

We are going to do some refactoring.
First priority is getting rid of the users cache that we used before.

Delete the whole `cache` package with the `UsersCache` in there.

Also delete the `@Bean` reference in the `Ex02SpringDataJpaApplication` class.
```java
@Bean
public UsersCache usersCache() {
    return new UsersCache();
}
```

Rename the `application.properties` to `application.yaml`.
In the `application.yaml` file, we want to explicitly set the url of our in-memory database:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
```

If you don't set this, a random name will be used which will be printed in the logs during the startup of the application.
For convenience sake, we want to name it `testdb`.

Next up, time to link the database and our code together!

We start off with the creation of a repository.
Create a new package `repository` and create a `UserRepository` Java interface in it.
Have it extend the `JpaRepository` interface.
You will need to specify two type parameters:
* Type of the entity model
* Type of the ID

You should end up with something like:
```java
public interface UserRepository
        extends JpaRepository<User, UUID> {
}
```

Next up, we will tweak our existing `User` model a bit.
Add the `@Entity` annotation to the class to mark the class as being a valid JPA entity.

Entities demand you to mark an identifier.
In our case this is our `private UUID id` field.
Annotate it with `@Id`.

That's about it basically...
You can also add the `@Table` annotation to the class to specify the table name as the table name and the name of our entities usually don't match.
For the columns there is the `@Column` annotation to specify the column name.

To give an example of both:
```java
@Entity
@Table(name = "product")
public class ProductEntity {
    @Id
    @Column(name = "product_id")
    private UUID id;
    @Column(name = "product_name")
    private String name;
```

Depending on how the ID values will be generated, either by the application or the database, you can either initialise the value yourself or let the database handle it.

If the database will generate the value you should annotate the ID field with the `@GeneratedValue` annotation.
One of the parameters the annotations accepts is a `strategy`.
For example: `@GeneratedValue(strategy=GenerationType.IDENTITY)`. 

If we want to generate the IDs ourselves, for UUIDs this is easy, we don't have to specify any annotation on the ID field.
We do however need to foresee a method and annotate it with `@PrePersist` to have it executed just before the object gets persisted.
Let's do this for our `UserEntity`:
```java
@PrePersist
public void ensureUuid() {
    if (getId() == null) {
        setId(UUID.randomUUID());
    }
}
``` 

Alright, looking good so far!

Right now our controllers contain some of user business logic... which isn't very clean.
Typically we want our controllers to expose APIs and to funnel the input to our domain logic layer.
The output of the domain logic layer is returned as a result in the controller.
A best practice is to also foresee mappings of the "outer world" (DTOs) and our "safe inner world" (domain models) but more on that later!

Create a new package `service` and create a `UserService` in it.
Annotate it the class with `@Service` so that Spring will manage it.
This `UserService` class will contain our domain logic.

We want to refactor everything so that we have the following flow going:
```
Controller > Service > Repository
```

The service class needs to hold a reference to the repository class in order to persist data to the database.
You will also need to foresee the necessary methods for your business logic.
In our case this will be limited to CRUD logic, but usually it will get a bit more complex.

The `UserService` should look as follows:
```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {

    }

    public User create(User user) {

    }

    public User find(UUID id) {
 
    }

    public User update(UUID id, User userUpdates) {

    }

    public void delete(UUID id) {

    }

}
```

Can you implement the different methods?
In many cases our logic is pretty simply and nothing more than just delegating the input we receive to the repository class although in the service you will usually also do extra business validation and other things. 

Once the service is correctly linked to the repository, we can have a look at our controllers.
Seeing as we deleted the `UsersCache`, our controllers are filled with errors everywhere.
Refactor both controllers, replace any `UsersCache` reference with our new and shiny `UserService`.
Any business logic such as `user.setId(UUID.randomUUID())` in the `addUser` method should be removed as we want our controllers to be only responsible for funneling the input to our service and to return the result of it to the end user.

It also seems like we forgot to foresee an API to update an existing user!
You can blame the analyst for that... although there aren't any analysis documents, so...? :)

Implement the `PUT` API.
It will be somewhat of a mix of the `GET` and the `POST`.
* Reuse the URI `/users/{id}`
* In the input of your method you will both get a `PathVariable` and a `@RequestBody`.
* A successful `PUT` will have a response with HTTP code `200`.

All done refactoring?
No more errors?
Let's take our now-better-than-ever-application for a spin!

Now our application is being fed by (and in turn, also feeds) an in-memory database.

Accessing it can be done by going to [http://localhost:8080/h2-console](http://localhost:8080/h2-console).
You can leave every field untouched, but you will need to update the JDBC URL field:
```
jdbc:h2:mem:testdb
```

Clicking on the "Connect"-button should take you to an overview page of the database.
You should see a `USER` table with an `ID`, `AGE`, and `NAME` column.

Try playing around with the Thymeleaf form and all the APIs to make sure that everything functions as it should.

Congratulations!
You made the application so much better.
Stay tuned for the next part... :)

## Exercise 2: Putting our database schema under version control
As it currently stands, Hibernate is taking control of our database schema.
Which can be handy... but we typically want to take the evolution of our database schema into our own hands.

Flway is a database versioning tool that can help us with this.

Let's start off by adding the Flyway dependency:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

We should also deactivate the auto-dll of Hibernate.
At the same time, let's add some extra properties to have the SQL statements printed in the logs in a pretty way.
Adapt our `application.yaml` as follows:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
  flyway:
    baseline-on-migrate: true # initialize the schema history table
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true # Show SQL in logs
    properties:
      hibernate:
        format_sql: true # "pretty print"
```

Now that Hibernate won't be helping us any longer setting up our database schema for us, we need to do it ourselves!
Or at least, we will tell Flyway what it needs to do for us!

In the `src/main/resources`-directory, create the following structure:
```
resources
'--db
   '--migration
      '--V1__Create_table_user.sql
```

In the `V1__Create_table_user.sql` script you need to add the `CREATE` statement for our `USER` table:
```sql
CREATE TABLE USER (
    ID UUID NOT NULL,
    NAME VARCHAR(50),
    AGE INT
);
```

Alright, that should be about it.
Start up the application again, and pay attention to the logs.
You will see a couple of interesting ones of Flyway:
```
H2 console available at '/h2-console'. Database available at 'jdbc:h2:mem:testdb'
Flyway Community Edition 7.1.1 by Redgate
Database: jdbc:h2:mem:testdb (H2 1.4)
Creating schema "loans" ...
Creating Schema History table "loans"."flyway_schema_history" ...
Current version of schema "loans": null
Migrating schema "loans" to version "1 - Create table user"
Successfully applied 1 migration to schema "loans" (execution time 00:00.030s)
```

You will see that Flyway checked our database's schema and noticed that it wasn't up to date.
So Flyway proceeded with executing our V1 script in order to update the schema to version 1.

Have a look at the H2 console again at [http://localhost:8080/h2-console](http://localhost:8080/h2-console).

Our `USER` table should be there once again!
And there is actually a second table there called `flyway_schema_history`.
It is in there that Flyway will keep all the necessary info about the schema including what the current version is and a checksum of the executed scripts.
If you end up editing an existing script and deploying your application, Flyway will detect this change as a conflict and will block the startup of your application!
Thereby, forcing you to define a new script (`V2__XXX`) for new modifications/additions.

Play around with the APIs again.
Everything should still be working as before.
Only now, we are master of our database schema!

This concludes exercise 02.
Good job!