package no.fint.provider.student.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.event.model.Status;
import no.fint.model.resource.FintLinks;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.model.utdanning.elev.ElevActions;
import no.fint.provider.student.behaviour.Behaviour;
import no.fint.provider.student.service.Handler;
import no.fint.provider.student.service.IdentifikatorFactory;
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
public class SkoleressursRepository implements Handler {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    IdentifikatorFactory identifikatorFactory;

    @Getter
    private Collection<SkoleressursResource> repository = new ConcurrentLinkedQueue<>();

    @Autowired
    private List<Behaviour<SkoleressursResource>> behaviours;

    @PostConstruct
    public void init() throws IOException {
        for (Resource r : new PathMatchingResourcePatternResolver(getClass().getClassLoader()).getResources("classpath*:/skoleressurs*.json")) {
            repository.add(objectMapper.readValue(r.getInputStream(), SkoleressursResource.class));
        }
    }

    @Override
    public Set<String> actions() {
        return ImmutableSet.of(ElevActions.GET_ALL_SKOLERESSURS.name(), ElevActions.UPDATE_SKOLERESSURS.name());
    }

    @Override
    public void accept(Event<FintLinks> response) {
        log.debug("Handling {} ...", response);
        log.trace("Event data: {}", response.getData());
        try {
            switch (ElevActions.valueOf(response.getAction())) {
                case GET_ALL_SKOLERESSURS:
                    response.setData(new ArrayList<>(repository));
                    break;
                case UPDATE_SKOLERESSURS:
                    List<SkoleressursResource> data = objectMapper.convertValue(response.getData(), objectMapper.getTypeFactory().constructCollectionType(List.class, SkoleressursResource.class));
                    log.trace("Converted data: {}", data);
                    data.stream().filter(i-> i.getSystemId()==null||i.getSystemId().getIdentifikatorverdi()==null).forEach(i->i.setSystemId(identifikatorFactory.create()));
                    repository.addAll(data);
                    response.setResponseStatus(ResponseStatus.ACCEPTED);
                    response.setData(null);
                    behaviours.forEach(b -> data.forEach(b.acceptPartially(response)));
                    if (response.getResponseStatus() == ResponseStatus.ACCEPTED) {
                        response.setData(new ArrayList<>(data));
                        data.forEach(r -> repository.removeIf(i -> i.getSystemId().getIdentifikatorverdi().equals(r.getSystemId().getIdentifikatorverdi())));
                        repository.addAll(data);
                    }
                    break;
                default:
                    response.setStatus(Status.ADAPTER_REJECTED);
                    response.setResponseStatus(ResponseStatus.REJECTED);
                    response.setMessage("Invalid action");
            }
        } catch (Exception e) {
            log.error("Error!", e);
            response.setResponseStatus(ResponseStatus.ERROR);
            response.setMessage(e.getMessage());
        }
    }

}
