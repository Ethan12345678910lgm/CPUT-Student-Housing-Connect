package co.za.cput.controller.users;
//Firstname:        Sinhle Xiluva
//LastName:         Mthethwa
//Student Number:   221802797.

import co.za.cput.domain.users.Landlord;
import co.za.cput.service.users.implementation.LandLordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(originPatterns = "${app.security.cors.allowed-origin-patterns}")
@RequestMapping({"/api/landlords", "/HouseConnect/Landlord"})
public class LandLordController {

    private final LandLordServiceImpl landLordService;

    @Autowired
    public LandLordController(LandLordServiceImpl landLordService) {
        this.landLordService = landLordService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody Landlord landlord) {
        if (landlord == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Landlord details are required."));
        }

        try {
            Landlord created = landLordService.create(landlord);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException exception) {
            HttpStatus status = "A landlord with this email already exists.".equals(exception.getMessage())
                    ? HttpStatus.CONFLICT
                    : HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).body(Map.of("message", exception.getMessage()));
        }
    }

    @GetMapping("/read/{Id}")
    public ResponseEntity<Landlord> read(@PathVariable Long Id) {
        Landlord landlord = landLordService.read(Id);
        if (landlord == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(landlord);
    }

    @PutMapping("/update")
    public ResponseEntity<Landlord> update(@RequestBody Landlord landlord) {
        if (landlord.getLandlordID() == null) {
            return ResponseEntity.badRequest().build();
        }
        Landlord updated = landLordService.update(landlord);
        if (updated == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/getAllLandlords")
    public ResponseEntity<List<Landlord>> getAllLandlords() {
        List<Landlord> landlords = landLordService.getAllLandlords();
        return ResponseEntity.ok(landlords);
    }

    @DeleteMapping("/delete/{Id}")
    public void delete(@PathVariable Long Id) {
        landLordService.delete(Id);
    }
}
