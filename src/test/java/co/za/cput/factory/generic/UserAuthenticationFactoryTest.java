package co.za.cput.factory.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.generic.UserAuthentication;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserAuthenticationFactoryTest {

    private static Contact contact = ContactFactory.createContact(
            "Abigail@yahoo.com",
            "0712345678",
            "0723456789",
            true,
            true,
            Contact.PreferredContactMethod.EMAIL
    );

    private static UserAuthentication userAuthentication = UserAuthenticationFactory.createUserAuthentication(
            "Abi@123",
            "student123",
            UserAuthentication.UserRole.STUDENT,
            contact,
            null,
            null,
            null
    );

    @Test
    void createUserAuthentication() {
        assertNotNull(userAuthentication);
        System.out.println("Created user authentication: " + userAuthentication);
    }

    @Test
    void testUserAuthentication_withEmptyFields() {
       UserAuthentication userAuthentication1 = UserAuthenticationFactory.createUserAuthentication(
                "",
                "student123",
                UserAuthentication.UserRole.STUDENT,
                contact,
                null,
                null,
                null
        );
        assertNull(userAuthentication1);
       System.out.println("passed due to empty fields. Results: " + userAuthentication1);
    }

    @Test
    void testInvalid_Password() {
        UserAuthentication userAuthentication2 = UserAuthenticationFactory.createUserAuthentication(
                "Abi@123",
                "student123!",//password should not contain special characters.
                UserAuthentication.UserRole.STUDENT,
                contact,
                null,
                null,
                null
        );
        assertNull(userAuthentication2);
        System.out.println("passed due to invalid password. Results: " + userAuthentication2);
    }
}