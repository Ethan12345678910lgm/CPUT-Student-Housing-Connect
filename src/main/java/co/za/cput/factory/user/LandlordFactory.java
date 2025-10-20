package co.za.cput.factory.user;

//Firstname:        Sinhle Xiluva
//LastName:         Mthethwa
//Student Number:   221802797.

import co.za.cput.domain.business.Accommodation;
import co.za.cput.domain.generic.Contact;
import co.za.cput.domain.users.Landlord;
import co.za.cput.util.Helper;

import java.time.LocalDate;
import java.util.List;

public class LandlordFactory {
    public static Landlord createLandlord(String landlordFirstName,
                                   String landlordLastName,
                                   boolean isVerified,
                                   LocalDate dateRegistered,
                                   String password,
                                   Contact contact,
                                   List<Accommodation> accommodationList
    ) {

        if (Helper.isNullorEmpty(landlordFirstName) ||
                Helper.isNullorEmpty(landlordLastName) ||
                !Helper.isValidDate(dateRegistered) ||
                (password != null && !Helper.isValidPassword(password))) {
            return null;
        }

        Landlord.Builder builder = new Landlord.Builder()
                .setLandlordFirstName(landlordFirstName)
                .setLandlordLastName(landlordLastName)
                .setVerified(isVerified)
                .setDateRegistered(dateRegistered)
                .setContact(contact)
                .setAccommodationList(accommodationList);

        if (!Helper.isNullorEmpty(password)) {
            builder.setPassword(password);
        }

        return builder.build();
    }

    public static Landlord createLandlord(String landlordFirstName,
                                          String landlordLastName,
                                          boolean isVerified,
                                          LocalDate dateRegistered,
                                          Contact contact,
                                          List<Accommodation> accommodationList) {
        return createLandlord(landlordFirstName, landlordLastName, isVerified, dateRegistered, null, contact, accommodationList);
    }
}
