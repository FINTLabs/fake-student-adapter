package no.fint.provider.student.generate;

import lombok.Getter;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource;
import no.fint.model.resource.utdanning.elev.MedlemskapResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class FakeData {

    @Value("${fint.adapter.fake.students:1000}")
    private int antallElever;

    @Value("${fint.adapter.fake.groups:100}")
    private int antallGrupper;

    @Getter
    private List<PersonResource> personer;

    @Getter
    private List<ElevResource> elever;

    @Getter
    private List<BasisgruppeResource> basisgrupper;

    @Getter
    private List<KontaktlarergruppeResource> kontaktlarergrupper;

    @Getter
    private List<MedlemskapResource> medlemskap;

    @Autowired
    private PersonGenerator personGenerator;

    @PostConstruct
    public void init() {
        personer = new ArrayList<>(antallElever);
        elever = new ArrayList<>(antallElever);
        for (int i = 0; i < antallElever; i++) {
            PersonResource personResource = personGenerator.generatePerson();
            String systemid = Integer.toString(50000 + i);
            ElevResource elevResource = new ElevResource();
            elevResource.setSystemId(personGenerator.identifikator(systemid));
            elevResource.addPerson(Link.with(PersonResource.class, "fodselsnummer", personResource.getFodselsnummer().getIdentifikatorverdi()));
            personResource.addElev(Link.with(ElevResource.class, "systemid", systemid));
            elever.add(i, elevResource);
            personer.add(i, personResource);
        }

        basisgrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            BasisgruppeResource r = new BasisgruppeResource();
            r.setNavn(String.format("1ST%s", (char)('A' + i - 1)));
            r.setSystemId(personGenerator.identifikator(Integer.toString(1000 + i)));
            return r;
        }).collect(Collectors.toList());

        kontaktlarergrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            KontaktlarergruppeResource r = new KontaktlarergruppeResource();
            r.setNavn(String.format("1SP%s", (char)('A' + i - 1)));
            r.setSystemId(personGenerator.identifikator(Integer.toString(100 + i)));
            return r;
        }).collect(Collectors.toList());

        ThreadLocalRandom r = ThreadLocalRandom.current();

        medlemskap = Stream.concat(
                elever.stream().filter(i -> r.nextBoolean()).limit(antallElever/antallGrupper).map(e -> {
                    MedlemskapResource m = new MedlemskapResource();
                    m.addMedlem(Link.with(ElevResource.class, "systemid", e.getSystemId().getIdentifikatorverdi()));
                    BasisgruppeResource g = personGenerator.sample(basisgrupper, r);
                    m.addGruppe(Link.with(BasisgruppeResource.class, "systemid", g.getSystemId().getIdentifikatorverdi()));
                    return m;
                }),
                elever.stream().filter(i -> r.nextBoolean()).limit(antallElever/antallGrupper).map(e -> {
                    MedlemskapResource m = new MedlemskapResource();
                    m.addMedlem(Link.with(ElevResource.class, "systemid", e.getSystemId().getIdentifikatorverdi()));
                    KontaktlarergruppeResource g = personGenerator.sample(kontaktlarergrupper, r);
                    m.addGruppe(Link.with(KontaktlarergruppeResource.class, "systemid", g.getSystemId().getIdentifikatorverdi()));
                    return m;
                })).collect(Collectors.toList());
    }

}
