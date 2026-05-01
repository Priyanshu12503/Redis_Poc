package com.ttn.redish.human;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/humans")
public class HumanController {

    private final HumanService humanService;

    public HumanController(HumanService humanService) {
        this.humanService = humanService;
    }

    @PostMapping
    public ResponseEntity<Human> createHuman(@Valid @RequestBody CreateHumanRequest request) {
        Human created = humanService.createHuman(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public Human getHumanById(@PathVariable Long id) {
        Human human = humanService.getHumanById(id);
        if (human == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Human not found");
        }
        return human;
    }

    @PatchMapping("/{id}")
    public Human updateHuman(@PathVariable Long id, @RequestParam String name) {
        Human updated = humanService.updateHuman(id, name);
        if (updated == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Human not found");
        }
        return updated;
    }

    @GetMapping
    public List<Human> getAllHumans() {
        return humanService.getAllHumans();
    }
}
