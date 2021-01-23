# Exercise 01 - Spring MVC
In this exercise we will build a small application that will offer a couple of webpages and some APIs.

## Creating our basic project
Start off with going to [https://start.spring.io](https://start.spring.io) where we will generate a Spring Boot application.

Pick a Maven project with Java 11 and Spring Boot's latest release version (2.4.2 at this moment).

Fill in the project metadata as you like

Pick the following dependencies:

* Spring Web
* Spring Boot DevTools
* Thymeleaf

Generate the project (CMD + RETURN or ALT + RETURN)

## Exercise 1: Basic users management webapp with Thymeleaf templates
Import the generated project into your IDE.

Execute a `$ mvn clean install` to make sure that the project build correctly.

Since our application is going to manage users, we need to define a model for it.<br/>
Create a new package `model` and create a new `User` class in there with 3 fields:
* An `id` field of type `UUID`
* A `name` field of type `String`
* An `age` field of type `int`

Tip: It is a best practice to also foresee (generate) the `hashCode`, `equals` and `toString` method of a POJO.

We want persist our users somewhere... For now, we will create a simple cache to help us with this.<br/>
Create a new package `cache` and import the following class into it:
```java
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

    public synchronized List<User> getAll() {
        return Collections.unmodifiableList(this.usersCache);
    }
}
```
Let's have Spring create a bean for it.<br/>
Inside your Application class, add the following snippet:
```java
@Bean
public UsersCache usersCache() {
    return new UsersCache();
}
```

Let's start with creating the necessary Thymeleaf templates.
We want a page that will show us an overview of our users.<br/>
Under `resources/templates` create a new file `users-overview.html` with the following content:
```html
<!DOCTYPE HTML>
<html xmlns:th="https://www.thymeleaf.org">
    <head>
        <title>Add a user</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    </head>
    <body>
        <h1>Users overview</h1>
        <a href="/pages/add-user">Add user</a>
        <table>
            <thead>
            <tr>
                <th th:text="ID">ID</th>
                <th th:text="Name">Name</th>
                <th th:text="Age">Age</th>
            </tr>
            </thead>
            <tbody>
            <tr th:each="user: ${users}">
                <td th:text="${user.id}"></td>
                <td th:text="${user.name}"></td>
                <td th:text="${user.age}"></td>
            </tr>
            </tbody>
        </table>
    </body>
</html>
```
Under `resources/templates` create a new file `add-user.html` with the following content:
```html
<!DOCTYPE HTML>
<html xmlns:th="https://www.thymeleaf.org">
    <head>
        <title>Add a user</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    </head>
    <body>
        <h1>Add a user</h1>
        <form action="#" th:object="${user}" method="post">
            <p>Name: <input type="text" th:field="*{name}" /></p>
            <p>Age: <input type="text" th:field="*{age}" /></p>
            <p><input type="submit" value="Submit" /> <input type="reset" value="Reset" /></p>
        </form>
    </body>
</html>
```

We now have our templates in place but in order to route requests to it we need to create a controller class for it.<br/>
Create a new package `controller` and create a new Java class `UserThymeleafController` in it.

Add the following two annotations to it:
```java
@Controller
@RequestMapping("/pages")
```

In our controller we want Spring to inject the `UsersCache` bean that we created earlier so create an instance variable for it and create a constructor with the cache as one of the arguments.
The result should be like:
```java
private final UsersCache usersCache;

public UserThymeleafController(UsersCache usersCache) {
    this.usersCache = usersCache;
}
```

Note that Spring is smart enough to inject the bean by just inspecting the constructor.
An `@Autowired` is not necessary.


Next up, we want to create a method to make our overview page available:
```java
@GetMapping("/users-overview")
public String usersOverview(Model model) {

}
```
Retrieve the list of users from the `usersCache` object via the `getAll` method.<br/>
Add the list to the `model` object via its `addAttribute` method.
The attribute name (first argument) should be `"users"` and the attribute value (second argument) should be the users list.<br/>
You will see that we need to return a string object. This refers to the Thymeleaf template name, so you can return `"users-overview"`. 

We can now launch the application and see if we can access our overview page!
Browse to [http://localhost:8080/pages/users-overview](http://localhost:8080/pages/users-overview).
You should see a rather empty page, although it is still something. :-)

Our "Add user" button already leads us to a different URL but our application is still unaware of it.

Inside the `UserThymeleafController`, we want to foresee a method to present the page to add a user.
Add the following to it:
```java
@GetMapping("/add-user")
public String addUserForm(Model model) {
    
}
```

We need to initialize an form object that will be filled up with the input of our end user.
For simplicity's sake we will just use a `new User()` for this.
Add it to the `model` object via its `addAttribute` method.
The attribute name should be `"user"` and the value should be our newly instantiated user object.
As for the return statement, we need to specify the name of our form Thymeleaf page:
```java
return "add-user";
```

When restarting our application, the button should now take us to the form!
However, submitting the form isn't doing anything!
Submitting the form leads to a `POST`.
Our controller doesn't handle a `POST` to `/pages/add-user` just yet.
Let's implement it!

Add the following method to the controller:
```java
@PostMapping("/add-user")
public String addUserForm(@ModelAttribute User user) {

}
```

Note that we receive a `User` instance which will be filled with the data specified by our end user in the form.
Before "persisting" the user we would normally perform the necessary validation.
It is a best practice to assign the identity to a record just before persisting it, either by ourselves or via the database.
Let's set the ID of the `User` instance:
```java
user.setId(UUID.randomUUID());
```

Now you can "persist" it in our users cache via the `add` method.

Finally, we want to do a redirect to the overview page.
This can be accomplished in the following way:
```java
return "redirect:/pages/users-overview";
``` 

Restart the application!
If everything went well, you should now be able to add users.
The newly added users should be found in the table on the overview page.

Well done!

## Exercise 2: Extending our webapp with APIs

We're very satisfied so far with our application, and we are very eager to put it into production!
Before we do that, let us extend the application with some REST APIs so that other systems can integrate with it!

Let's create a new controller for this.
Let's name it `UserRestController`.

Annotate it with `@RestController` and also have Spring inject the `UsersCache` bean into it, as we have done in the other controller.

Creating APIs is really easy with Spring.
We start off with thinking about what kind of API we want to foresee.
In this case we want to start off with an API to look up all our users.
Retrieving data is typically done via a `GET` API.
As for the URI, it should represent the resource that we expose so we can settle with `/users`.

Taking all that in mind, let's add a new method to the controller:
```java
@GetMapping("/users")
public ResponseEntity<List<User>> getUsers() {
    
}
```

Note that our controller returns a `ResponseEntity`.
This represents an HTTP request or response entity, consisting of headers and body.
For the happy path of a `GET` we return a HTTP code `200` ("Ok").
As for the body, we want to return all users using our cache's `getAll` method.

The `ResponseEntity` class has many useful static methods to easily create an instance.
The static method we're looking for is the `ok` method.
Your return statement will look like the following:
```java
return ResponseEntity.ok(users);
```

Restart your application, add a couple of users via your form and then visit [http://localhost:8080/users](http://localhost:8080/users).
By default, Spring will return a JSON output for our `GET`.
You might remember from the REST APIs training that a client is able to specify the format of the response using the `Accept` header.
Typically we will stick to JSON but a client could for example request for an XML output. 

Looking up users is cool, but we also want other systems to be able to add new users!
Let's also create an API for that.
For the creation of data, we typically use a `POST`.
According to REST standards, you should reuse the URI that represents the resource you want to manipulate (ie: `/users`).

Taking all this into account, let's extend our controller with another method:

```java
@PostMapping("/users")
public ResponseEntity<User> addUser(@RequestBody User user) {
    user.setId(UUID.randomUUID());
    this.usersCache.add(user);
    return ResponseEntity.created(URI.create("/users/" + user.getId()))
            .body(user);
}
```

Similar to the other controller, we will get something in input.
With a `POST` a request body can be specified.
Using the `@RequestBody` annotation we can capture the request body into an object.
Spring will automatically deserialize the JSON body into our object by matching the field names of both.

The treatment of the object we receive is similar to what we did in the other controller.
We want to assign an identity to the new record and then "persist" it into our cache object.

As for the response, with a `POST` we typically have two options for the happy path:
* Return a response with HTTP code `201` ("Created"), with the `Location` filled as well referring to where the newly created object can be found, and optionally with a response body.
* Return a response with HTTP code `204` ("No Content") without a response body.

In our case the first option makes the most sense as our `POST` API will result in the creation of a record in our system.
Let's be complete and return a response body as well.

Using the `ResponseEntity.created` static method prompts us to specify the location of newly created record.
This API doesn't exist yet, but we already know what the URI will look like: `/users/{id}`.
We will also need to specify the body to our response by chaining the `body` method.

The result should be something like:
```java
return ResponseEntity.created(URI.create("/users/" + user.getId()))
        .body(user);
```

Restart your application and let's take this new API of ours for a spin!
Doing a `POST` is slightly more complicated.
We could use a terminal command for this such as `curl` but as for testing APIs we want to have something more convenient.
Postman is a popular tool for API testing.

Create a new project in Postman.
After doing so, create a new request and adapt the following things:
* Make it a `POST` request
* Under the "Headers"-tab, specify the `Content-Type` header with as value `application/json`.
  This to define what our request's body payload will be like.
* Specify a "raw" JSON body in the "Body"-tab:
```json
{
    "name": "Yannick",
    "age": 31
}
```

All set, let's go!
Go hit that "Send"-button to propel our request towards our new and shiny API!
If everything went well you should receive a response with HTTP status `201`.
A response body such as the following should be in there:
```json
{
    "id": "d657b532-4866-499e-ba9d-c46d7939c2f1",
    "name": "Yannick",
    "age": 31
}
```

Also have a look at the "Headers"-tab of the response.
It should contain a `Location` header with the URI in there such as `/users/d657b532-4866-499e-ba9d-c46d7939c2f1`.
A URI is handy, but the client still needs to concatenate it with the hostname of the application.
We should improve our `POST` method by specifying the URL instead of the URI.
In order to accomplish this, we can have Spring inject a `UriComponentsBuilder` into our method to that we can build the URL:
```java
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
```

Restarting our application and doing another `POST` should now result into a URL being present in the `Location` header instead of simply the URI: `http://localhost:8080/users/ef76fb59-7673-47c1-acc8-7a61ec1f31c2`.

Our application is only getting better and better!
Let us finalize it by also foreseeing a `GET` for looking up a specific user and a `DELETE` for deleting a specific user.

The `GET` should be pretty straightforward as it is very similar to our existing `GET` API.
We just need to be able to retrieve the ID that is specified in the URI.
We can easily have Spring pass this onto us by specifying a `@PathVariable` as one of our arguments:
```java
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable UUID id) {

}
```

Note, you will notice that the `get` method of the user cache returns an optional...
Can you figure out what you should do with it?<br/>

Tip: If incorrect input values are specified we will typically throw an `IllegalArgumentException`.

For the `DELETE` however, we will need to improve our cache a bit in order to support deletes.
The `DELETE` API itself is similar to the `GET` API as it will reuse the URI. 
Can you figure it out yourself? Don't forget to use the right mapping annotation!
Also... with a `DELETE` we usually don't return a body so have it return a `204`.<br/>

Tip #1: When invoking the right static method of the `ResponseEntity` class for the "No Content", you will have to chain the `build` method.<br/>
Tip #2: As for the method's signature, if the response doesn't have a response body we can define the method as returning a `ResponseEnity<Void>`

Congratulations! You now know the very basics of Spring MVC! :-) 