package no.fint.provider.student.generate;

import lombok.Getter;
import no.fint.fake.person.PersonGenerator;
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon;
import no.fint.model.felles.kompleksedatatyper.Periode;
import no.fint.model.resource.Link;
import no.fint.model.resource.felles.PersonResource;
import no.fint.model.resource.utdanning.elev.BasisgruppeResource;
import no.fint.model.resource.utdanning.elev.ElevResource;
import no.fint.model.resource.utdanning.elev.ElevforholdResource;
import no.fint.model.resource.utdanning.elev.KontaktlarergruppeResource;
import no.fint.model.resource.utdanning.timeplan.FagResource;
import no.fint.model.resource.utdanning.timeplan.UndervisningsgruppeResource;
import no.fint.model.resource.utdanning.utdanningsprogram.SkoleResource;
import no.fint.model.utdanning.utdanningsprogram.Skole;
import no.fint.provider.student.service.GrepService;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class FakeData {

    @Value("${fint.adapter.fake.students:100}")
    private int antallElever;

    @Value("${fint.adapter.fake.groups:10}")
    private int antallGrupper;

    @Value("${fint.adapter.fake.subjects:3}")
    private int antallFag;

    @Value("${fint.adapter.organizations}")
    private String orgId;

    @Value("${fint.adapter.fake.year:2019}")
    private int skoleaar;

    private String[] programmer = {"BA", "DH", "EL", "HS", "ID", "KD", "MD", "ME", "MK", "NA", "PB", "RM", "SS", "ST", "TP"};

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
    private List<SkoleResource> skoler;

    @Getter
    private List<FagResource> fag;

    @Getter
    private List<UndervisningsgruppeResource> undervisningsgrupper;

    @Autowired
    private PersonGenerator personGenerator;

    @Autowired
    private GrepService grepService;

    @PostConstruct
    public void init() {
        fag = grepService.getFag();

        ThreadLocalRandom random = ThreadLocalRandom.current();

        skoler = new ArrayList<>(1);
        SkoleResource skoleResource = new SkoleResource();

        skoleResource.setSkolenummer(personGenerator.identifikator("42"));
        skoleResource.setSystemId(personGenerator.identifikator("42"));
        skoleResource.setNavn("Jalla videregående skole");
        skoleResource.setOrganisasjonsnummer(personGenerator.identifikator("999999999"));
        skoler.add(skoleResource);

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
        periode.setStart(Date.from(LocalDate.of(skoleaar, 8, 20).atStartOfDay(ZoneId.of("UTC")).toInstant()));
        periode.setSlutt(Date.from(LocalDate.of(skoleaar + 1, 6, 21).atStartOfDay(ZoneId.of("UTC")).toInstant()));
        periode.setBeskrivelse(String.format("%d-%d", skoleaar, skoleaar + 1));

        fag.forEach(r -> r.setPeriode(Collections.singletonList(periode)));

        basisgrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            BasisgruppeResource r = new BasisgruppeResource();
            r.setNavn(gruppekode(i));
            r.setBeskrivelse("BG " + r.getNavn());
            r.setPeriode(Collections.singletonList(periode));
            r.setSystemId(personGenerator.identifikator(Integer.toString(1000 + i)));
            return r;
        }).collect(Collectors.toList());

        kontaktlarergrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            KontaktlarergruppeResource r = new KontaktlarergruppeResource();
            r.setNavn("K" + gruppekode(i));
            r.setBeskrivelse("KG " + r.getNavn());
            r.setPeriode(Collections.singletonList(periode));
            r.setSystemId(personGenerator.identifikator(Integer.toString(100 + i)));
            return r;
        }).collect(Collectors.toList());

        undervisningsgrupper = IntStream.rangeClosed(1, antallGrupper).mapToObj(i -> {
            FagResource f = sample(fag, random);
            UndervisningsgruppeResource r = new UndervisningsgruppeResource();
            r.setNavn(String.format("%s-%d", f.getNavn(), i));
            r.setBeskrivelse(f.getBeskrivelse());
            r.setPeriode(Collections.singletonList(periode));
            r.addFag(Link.with(FagResource.class, "systemid", f.getSystemId().getIdentifikatorverdi()));
            r.setSystemId(personGenerator.identifikator(Integer.toString(1000 + i)));
            skoleResource.addFag(Link.with(FagResource.class, "systemid", f.getSystemId().getIdentifikatorverdi()));
            f.addSkole(Link.with(Skole.class, "skolenummer", "42"));
            return r;
        }).collect(Collectors.toList());

        elevforhold.forEach(e -> {
            BasisgruppeResource b = sample(basisgrupper, random);
            KontaktlarergruppeResource k = sample(kontaktlarergrupper, random);
            e.addKontaktlarergruppe(Link.with(k.getClass(), "systemid", k.getSystemId().getIdentifikatorverdi()));
            e.addBasisgruppe(Link.with(b.getClass(), "systemid", b.getSystemId().getIdentifikatorverdi()));
            k.addElevforhold(Link.with(e.getClass(), "systemid", e.getSystemId().getIdentifikatorverdi()));
            b.addElevforhold(Link.with(e.getClass(), "systemid", e.getSystemId().getIdentifikatorverdi()));

            e.addSkole(Link.with(Skole.class, "skolenummer", "42"));
            b.addSkole(Link.with(Skole.class, "skolenummer", "42"));
            k.addSkole(Link.with(Skole.class, "skolenummer", "42"));
            skoleResource.addElevforhold(Link.with(e.getClass(), "systemid", e.getSystemId().getIdentifikatorverdi()));
            skoleResource.addBasisgruppe(Link.with(b.getClass(), "systemid", b.getSystemId().getIdentifikatorverdi()));
            skoleResource.addKontaktlarergruppe(Link.with(k.getClass(), "systemid", k.getSystemId().getIdentifikatorverdi()));

            IntStream.rangeClosed(1, antallFag).forEach(i -> {
                UndervisningsgruppeResource u = sample(undervisningsgrupper, random);
                u.addElevforhold(Link.with(e.getClass(), "systemid", e.getSystemId().getIdentifikatorverdi()));
                u.addSkole(Link.with(Skole.class, "skolenummer", "42"));
                skoleResource.addUndervisningsgruppe(Link.with(u.getClass(), "systemid", u.getSystemId().getIdentifikatorverdi()));
                e.addUndervisningsgruppe(Link.with(u.getClass(), "systemid", u.getSystemId().getIdentifikatorverdi()));
            });
        });

    }

    public String gruppekode(int id) {
        assert id <= programmer.length * 26 * 3;
        id--;
        int trinn = id % 3 + 1;
        id /= 3;
        int div = programmer.length;
        int mod = id % div;
        int p = id / div;
        return String.format("%d%s%s", trinn, programmer[mod], (char) ('A' + p));
    }

    private static <T> T sample(List<T> collection, ThreadLocalRandom random) {
        return collection.get(random.nextInt(collection.size()));
    }

}
