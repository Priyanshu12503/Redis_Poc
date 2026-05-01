package com.ttn.redish.student;

import com.ttn.redish.common.HeavyPayload;
import com.ttn.redish.common.HeavyPayloadJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false)
    private String occupation;

    @Convert(converter = HeavyPayloadJsonConverter.class)
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private HeavyPayload payload;

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

    public HeavyPayload getPayload() {
        return payload;
    }

    public void setPayload(HeavyPayload payload) {
        this.payload = payload;
    }
}
