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
        def k1 = fakeData.gruppekode(1)
        then:
        k1 == '1BAA'

        when:
        k1 = fakeData.gruppekode(10)
        then:
        k1 == '1BAJ'

        when:
        k1 = fakeData.gruppekode(26)
        then:
        k1 == '1BAZ'

        when:
        k1 = fakeData.gruppekode(27)
        then:
        k1 == '1DHA'

        when:
        k1 = fakeData.gruppekode(339)
        then:
        k1 == '1STA'

        when:
        k1 = fakeData.gruppekode(390)
        then:
        k1 == '1TPZ'
    }
}
