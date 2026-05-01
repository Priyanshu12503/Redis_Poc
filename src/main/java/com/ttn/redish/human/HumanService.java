package com.ttn.redish.human;

import org.springframework.data.redis.core.RedisTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class HumanService {

    private static final String ALL_HUMANS_CACHE_KEY = "human:all";
    private static final String HUMAN_BY_ID_CACHE_PREFIX = "human:id:";
    private static final String HUMAN_HASH_FIELD = "data";
    private static final Duration HUMAN_CACHE_TTL = Duration.ofMinutes(90);
    private static final Logger log = LoggerFactory.getLogger(HumanService.class);

    private final HumanRepository humanRepository;
    private final RedisTemplate<String, Object> humanRedisTemplate;

    public HumanService(HumanRepository humanRepository,
                      RedisTemplate<String, Object> humanRedisTemplate) {
        this.humanRepository = humanRepository;
        this.humanRedisTemplate = humanRedisTemplate;
    }

    public Human createHuman(CreateHumanRequest request) {
        long start = System.nanoTime();
        log.info("START HumanService.createHuman");
        try {
            Human human = new Human();
            human.setName(request.name());
            human.setAge(request.age());
            human.setOccupation(request.occupation());
            human.setPayload(request.payload());

            Human saved = humanRepository.save(human);

            String humanByIdKey = buildByIdKey(saved.getId());
            humanRedisTemplate.opsForHash().put(humanByIdKey, HUMAN_HASH_FIELD, saved);
            humanRedisTemplate.expire(humanByIdKey, HUMAN_CACHE_TTL);
            humanRedisTemplate.delete(ALL_HUMANS_CACHE_KEY);

            return saved;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END HumanService.createHuman - elapsedMs={}", elapsedMs);
        }
    }

    public Human getHumanById(Long id) {
        String cacheKey = buildByIdKey(id);
        Object cached = humanRedisTemplate.opsForHash().get(cacheKey, HUMAN_HASH_FIELD);
        if (cached instanceof Human human) {
            return human;
        }

        Human human = humanRepository.findById(id).orElse(null);
        if (human != null) {
            humanRedisTemplate.opsForHash().put(cacheKey, HUMAN_HASH_FIELD, human);
            humanRedisTemplate.expire(cacheKey, HUMAN_CACHE_TTL);
        }
        return human;
    }

    public Human updateHuman(Long id, String name) {
        long start = System.nanoTime();
        log.info("START HumanService.updateHuman");
        try {
            Human human = humanRepository.findById(id).orElse(null);
            if (human != null) {
                human.setName(name);
                Human updated = humanRepository.save(human);
                String humanByIdKey = buildByIdKey(id);
                humanRedisTemplate.opsForHash().put(humanByIdKey, HUMAN_HASH_FIELD, updated);
                humanRedisTemplate.expire(humanByIdKey, HUMAN_CACHE_TTL);
                humanRedisTemplate.delete(ALL_HUMANS_CACHE_KEY);
                return updated;
            }
            return null;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END HumanService.updateHuman - elapsedMs={}", elapsedMs);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Human> getAllHumans() {
        Object cached = humanRedisTemplate.opsForValue().get(ALL_HUMANS_CACHE_KEY);
        if (cached instanceof List<?> cachedList) {
            return (List<Human>) cachedList;
        }

        List<Human> humans = humanRepository.findAll();
        humanRedisTemplate.opsForValue().set(ALL_HUMANS_CACHE_KEY, humans, HUMAN_CACHE_TTL);
        for (Human human : humans) {
            humanRedisTemplate.opsForValue().set(buildByIdKey(human.getId()), human, HUMAN_CACHE_TTL);
        }
        return humans;
    }

    private String buildByIdKey(Long id) {
        return HUMAN_BY_ID_CACHE_PREFIX + id;
    }
}
