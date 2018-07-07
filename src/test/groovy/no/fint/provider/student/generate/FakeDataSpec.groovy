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
}
