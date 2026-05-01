package com.ttn.redish.bench;

import com.ttn.redish.common.HeavyPayload;
import com.ttn.redish.student.Student;
import com.ttn.redish.student.cache.StudentCache;
import com.ttn.redish.student.cache.StudentCacheRepository;
import com.ttn.redish.user.User;
import com.ttn.redish.user.UserService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class BenchService {

    private static final String REDIS_ONLY_PREFIX = "bench:redis_only:";
    private static final String HUMAN_TEMPLATE_PREFIX = "bench:human_template:id:";
    private static final Duration HUMAN_TEMPLATE_TTL = Duration.ofMinutes(90);

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserService userService;
    private final CacheManager cacheManager;
    private final StudentCacheRepository studentCacheRepository;
    private final AtomicLong studentRepoIdSequence;
    private final AtomicLong humanTemplateIdSequence;

    public BenchService(RedisTemplate<String, Object> redisTemplate,
                        UserService userService,
                        CacheManager cacheManager,
                        StudentCacheRepository studentCacheRepository) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.cacheManager = cacheManager;
        this.studentCacheRepository = studentCacheRepository;
        this.studentRepoIdSequence = new AtomicLong(1_000_000L);
        this.humanTemplateIdSequence = new AtomicLong(2_000_000L);
    }

    public String redisOnlyWrite(Object payload) {
        String key = REDIS_ONLY_PREFIX + UUID.randomUUID();
        redisTemplate.opsForValue().set(key, payload);
        return key;
    }

    public Object redisOnlyRead(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public User getUserCacheMiss(Long id) {
        Cache cache = cacheManager.getCache("userById");
        if (cache != null) {
            cache.evict(id);
        }
        return userService.getUserById(id);
    }

    public User getUserCacheHit(Long id) {
        User warmup = userService.getUserById(id);
        if (warmup == null) {
            return null;
        }
        return userService.getUserById(id);
    }

    public Long studentRepoWrite(HeavyPayload payload) {
        long id = studentRepoIdSequence.getAndIncrement();
        Student student = new Student();
        student.setId(id);
        student.setName("bench_student_" + id);
        student.setAge(27);
        student.setOccupation("Benchmark");
        student.setPayload(payload);
        studentCacheRepository.save(StudentCache.fromStudent(student));
        return id;
    }

    public Student studentRepoRead(Long id) {
        return studentCacheRepository.findById(id).map(StudentCache::toStudent).orElse(null);
    }

    public String humanTemplateWrite(HeavyPayload payload) {
        long id = humanTemplateIdSequence.getAndIncrement();
        String key = HUMAN_TEMPLATE_PREFIX + id;
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("id", id);
        value.put("name", "bench_human_" + id);
        value.put("age", 30);
        value.put("occupation", "Benchmark");
        value.put("payload", payload);
        redisTemplate.opsForValue().set(key, value, HUMAN_TEMPLATE_TTL);
        return key;
    }

    public Object humanTemplateRead(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
