package no.fint.provider.student.service


import org.springframework.web.client.RestTemplate
import spock.lang.Specification

class GrepServiceSpec extends Specification {

    def 'Fetching Grep Fagkoder'() {
        given:
        def service = new GrepService(restTemplate: new RestTemplate())

        when:
        def results = service.getGrepFagkoder()

        then:
        noExceptionThrown()
        results.any { it.Kode == 'KHVK001' }
    }

    def 'Fetching and mapping FagResource'() {
        given:
        def service = new GrepService(restTemplate: new RestTemplate())

        when:
        def result = service.getFag()
        println(result[12])

        then:
        noExceptionThrown()
        result.size() > 0
        result.any { it.navn == 'PSP5738' }
    }
}
