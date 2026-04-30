package com.ttn.redish.human;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class HumanService {

    private static final String ALL_HUMANS_CACHE_KEY = "human:all";
    private static final String HUMAN_BY_ID_CACHE_PREFIX = "human:id:";
    private static final Duration HUMAN_CACHE_TTL = Duration.ofMinutes(90);

    private final HumanRepository humanRepository;
    private final RedisTemplate<String, Object> humanRedisTemplate;

    public HumanService(HumanRepository humanRepository,
                      RedisTemplate<String, Object> humanRedisTemplate) {
        this.humanRepository = humanRepository;
        this.humanRedisTemplate = humanRedisTemplate;
    }

    public Human createHuman(CreateHumanRequest request) {
        Human human = new Human();
        human.setName(request.name());
        human.setAge(request.age());
        human.setOccupation(request.occupation());

        Human saved = humanRepository.save(human);

        humanRedisTemplate.opsForValue().set(buildByIdKey(saved.getId()), saved, HUMAN_CACHE_TTL);
        humanRedisTemplate.delete(ALL_HUMANS_CACHE_KEY);

        return saved;
    }

    public Human getHumanById(Long id) {
        String cacheKey = buildByIdKey(id);
        Object cached = humanRedisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof Human human) {
            return human;
        }

        Human human = humanRepository.findById(id).orElse(null);
        if (human != null) {
            humanRedisTemplate.opsForValue().set(cacheKey, human, HUMAN_CACHE_TTL);
        }
        return human;
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
