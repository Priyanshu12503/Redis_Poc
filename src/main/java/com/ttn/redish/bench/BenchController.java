package com.ttn.redish.bench;

import com.ttn.redish.common.HeavyPayload;
import com.ttn.redish.student.Student;
import com.ttn.redish.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/bench")
public class BenchController {

    private final BenchService benchService;

    public BenchController(BenchService benchService) {
        this.benchService = benchService;
    }

    @PostMapping("/redis-only/write")
    public ResponseEntity<Map<String, String>> redisOnlyWrite(@RequestBody Object payload) {
        String key = benchService.redisOnlyWrite(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("key", key));
    }

    @GetMapping("/redis-only/read/{key}")
    public Object redisOnlyRead(@PathVariable String key) {
        Object value = benchService.redisOnlyRead(key);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Key not found");
        }
        return value;
    }

    @GetMapping("/cache-miss/users/{id}")
    public User userCacheMiss(@PathVariable Long id) {
        User user = benchService.getUserCacheMiss(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @GetMapping("/cache-hit/users/{id}")
    public User userCacheHit(@PathVariable Long id) {
        User user = benchService.getUserCacheHit(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    @PostMapping("/student-repo/write")
    public ResponseEntity<Map<String, Long>> studentRepoWrite(@RequestBody HeavyPayload payload) {
        Long id = benchService.studentRepoWrite(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", id));
    }

    @GetMapping("/student-repo/read/{id}")
    public Student studentRepoRead(@PathVariable Long id) {
        Student student = benchService.studentRepoRead(id);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student cache not found");
        }
        return student;
    }

    @PostMapping("/human-template/write")
    public ResponseEntity<Map<String, String>> humanTemplateWrite(@RequestBody HeavyPayload payload) {
        String key = benchService.humanTemplateWrite(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("key", key));
    }

    @GetMapping("/human-template/read/{key}")
    public Object humanTemplateRead(@PathVariable String key) {
        Object value = benchService.humanTemplateRead(key);
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Human template cache not found");
        }
        return value;
    }
}
