package co.za.cput.service.users;

import co.za.cput.domain.business.Verification;
import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.service.IService;

import java.util.List;

public interface IAdministratorService extends IService<Administrator, Long> {
    List<Administrator> getAllAdministrators();

    boolean hasAnyAdministrators();

    Administrator authenticateAdmin(Long adminId, String adminPassword);

    Administrator submitApplication(Administrator administrator);

    Administrator approveAdministrator(Long applicantId, String superAdminEmail, String superAdminPassword);

    List<Administrator> getPendingAdministrators();

    Landlord verifyLandlord(Long adminId, String adminPassword, Long landlordId, boolean approved);

    Verification verifyListing(Long adminId, String adminPassword, Long verificationId,
                               Verification.VerificationStatus status, String notes);
}
