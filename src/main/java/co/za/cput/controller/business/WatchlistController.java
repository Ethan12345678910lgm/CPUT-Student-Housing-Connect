package co.za.cput.controller.business;

import co.za.cput.domain.business.WatchlistItem;
import co.za.cput.service.business.IWatchlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final IWatchlistService watchlistService;

    public WatchlistController(IWatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, Long> request) {
        Long studentId = request.get("studentId");
        Long accommodationId = request.get("accommodationId");

        if (studentId == null || accommodationId == null) {
            return ResponseEntity.badRequest().body("Student id and accommodation id are required.");
        }

        try {
            WatchlistItem item = watchlistService.addToWatchlist(studentId, accommodationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(item);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<WatchlistItem>> list(@PathVariable Long studentId) {
        List<WatchlistItem> items = watchlistService.findForStudent(studentId);
        if (items.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(items);
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@RequestParam Long studentId, @RequestParam Long accommodationId) {
        watchlistService.removeFromWatchlist(studentId, accommodationId);
        return ResponseEntity.noContent().build();
    }
}