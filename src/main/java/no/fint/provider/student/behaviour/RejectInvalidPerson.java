package no.fint.provider.student.behaviour;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class RejectInvalidPerson implements Behaviour<PersonResource> {
    @Override
    public void accept(Event event, PersonResource person) {
        if (person.getFodselsnummer()==null||person.getFodselsnummer().getIdentifikatorverdi()==null) {
            addProblem(event, "fodselsnummer", "Mangler fødselsnummer");
        }
        if (isNull(person.getNavn())) {
            addProblem(event, "navn", "Mangler navn");
        } else {
            if (isNullOrEmpty(person.getNavn().getFornavn())) {
                addProblem(event, "navn.fornavn", "Mangler fornavn");
            }
            if (isNullOrEmpty(person.getNavn().getEtternavn())) {
                addProblem(event, "navn.etternavn", "Mangler etternavn");
            }
        }
        if (isNull(person.getFodselsdato())) {
            addProblem(event, "fodselsdato", "Mangler fødselsdato");
        }
        if (!event.getProblems().isEmpty()) {
            log.info("Rejecting {}", event);
            event.setResponseStatus(ResponseStatus.REJECTED);
            event.setMessage("Incomplete object.");
        }
    }
}
