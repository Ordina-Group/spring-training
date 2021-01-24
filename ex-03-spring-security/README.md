# Exercise 03 - Spring Security
In this exercise we will build further upon the application that we created in exercise 02.
Our application is starting to take shape.
As it currently stands, everything is unprotected however!

All APIs are accessible by everyone.
Let us put a change to that.

## Setting up
We pick up where we left off in exercise 02.

Let us add Spring Security to our project. 
Adapt the `pom.xml` of the project, adding the following two Spring Security dependencies:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

One of them has a `test` scope and will be used later on.

## Exercise 1: Four o ones and four o threes everywhere
Import the generated project into your IDE.

Execute a `$ mvn clean install` to make sure that the project builds correctly.

Let's take the application for another spin.
Notice anything particular in the logs...?
Can you try accessing any of the pages or any of the APIs?
What do you notice?

It seems like we are getting a `401` on everything!

Accessing one of our pages redirects us to a login screen...
But, we haven't set up any security credentials?
By default, Spring Boot will set up basic security if the Spring Security starter is added to the dependencies of a project.
This, in order to have the application be secure by default.
This includes a login-page, available at `/login`, serving both a `GET` and `POST`, while also foreseeing a `GET` at `/logout` to log out.

Luckily, in the logs we see a security password being printed out in the form of a UUID.
For example: `0c046fda-7234-4c12-8626-6ebbaf27b828`.
Using that with the default username `user`, we can perform a login into the application.

Note that this generated password can be overridden by specifying the following application property:
```yaml
spring:
  security:
    user:
      password: password
```

Let's not do that now though...

Let us also add a logout-button on the overview page so that we can better test the security:
```xhtml
<h1>Users overview</h1>
<a href="/pages/add-user">Add user</a>
<a href="/logout">Logout</a>
```

Alright, let's try to add a user again using our very finest of forms.
Does it work...?

403?! But didn't we do a login already?
Adding Spring Security to our path will not only secure access to our application, but it will also automatically protect against Cross-Site Request Forgery (CSRF) attacks.
CSRF is an attack which forces an end user to execute unwanted actions in a web application in which is currently authenticated.
Basically an attacker will inject unwanted commands and have them executed by you, the authenticated user.
Forms can be vulnerable to this.

In order to combat this, we have to add a randomly generated CSRF token into our form.
This is typically stored in an invisible field, but it will be part of our `POST` request body.
This way the server can verify whether the request truly originates from our own trusted form and not some shady copy that has been tinkered with.

In our `/templates/add-user.html` file we have to add the following inside the form:
```xhtml
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
```

Restart the application and try again... does it work now?

Hurray, it does!

But what about our APIs...?
Executing them still results in a 401.
How can we authenticate there?

We could authenticate via Basic Authentication by filling in the `Authorization` header.
Basic Authentication is a very simple authentication scheme built into the HTTP protocol but it is far from safe.
The username and password get concatenated with a colon (`:`) in between, after which the string gets converted to a base64-encoded string.
This value is then prefixed with a "Basic " and put as the `Authorization` header value.

For example:
```yaml
Basic dXNlcjpjNTBhMTUyNC01ZWMyLTRlOGMtYWZkYi1mMWY1ZTI4ZjQyYzY=
```

Base64 encoding is not safe! Anyone can freely convert it back to a regular string.
Exposing both your username and password!

Try it out with Postman.
Under the "Authorization"-tab you can select `Basic Auth` as type.
Specify the default username and the generated password and try executing one of the `GET` APIs.
The call should go through.

Try doing the same thing with our `POST` API.
Does that one go through...?

For o three once again!
How do we deal with the CSRF protection in this case?
Do we have to specify a CSRF token here as well?

For APIs we typically disable the CSRF protection.
CSRF is to prevent direct posting of data to your site.
In other words, the client must actually post through an approved path, i.e. view the form page, fill it out, submit the data.
The entire purpose of offering APIs is generally to allow 3rd-party entities to access and manipulate data in your application.
This does not mean that APIs have to be unprotected.
We will just protect them in a different way.

The REST architecture is stateless.
We do not want to keep a client state on the server.
A common security implementation consists of setting up OAuth2 integration.
The OAuth2 delegation protocol allows us to retrieve an access token from an identity provider and gain access to an API by passing the token with subsequent requests.
A JSON Web Token (JWT, pronounced "jot") is commonly used for this.

A JSON Web Token (JWT) is a compact URL-safe means of representing claims to be transferred between two parties.
The claims in a JWT are encoded as a JavaScript Object Notation (JSON) object that is used as the payload of a JSON Web Signature (JWS) structure or as the plaintext of a JSON Web Encryption (JWE) structure, enabling the claims to be digitally signed or MACed and/or encrypted.
The payload consists of some standard attributes (called claims), such as issuer, subject (the user’s identity), and expiration time.
The specification allows these claims to be customized, allowing additional information to be passed along.

We won't go to too deep into this right now as this requires setting up, or integrating with, a user authentication and authorization server.

Let's already deactivate CSRF for our APIs for now.
We can do this by adding a security config class that we can tune according to our needs.
Create a new package `security` and create a `WebSecurityConfiguration` in it with the following definition:

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions()
                .sameOrigin()
                .and()
            .csrf()
                .ignoringAntMatchers("/users/**", "/h2-console/**").and()
                .authorizeRequests()
                        .mvcMatchers(HttpMethod.GET, "/users").permitAll()
                        .antMatchers("/h2-console/**").permitAll()
                        .anyRequest().authenticated()
                        .and()
                .formLogin().and()
                .logout().and()
                .httpBasic();
    }

}
```

This configuration does a couple of things:
* The first part is necessary to allow the iFrames used in the H2 console
* CSRF is deactivated for both our user management APIs and the H2 console
* Being authenticated is not required for the `GET /users` API while everything else within the application requires being authenticated besides the login and logout pages
* Basic Authentication is configured as the authentication method

Play around with both the forms and the APIs and see how they react.
Try customizing the config a bit.
For example, also try to enable the `GET /users/{id}` API.

This concludes the exercise on Spring Security!
Our application is now secure... in a certain way at least. :)<br/>
Just don't forget that Basic Authentication is not to be used for a production application!