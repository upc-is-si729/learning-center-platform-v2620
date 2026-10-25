package com.acme.center.platform.learning.application.queryservices;

import com.acme.center.platform.learning.domain.model.aggregates.Student;
import com.acme.center.platform.learning.domain.model.queries.ExistsByAcmeStudentRecordIdQuery;
import com.acme.center.platform.learning.domain.model.queries.GetStudentByAcmeStudentRecordIdQuery;
import com.acme.center.platform.learning.domain.model.queries.GetStudentByProfileIdQuery;

import java.util.Optional;

/**
 * Application service contract for `student` read queries.
 */
public interface StudentQueryService {
    /**
     * Handles retrieval of a student by profile id.
     *
     * @param query profile-id query
     * @return matching student, if found
     * @see GetStudentByProfileIdQuery
     */
    Optional<Student> handle(GetStudentByProfileIdQuery query);

    /**
     * Handles retrieval of a student by ACME student record id.
     *
     * @param query student-record-id query
     * @return matching student, if found
     * @see GetStudentByAcmeStudentRecordIdQuery
     */
    Optional<Student> handle(GetStudentByAcmeStudentRecordIdQuery query);

    /**
     * Handles student existence verification by ACME student record id.
     *
     * @param query student-record-id query
     * @return {@code true} when student exists; otherwise {@code false}
     * @see ExistsByAcmeStudentRecordIdQuery
     */
    boolean handle(ExistsByAcmeStudentRecordIdQuery query);
}

