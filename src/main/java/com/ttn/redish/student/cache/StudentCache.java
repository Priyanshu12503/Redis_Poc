package com.ttn.redish.student.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ttn.redish.common.HeavyPayload;
import com.ttn.redish.student.Student;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "student_by_id", timeToLive = 1800L)
public class StudentCache {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    private Long id;
    private String name;
    private Integer age;
    private String occupation;
    private String payloadJson;

    public static StudentCache fromStudent(Student student) {
        StudentCache cache = new StudentCache();
        cache.setId(student.getId());
        cache.setName(student.getName());
        cache.setAge(student.getAge());
        cache.setOccupation(student.getOccupation());
        cache.setPayloadJson(serializePayload(student.getPayload()));
        return cache;
    }

    public Student toStudent() {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        student.setOccupation(occupation);
        student.setPayload(deserializePayload(payloadJson));
        return student;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    private static String serializePayload(HeavyPayload payload) {
        try {
            return OBJECT_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize student payload for cache", e);
        }
    }

    private static HeavyPayload deserializePayload(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(payloadJson, HeavyPayload.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize student payload from cache", e);
        }
    }
}
