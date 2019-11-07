package no.fint.provider.adapter.event;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.HeaderConstants;
import no.fint.provider.adapter.FintAdapterEndpoints;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Handles responses back to the provider status endpoint.
 */
@Slf4j
@Service
public class EventResponseService {

    @Autowired
    private FintAdapterEndpoints endpoints;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Method for posting back the response to the provider.
     *
     * @param component
     * @param event Event to post back
     */
    public void postResponse(String component, Event event) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HeaderConstants.ORG_ID, event.getOrgId());
        String url = endpoints.getProviders().get(component) + endpoints.getResponse();
        log.info("{}: Posting response for {} ...", component, event.getAction());
        ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(event, headers), Void.class);
        log.info("{}: Provider POST response: {}", component, response.getStatusCode());
    }
}
