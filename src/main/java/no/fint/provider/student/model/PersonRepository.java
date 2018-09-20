package no.fint.provider.student.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.felles.FellesActions;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.felles.PersonResource;
import no.fint.provider.student.behaviour.Behaviour;
import no.fint.provider.student.generate.FakeData;
import no.fint.provider.student.service.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Repository
public class PersonRepository implements Handler {

    @Autowired
    ObjectMapper objectMapper;

    private Collection<PersonResource> repository = new ConcurrentLinkedQueue<>();

    @Autowired
    private List<Behaviour<PersonResource>> behaviours;

    @Autowired
    private FakeData fakeData;

    @PostConstruct
    public void init() throws IOException {
        for (Resource r : new PathMatchingResourcePatternResolver(getClass().getClassLoader()).getResources("classpath*:/person*.json")) {
            repository.add(objectMapper.readValue(r.getInputStream(), PersonResource.class));
        }
        repository.addAll(fakeData.getPersoner());
        log.info("Repository contains {} items.", repository.size());
    }

    @Override
    public Set<String> actions() {
        return ImmutableSet.of(FellesActions.GET_ALL_PERSON.name(), FellesActions.UPDATE_PERSON.name());
    }

    @Override
    public void accept(Event<FintLinks> response) {
        log.debug("Handling {} ...", response);
        log.trace("Event data: {}", response.getData());
        switch (FellesActions.valueOf(response.getAction())) {
            case GET_ALL_PERSON:
                response.setData(new ArrayList<>(repository));
                break;
            case UPDATE_PERSON:
                List<PersonResource> data = objectMapper.convertValue(response.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, PersonResource.class));
                log.trace("Converted data: {}", data);
                response.setResponseStatus(ResponseStatus.ACCEPTED);
                response.setData(null);
                behaviours.forEach(b -> data.forEach(b.acceptPartially(response)));
                if (response.getResponseStatus() == ResponseStatus.ACCEPTED) {
                    response.setData(new ArrayList<>(data));
                    data.forEach(r -> repository.removeIf(i -> i.getFodselsnummer().getIdentifikatorverdi().equals(r.getFodselsnummer().getIdentifikatorverdi())));
                    repository.addAll(data);
                }
                break;
            default:
                response.setStatus(Status.ADAPTER_REJECTED);
                response.setResponseStatus(ResponseStatus.REJECTED);
                response.setMessage("Invalid action");
        }
    }

}
