package no.fint.provider.student.behaviour;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.provider.student.model.ElevRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ProhibitsUpdatesToFeideidForElev implements Behaviour<ElevResource> {
    @Autowired
    ElevRepository elevRepository;

    @Override
    public void accept(Event event, ElevResource elev) {
        Optional<ElevResource> existingElev = elevRepository.getRepository().stream().filter(e -> e.getElevnummer().equals(elev.getElevnummer())).findAny();
        existingElev.ifPresent(e -> {
            if (Objects.nonNull(e.getFeidenavn()) && !Strings.isNullOrEmpty(e.getFeidenavn().getIdentifikatorverdi())) {
                event.setProblems(Collections.singletonList(newProblem("feidenavn", "Feidenavn er allerede satt")));
                event.setResponseStatus(ResponseStatus.REJECTED);
                log.info("Rejecting {}", event);
            }
        });
    }
}
