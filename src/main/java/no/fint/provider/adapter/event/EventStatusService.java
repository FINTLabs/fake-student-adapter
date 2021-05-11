package no.fint.provider.adapter.event;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.DefaultActions;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.event.model.Status;
import no.fint.provider.adapter.FintAdapterEndpoints;
import no.fint.provider.student.SupportedActions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Handles statuses back to the provider status endpoint.
 */
@Slf4j
@Service
public class EventStatusService {

    @Autowired
    private FintAdapterEndpoints endpoints;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SupportedActions supportedActions;

    /**
     * Verifies if we can handle the event and set the status accordingly.
     *
     *
     * @param component
     * @param event
     * @return The inbound event.
     */
    public Event verifyEvent(String component, Event event) {
        if (supportedActions.supports(event.getAction()) || DefaultActions.getDefaultActions().contains(event.getAction())) {
            event.setStatus(Status.ADAPTER_ACCEPTED);
        } else {
            log.info("Rejecting {}", event.getAction());
            event.setStatus(Status.ADAPTER_REJECTED);
        }

        log.info("{}: Posting status for {} {} ...", component, event.getAction(), event.getCorrId());
        postStatus(component, event);
        return event;
    }

    /**
     * Method for posting back the status to the provider.
     *
     * @param component
     * @param event
     */
    public void postStatus(String component, Event event) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HeaderConstants.ORG_ID, event.getOrgId());
            headers.set(HeaderConstants.CLIENT, event.getClient());
            String url = endpoints.getProviders().get(component) + endpoints.getStatus();
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(event, headers), Void.class);
            log.info("{}: Provider POST status response: {}", component, response.getStatusCode());
        } catch (RestClientException e) {
            log.warn("{}: Provider POST status error: {}", component, e.getMessage());
        }
    }
}
