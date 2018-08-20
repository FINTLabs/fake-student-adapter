package no.fint.provider.student.generate;

import lombok.Getter;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
public class FakeData {

    @Value("${fint.adapter.fake.students:100}")
    private int antallElever;

    @Value("${fint.adapter.fake.groups:10}")
    private int antallGrupper;

    @Value("${fint.adapter.organizations}")
    private String orgId;

    private String[] programmer = { "BA", "DH", "EL", "HS", "ID", "KD", "MD", "ME", "MK", "NA", "PB", "RM", "SS", "ST", "TP" };

    @Getter
    private List<PersonResource> personer;

    @Getter
    private List<ElevResource> elever;

    @Getter
    private List<ElevforholdResource> elevforhold;

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
        elevforhold = new ArrayList<>(antallElever);
        for (int i = 0; i < antallElever; i++) {
            String systemid = Integer.toString(50000 + i);
            PersonResource personResource = personGenerator.generatePerson();
            personResource.addElev(Link.with(ElevResource.class, "systemid", systemid));

            ElevResource elevResource = new ElevResource();
            Kontaktinformasjon kontaktinformasjon = new Kontaktinformasjon();
            kontaktinformasjon.setEpostadresse(String.format("%s@%s", systemid, orgId));
            elevResource.setKontaktinformasjon(kontaktinformasjon);
            elevResource.setElevnummer(personGenerator.identifikator(systemid));
            elevResource.setSystemId(personGenerator.identifikator(systemid));
            elevResource.setFeidenavn(personGenerator.identifikator(String.format("%s@%s", systemid, orgId)));
            elevResource.addPerson(Link.with(PersonResource.class, "fodselsnummer", personResource.getFodselsnummer().getIdentifikatorverdi()));
            elevResource.addElevforhold(Link.with(ElevforholdResource.class, "systemid", systemid));

            ElevforholdResource elevforholdResource = new ElevforholdResource();
            elevforholdResource.addElev(Link.with(ElevResource.class, "systemid", systemid));
            elevforholdResource.setSystemId(personGenerator.identifikator(systemid));
            elevforholdResource.setBeskrivelse(systemid);

            elevforhold.add(i, elevforholdResource);
            elever.add(i, elevResource);
            personer.add(i, personResource);
        }

        personer.stream().map(PersonResource::getNavn).map(PersonGenerator::getPersonnavnAsString).forEach(System.out::println);

        Periode periode = new Periode();
        periode.setStart(Date.from(LocalDate.of(2018,8,20).atStartOfDay(ZoneId.of("UTC")).toInstant()));
        periode.setSlutt(Date.from(LocalDate.of(2019,6,21).atStartOfDay(ZoneId.of("UTC")).toInstant()));
        periode.setBeskrivelse("2018-2019");

        basisgrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            BasisgruppeResource r = new BasisgruppeResource();
            r.setNavn(gruppekode(i));
            r.setBeskrivelse(r.getNavn());
            r.setPeriode(Collections.singletonList(periode));
            r.setSystemId(personGenerator.identifikator(Integer.toString(1000 + i)));
            return r;
        }).collect(Collectors.toList());

        kontaktlarergrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            KontaktlarergruppeResource r = new KontaktlarergruppeResource();
            r.setNavn("K" + gruppekode(i));
            r.setBeskrivelse(r.getNavn());
            r.setPeriode(Collections.singletonList(periode));
            r.setSystemId(personGenerator.identifikator(Integer.toString(100 + i)));
            return r;
        }).collect(Collectors.toList());

        ThreadLocalRandom r = ThreadLocalRandom.current();
        AtomicInteger id = new AtomicInteger(100000);

        medlemskap = Stream.concat(
                elevforhold.stream().map(e -> {
                    MedlemskapResource m = new MedlemskapResource();
                    String systemId = Integer.toString(id.incrementAndGet());
                    m.setSystemId(personGenerator.identifikator(systemId));
                    m.addMedlem(Link.with(ElevforholdResource.class, "systemid", e.getSystemId().getIdentifikatorverdi()));
                    BasisgruppeResource g = personGenerator.sample(basisgrupper, r);
                    m.addGruppe(Link.with(BasisgruppeResource.class, "systemid", g.getSystemId().getIdentifikatorverdi()));
                    Link link = Link.with(MedlemskapResource.class, "systemid", systemId);
                    e.addMedlemskap(link);
                    g.addMedlemskap(link);
                    return m;
                }),
                elevforhold.stream().map(e -> {
                    MedlemskapResource m = new MedlemskapResource();
                    String systemId = Integer.toString(id.incrementAndGet());
                    m.setSystemId(personGenerator.identifikator(systemId));
                    m.addMedlem(Link.with(ElevforholdResource.class, "systemid", e.getSystemId().getIdentifikatorverdi()));
                    KontaktlarergruppeResource g = personGenerator.sample(kontaktlarergrupper, r);
                    m.addGruppe(Link.with(KontaktlarergruppeResource.class, "systemid", g.getSystemId().getIdentifikatorverdi()));
                    Link link = Link.with(MedlemskapResource.class, "systemid", systemId);
                    e.addMedlemskap(link);
                    g.addMedlemskap(link);
                    return m;
                })).collect(Collectors.toList());
    }

    public String gruppekode(int id) {
        assert id <= programmer.length * 26 * 3;
        id--;
        int trinn = id % 3 + 1;
        id /= 3;
        int div = programmer.length;
        int mod = id % div;
        int p = id / div;
        return String.format("%d%s%s", trinn, programmer[mod], (char)('A' + p));
    }

}
