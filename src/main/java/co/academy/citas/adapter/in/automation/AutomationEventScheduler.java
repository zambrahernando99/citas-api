package co.academy.citas.adapter.in.automation;

import co.academy.citas.application.port.in.AutomationOperationsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Despacho periódico del outbox WF-002. Se desactiva con app.automation.dispatch-enabled=false (pruebas). */
@Component
@ConditionalOnProperty(prefix = "app.automation", name = "dispatch-enabled", havingValue = "true", matchIfMissing = true)
public class AutomationEventScheduler {
    private static final Logger log = LoggerFactory.getLogger(AutomationEventScheduler.class);
    private final AutomationOperationsUseCase useCase;

    public AutomationEventScheduler(AutomationOperationsUseCase useCase) { this.useCase = useCase; }

    @Scheduled(fixedDelayString = "${app.automation.dispatch-interval-ms:30000}", initialDelayString = "${app.automation.dispatch-interval-ms:30000}")
    void dispatch() {
        var result = useCase.dispatchPendingEvents();
        if (result.delivered() + result.retried() + result.failed() > 0)
            log.info("WF-002 dispatch: delivered={} retried={} failed={}", result.delivered(), result.retried(), result.failed());
    }
}
