package com.ttn.redish.user;

import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @CacheEvict(cacheNames = {"userById", "allUsers"}, allEntries = true)
    public User createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setAge(request.age());
        user.setOccupation(request.occupation());
        return userRepository.save(user);
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
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setName(name);
            return userRepository.save(user);
        }
        return user;
    }

    @Cacheable(cacheNames = "allUsers")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
