package com.ttn.redish.student;

import com.ttn.redish.student.cache.StudentAllCache;
import com.ttn.redish.student.cache.StudentAllCacheRepository;
import com.ttn.redish.student.cache.StudentCache;
import com.ttn.redish.student.cache.StudentCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private static final String ALL_STUDENTS_CACHE_ID = "all_students_key";
    private static final Logger log = LoggerFactory.getLogger(StudentService.class);

    private final StudentRepository studentRepository;
    private final StudentCacheRepository studentCacheRepository;
    private final StudentAllCacheRepository studentAllCacheRepository;

    public StudentService(StudentRepository studentRepository,
                          StudentCacheRepository studentCacheRepository,
                          StudentAllCacheRepository studentAllCacheRepository) {
        this.studentRepository = studentRepository;
        this.studentCacheRepository = studentCacheRepository;
        this.studentAllCacheRepository = studentAllCacheRepository;
    }

    public Student createStudent(CreateStudentRequest request) {
        long start = System.nanoTime();
        log.info("START StudentService.createStudent");
        try {
            Student student = new Student();
            student.setName(request.name());
            student.setAge(request.age());
            student.setOccupation(request.occupation());
            student.setPayload(request.payload());

            Student saved = studentRepository.save(student);

            // studentCacheRepository.save(StudentCache.fromStudent(saved));
            studentAllCacheRepository.deleteById(ALL_STUDENTS_CACHE_ID);

            return saved;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END StudentService.createStudent - elapsedMs={}", elapsedMs);
        }
    }

    public Student getStudentById(Long id) {
        Optional<StudentCache> cached = studentCacheRepository.findById(id);
        if (cached.isPresent()) {
            return cached.get().toStudent();
        }

        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            studentCacheRepository.save(StudentCache.fromStudent(student));
        }
        return student;
    }

    public Student updateStudent(Long id, String name) {
        long start = System.nanoTime();
        log.info("START StudentService.updateStudent");
        try {
            Student student = studentRepository.findById(id).orElse(null);
            if (student != null) {
                student.setName(name);
                Student updated = studentRepository.save(student);
                studentCacheRepository.save(StudentCache.fromStudent(updated));
                studentAllCacheRepository.deleteById(ALL_STUDENTS_CACHE_ID);
                return updated;
            }
            return null;
        } finally {
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            log.info("END StudentService.updateStudent - elapsedMs={}", elapsedMs);
        }
    }

    public List<Student> getAllStudents() {
        Optional<StudentAllCache> allCache = studentAllCacheRepository.findById(ALL_STUDENTS_CACHE_ID);
        if (allCache.isPresent()) {
            List<StudentCache> cachedStudents = allCache.get().getStudents();
            if (cachedStudents != null) {
                return cachedStudents.stream().map(StudentCache::toStudent).toList();
            }
        }

        List<Student> students = studentRepository.findAll();
        List<StudentCache> studentCaches = students.stream().map(StudentCache::fromStudent).toList();
        studentCacheRepository.saveAll(studentCaches);

        studentAllCacheRepository.save(StudentAllCache.fromStudentCaches(ALL_STUDENTS_CACHE_ID, studentCaches));

        System.out.println("this is");

        return students;

    }
}
