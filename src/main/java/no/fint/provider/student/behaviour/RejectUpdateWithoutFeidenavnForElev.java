package no.fint.provider.student.behaviour;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.utdanning.elev.ElevResource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Objects;

@Service
@Slf4j
public class RejectUpdateWithoutFeidenavnForElev implements Behaviour<ElevResource> {
    @Override
    public void accept(Event event, ElevResource elev) {
        if (Objects.isNull(elev.getFeidenavn()) || Strings.isNullOrEmpty(elev.getFeidenavn().getIdentifikatorverdi())) {
            event.setProblems(Collections.singletonList(newProblem("feidenavn", "Mangler Feidenavn")));
            event.setResponseStatus(ResponseStatus.REJECTED);
            log.info("Rejecting {}", event);
        }
    }
}
