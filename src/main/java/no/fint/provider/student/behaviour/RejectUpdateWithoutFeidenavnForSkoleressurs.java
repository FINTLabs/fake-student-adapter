package no.fint.provider.student.behaviour;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.utdanning.elev.SkoleressursResource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class RejectUpdateWithoutFeidenavnForSkoleressurs implements Behaviour<SkoleressursResource> {
    @Override
    public void accept(Event event, SkoleressursResource resource) {
        if (Objects.isNull(resource.getFeidenavn()) || Strings.isNullOrEmpty(resource.getFeidenavn().getIdentifikatorverdi())) {
            addProblem(event, "feidenavn", "Mangler Feidenavn");
            event.setResponseStatus(ResponseStatus.REJECTED);
            log.info("Rejecting {}", event);
        }
    }
}
