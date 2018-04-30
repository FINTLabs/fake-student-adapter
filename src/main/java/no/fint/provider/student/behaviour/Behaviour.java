package no.fint.provider.student.behaviour;

import no.fint.event.model.Event;
import no.fint.event.model.Problem;
import org.jooq.lambda.function.Consumer2;

public interface Behaviour<T> extends Consumer2<Event,T> {
    default Problem newProblem(String field, String message) {
        Problem problem = new Problem();
        problem.setField(field);
        problem.setMessage(message);
        return problem;
    }
}
