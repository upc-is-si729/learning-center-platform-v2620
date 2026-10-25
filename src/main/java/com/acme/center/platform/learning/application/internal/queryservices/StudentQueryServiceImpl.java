package com.acme.center.platform.learning.application.internal.queryservices;

import com.acme.center.platform.learning.application.queryservices.StudentQueryService;
import com.acme.center.platform.learning.domain.model.aggregates.Student;
import com.acme.center.platform.learning.domain.model.queries.ExistsByAcmeStudentRecordIdQuery;
import com.acme.center.platform.learning.domain.model.queries.GetStudentByAcmeStudentRecordIdQuery;
import com.acme.center.platform.learning.domain.model.queries.GetStudentByProfileIdQuery;
import com.acme.center.platform.learning.domain.repositories.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Application service that resolves `student` read queries.
 */
@Service
public class StudentQueryServiceImpl implements StudentQueryService {
    private final StudentRepository studentRepository;

    public StudentQueryServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    public Optional<Student> handle(GetStudentByAcmeStudentRecordIdQuery query) {
        return studentRepository.findByAcmeStudentRecordId(query.studentRecordId());
    }

    @Override
    public Optional<Student> handle(GetStudentByProfileIdQuery query) {
        return studentRepository.findByProfileId(query.profileId());
    }

    @Override
    public boolean handle(ExistsByAcmeStudentRecordIdQuery query) {
        return studentRepository.existsByAcmeStudentRecordId(query.studentRecordId());
    }
}
