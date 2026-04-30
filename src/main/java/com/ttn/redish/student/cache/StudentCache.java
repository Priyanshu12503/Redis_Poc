package com.ttn.redish.student.cache;

import com.ttn.redish.student.Student;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash(value = "student_by_id", timeToLive = 1800L)
public class StudentCache {

    @Id
    private Long id;
    private String name;
    private Integer age;
    private String occupation;

    public static StudentCache fromStudent(Student student) {
        StudentCache cache = new StudentCache();
        cache.setId(student.getId());
        cache.setName(student.getName());
        cache.setAge(student.getAge());
        cache.setOccupation(student.getOccupation());
        return cache;
    }

    public Student toStudent() {
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);
        student.setOccupation(occupation);
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
}
