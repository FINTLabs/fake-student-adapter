package no.fint.provider.student.generate

import no.fint.fake.person.Adresser
import no.fint.fake.person.Navn
import no.fint.fake.person.PersonGenerator
import no.fint.model.felles.Person
import no.fint.model.resource.Link
import no.fint.model.utdanning.elev.Basisgruppe
import no.fint.model.utdanning.elev.Elev
import no.fint.model.utdanning.elev.Elevforhold
import no.fint.model.utdanning.elev.Kontaktlarergruppe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

import java.util.concurrent.ThreadLocalRandom

@SpringBootTest(classes = [FakeData, PersonGenerator, Navn, Adresser])
class FakeDataSpec extends Specification {
    @Autowired
    FakeData fakeData

    def "Loads correctly"() {
        when:
        println(fakeData.personer)
        println(fakeData.elever)
        println(fakeData.basisgrupper)
        println(fakeData.kontaktlarergrupper)

        then:
        noExceptionThrown()
    }

    def "Verify generation of group codes"() {
        when:
        def g = (1..1170).collect(fakeData.&gruppekode)

        then:
        g.contains('1BAA')
        g.contains('2BAA')
        g.contains('3BAA')
        g.contains('1DHA')
        g.contains('1TPA')
        g.contains('1STA')
        g.contains('3TPZ')
    }

    def "Consistently linked"() {
        given:
        fakeData.personer.each { p -> p.addSelf(Link.with(Person, "fodselsnummer", p.fodselsnummer.identifikatorverdi)) }
        fakeData.elever.each { e -> e.addSelf(Link.with(Elev, "elevnummer", e.elevnummer.identifikatorverdi)) }
        fakeData.elevforhold.each { f -> f.addSelf(Link.with(Elevforhold, "systemid", f.systemId.identifikatorverdi)) }
        fakeData.basisgrupper.each { b -> b.addSelf(Link.with(Basisgruppe, "systemid", b.systemId.identifikatorverdi)) }
        fakeData.kontaktlarergrupper.each { k -> k.addSelf(Link.with(Kontaktlarergruppe, "systemid", k.systemId.identifikatorverdi)) }
        def p = fakeData.personer[ThreadLocalRandom.current().nextInt(fakeData.personer.size())]
        when:
        def e = p.elev[0]

        then:
        fakeData.elever.any { it -> it.selfLinks.any { e.&equals } && it.person.any { p.selfLinks[0].&equals } }

        when:
        def elev = fakeData.elever.find { it -> it.selfLinks.any { e.&equals } }
        def r = elev.elevforhold[0]

        then:
        fakeData.elevforhold.any { it -> it.selfLinks.any { r.&equals } && it.elev.any { e.&equals } }

        when:
        def forhold = fakeData.elevforhold.find { it -> it.selfLinks.any { r.&equals } }
        def f = forhold.selfLinks[0]

        then:
        fakeData.basisgrupper.any { it ->
            it.elevforhold.any {
                f.&equals
            } && forhold.basisgruppe.any { g -> it.selfLinks[0].&equals }
        }
        fakeData.kontaktlarergrupper.any { it ->
            it.elevforhold.any {
                f.&equals
            } && forhold.kontaktlarergruppe.any { g -> it.selfLinks[0].&equals }
        }
    }
}
