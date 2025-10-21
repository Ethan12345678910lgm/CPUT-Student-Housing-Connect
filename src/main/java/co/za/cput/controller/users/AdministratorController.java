package co.za.cput.controller.users;

import co.za.cput.domain.business.Verification;
import co.za.cput.domain.users.Administrator;
import co.za.cput.domain.users.Landlord;
import co.za.cput.dto.AdminApprovalRequest;
import co.za.cput.dto.LandlordVerificationRequest;
import co.za.cput.dto.ListingVerificationRequest;
import co.za.cput.service.users.implementation.AdministratorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/admins", "/HouseConnect/Administrator"})
public class AdministratorController {

    private final AdministratorServiceImpl administratorService;

    @Autowired
    public AdministratorController(AdministratorServiceImpl administratorService) {
        this.administratorService = administratorService;
    }

    @PostMapping("/create")
    public ResponseEntity<Administrator> create(
            @RequestBody Administrator administrator,
            @RequestParam(value = "creatorAdminId", required = false) Long creatorAdminId,
            @RequestParam(value = "creatorPassword", required = false) String creatorPassword) {

        if (administrator == null) {
            return ResponseEntity.badRequest().build();
        }

        if (administratorService.hasAnyAdministrators()) {
            if (creatorAdminId == null || creatorPassword == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            Administrator creator = administratorService.authenticateAdmin(creatorAdminId, creatorPassword);
            if (creator == null || !creator.isSuperAdmin()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            administrator = new Administrator.Builder()
                    .copy(administrator)
                    .setSuperAdmin(false)
                    .build();
        }

        Administrator created = administratorService.create(administrator);
        return ResponseEntity.ok(created);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(@RequestBody Administrator administrator) {
        if (administrator == null) {
            return ResponseEntity.badRequest().body("Administrator application is required.");
        }

        try {
            Administrator created = administratorService.submitApplication(administrator);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/applications")
    public ResponseEntity<?> listPendingApplications(
            @RequestParam("superAdminId") Long superAdminId) {

        if (superAdminId == null) {
            return ResponseEntity.badRequest().body("Super administrator id is required.");
        }

        try {
            List<Administrator> pending = administratorService.getPendingAdministrators(superAdminId);
            if (pending.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(pending);
        } catch (IllegalArgumentException exception) {
            if ("Invalid super administrator credentials.".equals(exception.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exception.getMessage());
            }
            return ResponseEntity.badRequest().body(exception.getMessage());
        }
    }

    @PostMapping("/{applicantId}/approve")
    public ResponseEntity<?> approve(
            @PathVariable Long applicantId,
            @RequestBody AdminApprovalRequest request
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Approval request is required.");
        }

        if (request.getSuperAdminId() == null) {
            return ResponseEntity.badRequest().body("Super administrator id is required.");
        }

        try {
            Administrator approved = administratorService.approveAdministrator(
                    applicantId,
                    request.getSuperAdminId()
            );
            return ResponseEntity.ok(approved);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "Invalid super administrator credentials.".equals(exception.getMessage())
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.BAD_REQUEST;
            if ("Administrator not found.".equals(exception.getMessage())) {
                status = HttpStatus.NOT_FOUND;
            }
            return ResponseEntity.status(status).body(exception.getMessage());
        }
    }

    @PostMapping("/{applicantId}/reject")
    public ResponseEntity<?> reject(
            @PathVariable Long applicantId,
            @RequestBody AdminApprovalRequest request
    ) {
        if (request == null) {
            return ResponseEntity.badRequest().body("Approval request is required.");
        }

        if (request.getSuperAdminId() == null) {
            return ResponseEntity.badRequest().body("Super administrator id is required.");
        }

        try {
            Administrator rejected = administratorService.rejectAdministrator(
                    applicantId,
                    request.getSuperAdminId()
            );
            return ResponseEntity.ok(rejected);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "Invalid super administrator credentials.".equals(exception.getMessage())
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.BAD_REQUEST;
            if ("Administrator not found.".equals(exception.getMessage())) {
                status = HttpStatus.NOT_FOUND;
            }
            return ResponseEntity.status(status).body(exception.getMessage());
        }
    }


    @GetMapping("/read/{Id}")
    public ResponseEntity<Administrator> read(@PathVariable Long Id) {
        Administrator admin = administratorService.read(Id);
        if (admin == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(admin);
    }

    @PutMapping("/update")
    public ResponseEntity<Administrator> update(@RequestBody Administrator administrator) {
        if (administrator.getAdminID() == null) {
            return ResponseEntity.badRequest().build();
        }
        Administrator updated = administratorService.update(administrator);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllAdministrators")
    public ResponseEntity<List<Administrator>> getAllAdministrators() {
        List<Administrator> admins = administratorService.getAllAdministrators();
        if (admins == null || admins.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(admins);
    }

    @DeleteMapping("/delete/{Id}")
    public void delete(@PathVariable Long Id) {
        administratorService.delete(Id);
    }

    @PostMapping("/landlords/{landlordId}/verification")
    public ResponseEntity<?> verifyLandlord(
            @PathVariable Long landlordId,
            @RequestBody LandlordVerificationRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest().body("Verification request is required.");
        }

        try {
            Landlord landlord = administratorService.verifyLandlord(
                    request.getAdminId(),
                    request.getAdminPassword(),
                    landlordId,
                    request.isApproved()
            );
            return ResponseEntity.ok(landlord);
        } catch (IllegalArgumentException exception) {
            return handleAdminActionException(exception);
        }
    }

    @PostMapping("/verifications/{verificationId}/status")
    public ResponseEntity<?> verifyListing(
            @PathVariable Long verificationId,
            @RequestBody ListingVerificationRequest request) {

        if (request == null) {
            return ResponseEntity.badRequest().body("Verification request is required.");
        }

        try {
            Verification verification = administratorService.verifyListing(
                    request.getAdminId(),
                    request.getAdminPassword(),
                    verificationId,
                    request.getStatus(),
                    request.getNotes()
            );
            return ResponseEntity.ok(verification);
        } catch (IllegalArgumentException exception) {
            return handleAdminActionException(exception);
        }
    }

    private ResponseEntity<String> handleAdminActionException(IllegalArgumentException exception) {
        String message = exception.getMessage();
        if ("Invalid administrator credentials.".equals(message)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(message);
        }
        if ("Landlord not found.".equals(message) || "Verification not found.".equals(message)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        }
        if ("Verification status is required.".equals(message)) {
            return ResponseEntity.badRequest().body(message);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }
}
