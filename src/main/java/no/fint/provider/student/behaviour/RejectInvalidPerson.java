package no.fint.provider.student.behaviour;

import lombok.extern.slf4j.Slf4j;
import no.fint.event.model.Event;
import no.fint.event.model.Problem;
import no.fint.event.model.ResponseStatus;
import no.fint.model.resource.felles.PersonResource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Objects.isNull;

@Service
@Slf4j
public class RejectInvalidPerson implements Behaviour<PersonResource> {
    @Override
    public void accept(Event event, PersonResource person) {
        List<Problem> problems = new ArrayList<>();
        if (person.getFodselsnummer()==null||person.getFodselsnummer().getIdentifikatorverdi()==null) {
            problems.add(newProblem("fodselsnummer", "Mangler fødselsnummer"));
        }
        if (isNull(person.getNavn())) {
            problems.add(newProblem("navn", "Mangler navn"));
        } else {
            if (isNullOrEmpty(person.getNavn().getFornavn())) {
                problems.add(newProblem("navn.fornavn", "Mangler fornavn"));
            }
            if (isNullOrEmpty(person.getNavn().getEtternavn())) {
                problems.add(newProblem("navn.etternavn", "Mangler etternavn"));
            }
        }
        if (isNull(person.getFodselsdato())) {
            problems.add(newProblem("fodselsdato", "Mangler fødselsdato"));
        }
        if (!problems.isEmpty()) {
            log.info("Rejecting {}", event);
            event.setResponseStatus(ResponseStatus.REJECTED);
            event.setMessage("Incomplete object.");
        }
    }
}
