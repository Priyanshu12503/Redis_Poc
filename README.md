# Redish - Redis Learning POC (Spring Boot)

This project demonstrates Redis usage patterns in a single Spring Boot app:

- Cache-aside using `@Cacheable/@CachePut/@CacheEvict` (`User`)
- Cache-aside using Spring Data Redis repositories (`Student`)
- Manual RedisTemplate caching with explicit keys (`Human`)
- Redis Pub/Sub producer/consumer flow (`Eval`)

## Tech Stack

- Java 21
- Spring Boot 4
- Spring Data JPA + MySQL
- Spring Data Redis + Lettuce

## TTL Strategy (Separate Per Domain)

- Student cache: 30 minutes
  - `@RedisHash(value = "...", timeToLive = 1800L)`
- User cache: 1 hour
  - Redis `CacheManager` per-cache TTL
- Human cache: 90 minutes
  - `RedisTemplate.opsForValue().set(..., Duration.ofMinutes(90))`

## Prerequisites

- Java 21 active in terminal
- Redis running on `localhost:6379`
- MySQL running and accessible

## Run

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH

./mvnw spring-boot:run
```

Default app port: `8091`

## Environment Variables

You can override all important settings:

- `SERVER_PORT` (default `8091`)
- `DB_URL` (default `jdbc:mysql://localhost:3306/redish?createDatabaseIfNotExist=true`)
- `DB_USERNAME` (default `priyanshu`)
- `DB_PASSWORD` (default `coffee`)
- `REDIS_HOST` (default `localhost`)
- `REDIS_PORT` (default `6379`)
- `DDL_AUTO` (default `update`)
- `SHOW_SQL` (default `true`)

## Demo Script (POC Flow)

### 1) User Cache (`@Cacheable`)

```bash
curl -X POST http://localhost:8091/api/users \
  -H "Content-Type: application/json" \
  -d '{"name":"Aman","age":24,"occupation":"Engineer"}'

curl http://localhost:8091/api/users/1
curl http://localhost:8091/api/users/1
```

Second GET should hit Redis cache.

### 2) Student Cache (Redis repositories + 30 min TTL)

```bash
curl -X POST http://localhost:8091/api/students \
  -H "Content-Type: application/json" \
  -d '{"name":"Riya","age":22,"occupation":"Analyst"}'

curl http://localhost:8091/api/students/1
curl http://localhost:8091/api/students/1
```

### 3) Human Cache (RedisTemplate + 90 min TTL)

```bash
curl -X POST http://localhost:8091/api/humans \
  -H "Content-Type: application/json" \
  -d '{"name":"Kabir","age":30,"occupation":"Architect"}'

curl http://localhost:8091/api/humans/1
curl http://localhost:8091/api/humans/1
```

### 4) Pub/Sub Demo

```bash
curl -X POST "http://localhost:8091/eval/pubsub/publish?message=HelloRedis"
```

Subscriber log should show message receipt.

## API Summary

- `POST /api/users`
- `GET /api/users/{id}`
- `PATCH /api/users/{id}?name=...`
- `GET /api/users`

- `POST /api/students`
- `GET /api/students/{id}`
- `PATCH /api/students/{id}?name=...`
- `GET /api/students`

- `POST /api/humans`
- `GET /api/humans/{id}`
- `GET /api/humans`

- `POST /eval/pubsub/publish?message=...`

## Validation & Errors

- Bean validation is enabled on create requests.
- Standard JSON error response is returned for:
  - validation failures (`400`)
  - not found (`404`)
