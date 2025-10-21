package co.za.cput.controller.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.generic.UserAuthentication;
import co.za.cput.factory.generic.ContactFactory;
import co.za.cput.factory.generic.UserAuthenticationFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.MethodName.class)
class UserAuthenticationControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private static final String BASE_URL = "/HouseConnect/UserAuthentication";

    // Create Contact instance for the UserAuthentication
    private static final Contact contact = ContactFactory.createContact(
            "thina@gmail.com",
            "0712345678",
            "0723456789",
            true,
            true,
            Contact.PreferredContactMethod.ALTERNATE_PHONE
    );

    // Create initial UserAuthentication instance
    private static final UserAuthentication userAuthentication = UserAuthenticationFactory.createUserAuthentication(
            "thina@1505",
            "student1999", // Normally hashed before saving
            UserAuthentication.UserRole.STUDENT,
            contact,
            null,   // student is null for now
            null,   // landlord is null
            null    // admin is null
    );

    private static UserAuthentication userAuthentication_withId;

    @Test
    void a_create() {
        String url = BASE_URL + "/create";
        ResponseEntity<UserAuthentication> createResponse = this.restTemplate.postForEntity(url, userAuthentication, UserAuthentication.class);
        assertNotNull(createResponse.getBody());
        System.out.println("Created UserAuthentication: " + createResponse.getBody());
        userAuthentication_withId = createResponse.getBody();
        assertNotNull(userAuthentication_withId);
    }

    @Test
    void b_read() {
        assertNotNull(userAuthentication_withId);
        String url = BASE_URL + "/read/" + userAuthentication_withId.getAuthenticationId();
        ResponseEntity<UserAuthentication> readResponse = this.restTemplate.getForEntity(url, UserAuthentication.class);
        assertNotNull(readResponse.getBody());
        System.out.println("Read UserAuthentication: " + readResponse.getBody());
    }

    @Test
    void c_update() {
        assertNotNull(userAuthentication_withId);
        String url = BASE_URL + "/update";

        UserAuthentication updatedUserAuth = new UserAuthentication.Builder()
                .copy(userAuthentication_withId)
                .setPassword("newPassword2025") // Simulating password update
                .setUserRole(UserAuthentication.UserRole.LANDLORD) // Changing role
                .build();

        this.restTemplate.put(url, updatedUserAuth);

        ResponseEntity<UserAuthentication> readResponse =
                this.restTemplate.getForEntity(BASE_URL + "/read/" + updatedUserAuth.getAuthenticationId(), UserAuthentication.class);
        assertNotNull(readResponse.getBody());

        System.out.println("Read UserAuthentication after update: " + readResponse.getBody());

        assertEquals("newPassword2025", readResponse.getBody().getPassword());
        assertEquals(UserAuthentication.UserRole.LANDLORD, readResponse.getBody().getUserRole());
    }

    @Test
    void d_getAllUserAuthentications() {
        String url = BASE_URL + "/getAllUserAuthentications";
        ResponseEntity<UserAuthentication[]> response = this.restTemplate.getForEntity(url, UserAuthentication[].class);
        assertNotNull(response.getBody());

        System.out.println("All UserAuthentications:");
        for (UserAuthentication userAuth : response.getBody()) {
            System.out.println(userAuth);
        }
    }

    @Test
    void e_delete() {
        assertNotNull(userAuthentication_withId);
        String url = BASE_URL + "/delete/" + userAuthentication_withId.getAuthenticationId();
        this.restTemplate.delete(url);

        String readUrl = BASE_URL + "/read/" + userAuthentication_withId.getAuthenticationId();
        HttpClientErrorException.NotFound exception = assertThrows(HttpClientErrorException.NotFound.class,
                () -> this.restTemplate.getForEntity(readUrl, UserAuthentication.class));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        System.out.println("UserAuthentication successfully deleted. Status code: " + exception.getStatusCode());
    }
}
