package co.za.cput.controller.generic;

import co.za.cput.domain.generic.Contact;
import co.za.cput.service.generic.implementation.ContactServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequestMapping({"/api/contacts", "/HouseConnect/Contact"})

public class ContactController {

    private ContactServiceImpl contactService;

    public ContactController(ContactServiceImpl contactService) {
        this.contactService = contactService;
    }

    @PostMapping("/create")
    public ResponseEntity<Contact> create(@RequestBody Contact contact) {
        Contact createdContact = contactService.create(contact);
        return ResponseEntity.ok(createdContact);
    }

    @GetMapping("/read/{contactID}")
    public ResponseEntity<Contact> read(@PathVariable Long contactID) {
        Contact readContact = contactService.read(contactID);
        if (readContact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readContact);
    }

    @PutMapping("/update")
    public ResponseEntity<Contact> update(@RequestBody Contact contact) {
        if (contact.getContactID() == null) {
            return ResponseEntity.badRequest().build();
        }

        Contact updatedContact = contactService.update(contact);
        if (updatedContact == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedContact);
    }

    @GetMapping("/getAllContacts")
    public ResponseEntity<List<Contact>> getAllContacts() {
        List<Contact> contacts = contactService.getAllContacts();
        if (contacts == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(contacts);
    }

    @DeleteMapping("/delete/{contactID}")
    public void delete(@PathVariable Long contactID) {
         contactService.delete(contactID);
    }
}
