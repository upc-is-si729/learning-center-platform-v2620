package com.acme.center.platform.shared.infrastructure.events;

import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Shared infrastructure service responsible for publishing and clearing domain events
 * registered on {@link AbstractDomainAggregateRoot} instances.
 */
@Component
@NullMarked
public class DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * Constructs a new {@link DomainEventPublisher}.
     *
     * @param applicationEventPublisher the Spring {@link ApplicationEventPublisher}
     */
    public DomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Dispatches all domain events registered on the aggregate root through the Spring
     * application event publisher and clears the aggregate's internal event collection.
     *
     * @param aggregate the domain aggregate root whose events are to be published
     * @param <T> the aggregate root type
     */
    public <T extends AbstractDomainAggregateRoot<T>> void publishAndClearEvents(@Nullable T aggregate) {
        if (aggregate == null) {
            return;
        }
        aggregate.domainEvents().forEach(applicationEventPublisher::publishEvent);
        aggregate.clearDomainEvents();
    }
}
