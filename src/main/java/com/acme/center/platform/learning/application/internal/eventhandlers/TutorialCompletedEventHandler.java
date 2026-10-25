package com.acme.center.platform.learning.application.internal.eventhandlers;

import com.acme.center.platform.learning.application.commandservices.StudentCommandService;
import com.acme.center.platform.learning.application.queryservices.EnrollmentQueryService;
import com.acme.center.platform.learning.domain.model.commands.UpdateStudentMetricsOnTutorialCompletedCommand;
import com.acme.center.platform.learning.domain.model.events.TutorialCompletedEvent;
import com.acme.center.platform.learning.domain.model.queries.GetEnrollmentByIdQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Event handler for the TutorialCompletedEvent.
 */
@Service
@Slf4j
public class TutorialCompletedEventHandler {
    private final StudentCommandService studentCommandService;
    private final EnrollmentQueryService enrollmentQueryService;

    /**
     * Constructor.
     *
     * @param studentCommandService the student command service
     * @param enrollmentQueryService the enrollment query service
     * @see StudentCommandService
     * @see EnrollmentQueryService
     */
    public TutorialCompletedEventHandler(StudentCommandService studentCommandService, EnrollmentQueryService enrollmentQueryService) {
        this.studentCommandService = studentCommandService;
        this.enrollmentQueryService = enrollmentQueryService;
    }

    /**
     * Handles the TutorialCompletedEvent.
     * <p>
     *     Updates the student metrics when a tutorial is completed. The student metrics are updated by the
     *     {@link UpdateStudentMetricsOnTutorialCompletedCommand}.
     * </p>
     *
     * @param event The {@link TutorialCompletedEvent} event
     */
    @EventListener(TutorialCompletedEvent.class)
    public void on(TutorialCompletedEvent event) {
        var getEnrollmentByIdQuery = new GetEnrollmentByIdQuery(event.getEnrollmentId());
        var enrollment = enrollmentQueryService.handle(getEnrollmentByIdQuery);
        if (enrollment.isPresent()) {
            var studentEnrollment = enrollment.get();
            var updateStudentMetricsOnTutorialCompletedCommand = new UpdateStudentMetricsOnTutorialCompletedCommand(studentEnrollment.getAcmeStudentRecordId());
            var result = studentCommandService.handle(updateStudentMetricsOnTutorialCompletedCommand);

            if (result instanceof com.acme.center.platform.shared.application.result.Result.Failure(var error)) {
                log.warn("Failed to update student metrics for enrollment {}: {}",
                        studentEnrollment.getId(), error.message());
            }
        }

    }
}
