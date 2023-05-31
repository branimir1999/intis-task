package org.ivic.intis;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;

@QuarkusTest
public class UserResourceTest {

    // Kreiranje korisnika i provjera da li se korisnik uspješno sprema u bazu.
    // Validacija korisničkog imena i e-mail adrese pri kreiranju korisnika.
    @Test
    public void testCreateUser() {
        String name = "Test";
        String email = "test@email.com";

        User user = given()
                .contentType("application/json")
                .body("{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .body("name", is(name),
                        "email", is(email),
                        "id", notNullValue())
                .extract()
                .as(User.class);

        // Check that the user has been saved to the database
        User savedUser = User.findById(user.id);
        LOGGER.info(savedUser.name + " " + savedUser.email);
        assert(savedUser.name.equals(name) && savedUser.email.equals(email));
    }

    // Dohvaćanje korisnika po ID-u i provjera da li se vraća očekivani korisnik
    @Test
    public void testGetUserEndpoint() {
        User user = new User("Test", "test@email.com");
        user.persist();

        RestAssured.given()
                .pathParam("id", user.id)
                .when().get("/users/{id}")
                .then()
                .statusCode(200)
                .body(
                        "name", is("Test")
                );
    }

    // Dohvaćanje svih korisnika i provjera da li se vraćaju svi korisnici iz baze.
    @Test
    public void testGetAllUsersEndpoint() {
        // In this case, assuming that we have two users in the database
        List<User> users = Arrays.asList(new User("Test1", "test1@email.com"), new User("Test2", "test2@email.com"));

        users.forEach(user -> user.persist());

        RestAssured.given()
                .when().get("/users")
                .then()
                .statusCode(200)
                .body(
                        "size()", is(2),
                        "[0].name", is("Test1"),
                        "[1].name", is("Test2")
                );
    }

    // Ažuriranje korisnika i provjera da li se korisnik uspješno ažurira u bazi.
    // Validacija korisničkog imena i e-mail adrese pri žuriranju korisnika.
    @Test
    public void testUpdateUser() {
        String originalName = "Original";
        String originalEmail = "original@email.com";
        String updatedName = "Updated";
        String updatedEmail = "updated@email.com";

        // Create a new user to update
        User user = new User();
        user.name = originalName;
        user.email = originalEmail;
        user.persist();

        given()
                .contentType("application/json")
                .body("{\"name\":\"" + updatedName + "\", \"email\":\"" + updatedEmail + "\"}")
                .when()
                .put("/users/" + user.id)
                .then()
                .statusCode(200)
                .body("name", is(updatedName),
                        "email", is(updatedEmail));

        // Check that the user has been updated in the database
        User updatedUser = User.findById(user.id);
        assert(updatedUser.name.equals(updatedName) && updatedUser.email.equals(updatedEmail));
    }

    // Brisanje korisnika i provjera da li se korisnik uspješno briše iz baze.
    @Test
    @Transactional
    public void testDeleteUser() {
        // Create a new user to delete
        User user = new User();
        user.name = "Test";
        user.email = "test@email.com";
        user.persist();

        given()
                .when()
                .delete("/users/" + user.id)
                .then()
                .statusCode(204);

        // Check that the user has been deleted from the database
        assertFalse(User.findByIdOptional(user.id).isPresent());
    }
}