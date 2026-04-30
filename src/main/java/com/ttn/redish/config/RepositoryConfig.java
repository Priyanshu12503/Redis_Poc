package com.ttn.redish.config;

import com.ttn.redish.human.HumanRepository;
import com.ttn.redish.student.StudentRepository;
import com.ttn.redish.student.cache.StudentAllCacheRepository;
import com.ttn.redish.student.cache.StudentCacheRepository;
import com.ttn.redish.user.UserRepository;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableJpaRepositories(
        basePackageClasses = {
                HumanRepository.class,
                StudentRepository.class,
                UserRepository.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.ttn\\.redish\\.student\\.cache\\..*"
        )
)
@EnableRedisRepositories(basePackageClasses = {
        StudentCacheRepository.class,
        StudentAllCacheRepository.class
})
public class RepositoryConfig {
}
