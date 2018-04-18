package no.fint.provider.student.service;

import no.fint.event.model.Event;
import no.fint.model.resource.FintLinks;
import no.fint.model.utdanning.elev.ElevActions;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

public interface Handler extends Consumer<Event<FintLinks>> {

    default Set<String> actions() {
        return Collections.emptySet();
    }

}
