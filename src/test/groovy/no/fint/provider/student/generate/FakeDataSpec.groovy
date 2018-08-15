package no.fint.provider.student.generate

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

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
        println(fakeData.medlemskap)

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
}
