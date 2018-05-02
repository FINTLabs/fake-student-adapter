package no.fint.provider.student.behaviour;

import no.fint.event.model.Event;
import no.fint.event.model.Problem;
import org.jooq.lambda.function.Consumer2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public interface Behaviour<T> extends Consumer2<Event,T> {
    default void addProblem(Event event, String field, String message) {
        if (Objects.isNull(event.getProblems())) {
            event.setProblems(new ArrayList<>());
        }
        Problem problem = new Problem();
        problem.setField(field);
        problem.setMessage(message);
        event.getProblems().add(problem);
    }

    default boolean empty(List<?> l) {
        return Objects.isNull(l) || l.isEmpty();
    }
}
