package no.fint.provider.student.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource;
import no.fint.model.utdanning.elev.ElevActions;
import no.fint.provider.student.generate.FakeData;
import no.fint.provider.student.service.Handler;
import no.fint.provider.student.service.IdentifikatorFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Repository
public class KontaktlarergruppeRepository implements Handler {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IdentifikatorFactory identifikatorFactory;

    @Getter
    private Collection<KontaktlarergruppeResource> repository = new ConcurrentLinkedQueue<>();

    @Autowired
    private FakeData fakeData;

    @PostConstruct
    public void init() {
        repository.addAll(fakeData.getKontaktlarergrupper());
        log.info("Repository contains {} items.", repository.size());
    }

    @Override
    public Set<String> actions() {
        return ImmutableSet.of(ElevActions.GET_ALL_KONTAKTLARERGRUPPE.name());
    }

    @Override
    public void accept(Event<FintLinks> response) {
        log.debug("Handling {} ...", response);
        log.trace("Event data: {}", response.getData());
        if (ElevActions.valueOf(response.getAction()) == ElevActions.GET_ALL_KONTAKTLARERGRUPPE) {
            response.setData(new ArrayList<>(repository));
        } else {
            response.setStatus(Status.ADAPTER_REJECTED);
            response.setResponseStatus(ResponseStatus.REJECTED);
            response.setMessage("Invalid action");
        }
    }

}
