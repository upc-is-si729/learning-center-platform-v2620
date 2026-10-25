package com.acme.center.platform.shared.infrastructure.events;

import com.acme.center.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DomainEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private DomainEventPublisher domainEventPublisher;

    @BeforeEach
    void setUp() {
        domainEventPublisher = new DomainEventPublisher(applicationEventPublisher);
    }

    @Test
    void publishAndClearEventsWithNullAggregateShouldDoNothing() {
        assertThatCode(() -> domainEventPublisher.publishAndClearEvents(null))
                .doesNotThrowAnyException();
        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    void publishAndClearEventsWithEmptyEventsShouldDoNothing() {
        var aggregate = new TestAggregate();

        domainEventPublisher.publishAndClearEvents(aggregate);

        verifyNoInteractions(applicationEventPublisher);
        assertThat(aggregate.domainEvents()).isEmpty();
    }

    @Test
    void publishAndClearEventsWithRegisteredEventsShouldPublishAndClear() {
        var aggregate = new TestAggregate();
        var event = new TestEvent("test-payload");
        aggregate.register(event);

        assertThat(aggregate.domainEvents()).hasSize(1);

        domainEventPublisher.publishAndClearEvents(aggregate);

        var captor = ArgumentCaptor.forClass(TestEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().payload()).isEqualTo("test-payload");

        assertThat(aggregate.domainEvents()).isEmpty();
    }

    private static class TestAggregate extends AbstractDomainAggregateRoot<TestAggregate> {
        void register(Object event) {
            registerDomainEvent(event);
        }
    }

    private record TestEvent(String payload) {}
}
