package com.ttn.redish.student.cache;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.util.List;

@RedisHash(value = "students_all", timeToLive = 1800L)
public class StudentAllCache {

    @Id
    private String id;
    private List<StudentCache> students;

    public StudentAllCache() {
    }

    public StudentAllCache(String id, List<StudentCache> students) {
        this.id = id;
        this.students = students;
    }

    public static StudentAllCache fromStudentCaches(String id, List<StudentCache> students) {
        return new StudentAllCache(id, students);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<StudentCache> getStudents() {
        return students;
    }

    public void setStudents(List<StudentCache> students) {
        this.students = students;
    }
}
