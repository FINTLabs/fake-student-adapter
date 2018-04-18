package no.fint.provider.student;

import no.fint.provider.adapter.AbstractSupportedActions;
import no.fint.provider.student.service.Handler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Collection;

@Component
public class SupportedActions extends AbstractSupportedActions {

    @Autowired
    private Collection<Handler> handlers;

    @PostConstruct
    public void addSupportedActions() {
        handlers.stream().flatMap(h -> h.actions().stream()).forEach(this::add);
    }

}
