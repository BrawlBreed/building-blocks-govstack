package com.alibou.school;

import com.alibou.school.client.StudentClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository repository;
    private final StudentClient client;

    public void saveSchool(School school) {
        repository.save(school);
    }

    public List<School> findAllSchools() {
        return repository.findAll();
    }

    public FullSchoolResponse findSchoolsWithStudents(Integer schoolId) {
        var school = repository.findById(schoolId)
                .orElseThrow(() -> new RuntimeException("School with ID " + schoolId + " not found"));

        List<Student> students;
        try {
            students = client.findAllStudentsBySchool(schoolId);
        } catch (Exception e) {
            students = Collections.emptyList(); // Prevent failure if students service is down
        }

        return FullSchoolResponse.builder()
                .name(school.getName())
                .email(school.getEmail())
                .students(students)
                .build();
    }
}
