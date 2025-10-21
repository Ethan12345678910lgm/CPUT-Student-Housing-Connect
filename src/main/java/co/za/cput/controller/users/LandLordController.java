package co.za.cput.controller.users;
//Firstname:        Sinhle Xiluva
//LastName:         Mthethwa
//Student Number:   221802797.

import co.za.cput.domain.users.Landlord;
import co.za.cput.service.users.implementation.LandLordServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(originPatterns = {"http://localhost:*", "http://127.0.0.1:*"})
@RequestMapping({"/api/landlords", "/HouseConnect/Landlord"})
public class LandLordController {

    private final LandLordServiceImpl landLordService;

    @Autowired
    public LandLordController(LandLordServiceImpl landLordService) {
        this.landLordService = landLordService;
    }

    @PostMapping("/create")
    public ResponseEntity<Landlord> create(@RequestBody Landlord landlord) {
        Landlord created = landLordService.create(landlord);
        return ResponseEntity.ok(created);
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
