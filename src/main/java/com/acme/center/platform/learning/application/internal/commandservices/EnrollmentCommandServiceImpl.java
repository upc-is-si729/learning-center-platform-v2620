package com.acme.center.platform.learning.application.internal.commandservices;

import com.acme.center.platform.learning.application.commandservices.EnrollmentCommandService;
import com.acme.center.platform.learning.domain.model.aggregates.Enrollment;
import com.acme.center.platform.learning.domain.model.commands.*;
import com.acme.center.platform.learning.domain.model.valueobjects.CourseId;
import com.acme.center.platform.learning.domain.repositories.CourseRepository;
import com.acme.center.platform.learning.domain.repositories.EnrollmentRepository;
import com.acme.center.platform.learning.domain.repositories.StudentRepository;
import com.acme.center.platform.shared.application.result.ApplicationError;
import com.acme.center.platform.shared.application.result.Result;
import org.springframework.stereotype.Service;

/**
 * Application service that executes enrollment lifecycle commands.
 */
@Service
public class EnrollmentCommandServiceImpl implements EnrollmentCommandService {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentCommandServiceImpl(CourseRepository courseRepository, StudentRepository studentRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public Result<Long, ApplicationError> handle(RequestEnrollmentCommand command) {
        if (!studentRepository.existsByAcmeStudentRecordId(command.studentRecordId())) {
            return Result.failure(ApplicationError.notFound("Student", command.studentRecordId().studentRecordId()));
        }
        var course = courseRepository.findById(command.courseId());
        if (course.isEmpty()) {
            return Result.failure(ApplicationError.notFound("Course", command.courseId().toString()));
        }
        try {
            var enrollment = new Enrollment(command.studentRecordId(), new CourseId(command.courseId()));
            enrollment = enrollmentRepository.save(enrollment);
            return Result.success(enrollment.getId());
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("request-enrollment", e.getMessage()));
        }
    }

    @Override
    public Result<Long, ApplicationError> handle(ConfirmEnrollmentCommand command) {
        return enrollmentRepository.findById(command.enrollmentId()).map(enrollment -> {
            if (enrollment.isConfirmed())
                return Result.<Long, ApplicationError>failure(ApplicationError.conflict("Enrollment", "Enrollment is already confirmed"));
            var course = courseRepository.findById(enrollment.getCourseId().courseId());
            if (course.isEmpty())
                return Result.<Long, ApplicationError>failure(ApplicationError.notFound("Course", enrollment.getCourseId().courseId().toString()));
            enrollment.confirm(course.get().getLearningPath());
            return Result.<Long, ApplicationError>success(enrollmentRepository.save(enrollment).getId());
        }).orElseGet(() -> Result.failure(ApplicationError.notFound("Enrollment", command.enrollmentId().toString())));
    }

    @Override
    public Result<Long, ApplicationError> handle(RejectEnrollmentCommand command) {
        return enrollmentRepository.findById(command.enrollmentId()).map(enrollment -> {
            if (enrollment.isRejected())
                return Result.<Long, ApplicationError>failure(ApplicationError.conflict("Enrollment", "Enrollment is already rejected"));
            enrollment.reject();
            return Result.<Long, ApplicationError>success(enrollmentRepository.save(enrollment).getId());
        }).orElseGet(() -> Result.failure(ApplicationError.notFound("Enrollment", command.enrollmentId().toString())));
    }

    @Override
    public Result<Long, ApplicationError> handle(CancelEnrollmentCommand command) {
        return enrollmentRepository.findById(command.enrollmentId()).map(enrollment -> {
            if (enrollment.isCancelled())
                return Result.<Long, ApplicationError>failure(ApplicationError.conflict("Enrollment", "Enrollment is already cancelled"));
            enrollment.cancel();
            return Result.<Long, ApplicationError>success(enrollmentRepository.save(enrollment).getId());
        }).orElseGet(() -> Result.failure(ApplicationError.notFound("Enrollment", command.enrollmentId().toString())));
    }

    @Override
    public Result<Long, ApplicationError> handle(CompleteTutorialForEnrollmentCommand command) {
        return enrollmentRepository.findById(command.enrollmentId()).map(enrollment -> {
            var course = courseRepository.findById(enrollment.getCourseId().courseId());
            if (course.isEmpty())
                return Result.<Long, ApplicationError>failure(ApplicationError.notFound("Course", enrollment.getCourseId().courseId().toString()));
            enrollment.completeTutorial(command.tutorialId(), course.get().getLearningPath());
            return Result.<Long, ApplicationError>success(enrollmentRepository.save(enrollment).getId());
        }).orElseGet(() -> Result.failure(ApplicationError.notFound("Enrollment", command.enrollmentId().toString())));
    }
}
