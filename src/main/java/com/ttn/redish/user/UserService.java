package com.ttn.redish.user;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @CacheEvict(cacheNames = {"userById", "allUsers"}, allEntries = true)
    public User createUser(CreateUserRequest request) {
        long start = System.nanoTime();
        log.info("START UserService.createUser");
        try {
            User user = new User();
            user.setName(request.name());
            user.setAge(request.age());
            user.setOccupation(request.occupation());
            user.setPayload(request.payload());
            return userRepository.save(user);
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END UserService.createUser - elapsedMs={}", elapsedMs);
        }
    }

    @Cacheable(cacheNames = "userById", key = "#id", unless = "#result == null")
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }


    @Caching(
            put = @CachePut(cacheNames = "userById", key = "#id", unless = "#result == null"),
            evict = @CacheEvict(cacheNames = "allUsers", allEntries = true)
    )
    public User updateUser(Long id, String name) {
        long start = System.nanoTime();
        log.info("START UserService.updateUser");
        try {
            User user = userRepository.findById(id).orElse(null);
            if (user != null) {
                user.setName(name);
                return userRepository.save(user);
            }
            return user;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END UserService.updateUser - elapsedMs={}", elapsedMs);
        }
    }

    @Cacheable(cacheNames = "allUsers")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
