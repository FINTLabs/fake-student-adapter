package no.fint.provider.student.service;

import no.fint.model.felles.kompleksedatatyper.Identifikator;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class IdentifikatorFactory {

    private AtomicInteger atomicInteger = new AtomicInteger();

    public Identifikator create() {
        Identifikator identifikator = new Identifikator();
        identifikator.setIdentifikatorverdi(String.format("ID%06d", atomicInteger.incrementAndGet()));
        return identifikator;
    }
}
