package co.za.cput.service.users.implementation;
//Firstname:        Sinhle Xiluva
//LastName:         Mthethwa
//Student Number:   221802797.

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Landlord;
import co.za.cput.repository.business.AccommodationRepository;
import co.za.cput.repository.users.LandLordRepository;
import co.za.cput.service.users.ILandLordService;
import co.za.cput.util.Helper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Locale;

@Service
public class LandLordServiceImpl implements ILandLordService {

    private LandLordRepository landLordRepository;
    private AccommodationRepository accommodationRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public LandLordServiceImpl(LandLordRepository landLordRepository,
                               AccommodationRepository accommodationRepository,
                               PasswordEncoder passwordEncoder) {
        this.landLordRepository = landLordRepository;
        this.accommodationRepository = accommodationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public Landlord create(Landlord landlord) {
        Landlord securedLandlord = secureLandlord(landlord);
        if (securedLandlord == null) {
            throw new IllegalArgumentException("Landlord details are required.");
        }

        Contact contact = securedLandlord.getContact();
        if (contact != null && !Helper.isNullorEmpty(contact.getEmail())
                && landLordRepository.existsByContact_EmailIgnoreCase(contact.getEmail())) {
            throw new IllegalArgumentException("A landlord with this email already exists.");
        }

        // Step 1: Save landlord without accommodations
        Landlord savedLandlord = new Landlord.Builder()
                .copy(securedLandlord)
                .setAccommodationList(null)
                .build();

        savedLandlord = landLordRepository.saveAndFlush(savedLandlord);

        // Step 2: Link accommodations to savedLandlord
        List<Accommodation> linkedAccommodations = new ArrayList<>();
        if (securedLandlord.getAccommodationList() != null) {
            for (Accommodation accommodation : securedLandlord.getAccommodationList()) {
                Accommodation linkedAccommodation = new Accommodation.Builder()
                        .copy(accommodation)
                        .setLandlord(savedLandlord)
                        .build();
                linkedAccommodations.add(accommodationRepository.saveAndFlush(linkedAccommodation)); // Save directly
            }
        }

        // Step 3: Return saved landlord with linked accommodations (optional)
        return savedLandlord;
    }





    @Override
    public Landlord read(Long Id) {
        return landLordRepository.findById(Id).orElse(null);
    }

   

    @Override
    public Landlord update(Landlord landlord) {
        if (landlord == null || landlord.getLandlordID() == null) {
            throw new IllegalArgumentException("Landlord or Landlord ID must not be null");
        }

        Landlord securedLandlord = secureLandlord(landlord);


        Contact contact = securedLandlord.getContact();
        if (contact != null && !Helper.isNullorEmpty(contact.getEmail())) {
            landLordRepository.findFirstByContact_EmailIgnoreCase(contact.getEmail())
                    .filter(existing -> !existing.getLandlordID().equals(securedLandlord.getLandlordID()))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("A landlord with this email already exists.");
                    });
        }
        // Step 1: Fetch existing landlord (managed instance)
        Landlord existing = landLordRepository.findById(securedLandlord.getLandlordID())
                .orElseThrow(() -> new IllegalArgumentException("Landlord not found"));

        // Step 2: Link accommodations to managed landlord
        List<Accommodation> fixedAccommodations = null;
        if (securedLandlord.getAccommodationList() != null) {
            fixedAccommodations = securedLandlord.getAccommodationList().stream()
                    .map(acc -> new Accommodation.Builder()
                            .copy(acc)
                            .setLandlord(existing) // prevent detached instance issue
                            .build())
                    .collect(Collectors.toList());
        }

        // Step 3: Rebuild landlord with fixed accommodations
        Landlord updated = new Landlord.Builder()
                .copy(securedLandlord)
                .setLandlordID(existing.getLandlordID())
                .setAccommodationList(fixedAccommodations)
                .build();

        // Step 4: Save updated landlord
        return landLordRepository.saveAndFlush(updated);
    }

    @Override
    public List<Landlord> getAllLandlords() {
        return landLordRepository.findAll();
    }

    @Override
    public void delete(Long Id) {
        landLordRepository.deleteById(Id);
    }
    private Landlord secureLandlord(Landlord landlord) {
        if (landlord == null) {
            return null;
        }

        Contact contact = sanitiseContact(landlord.getContact());

        return new Landlord.Builder()
                .copy(landlord)
                .setLandlordFirstName(normalise(landlord.getLandlordFirstName()))
                .setLandlordLastName(normalise(landlord.getLandlordLastName()))
                .setPassword(hashPassword(landlord.getPassword()))
                .setContact(contact)
                .build();
    }

    private Contact sanitiseContact(Contact contact) {
        if (contact == null) {
            return null;
        }

        return new Contact.Builder()
                .copy(contact)
                .setEmail(contact.getEmail() != null ? contact.getEmail().trim().toLowerCase(Locale.ROOT) : null)
                .setPhoneNumber(normalise(contact.getPhoneNumber()))
                .setAlternatePhoneNumber(normalise(contact.getAlternatePhoneNumber()))
                .build();
    }

    private String hashPassword(String password) {
        if (password == null || password.isBlank()) {
            return password;
        }
        String trimmed = password.trim();
        if (trimmed.startsWith("$2a$") || trimmed.startsWith("$2b$") || trimmed.startsWith("$2y$")) {
            return trimmed;
        }
        return passwordEncoder.encode(trimmed);
    }

    private String normalise(String value) {
        return value != null ? value.trim() : null;
    }
}
