package no.fint.provider.student.behaviour;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import no.fint.provider.student.model.SkoleressursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class ProhibitsUpdatesToFeideidForSkoleressurs implements Behaviour<SkoleressursResource> {
    @Autowired
    SkoleressursRepository repository;

    @Override
    public void accept(Event event, SkoleressursResource skoleressurs) {
        Optional<SkoleressursResource> existing = repository.getRepository().stream().filter(e -> e.getSystemId().equals(skoleressurs.getSystemId())).findAny();
        existing.ifPresent(e -> {
            if (Objects.nonNull(e.getFeidenavn()) && !Strings.isNullOrEmpty(e.getFeidenavn().getIdentifikatorverdi())) {
                addProblem(event, "feidenavn", "Feidenavn er allerede satt");
                event.setResponseStatus(ResponseStatus.REJECTED);
                log.info("Rejecting {}", event);
            }
        });
    }
}
